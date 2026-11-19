package com.haedong.erp.domains.routeroster;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.common.NameNormalizer;
import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.duty.DutyService;
import com.haedong.erp.domains.duty.dto.DutyCell;
import com.haedong.erp.domains.duty.dto.DutyDriverResponse;
import com.haedong.erp.domains.routeroster.dto.RouteRosterAssignment;
import com.haedong.erp.domains.routeroster.dto.RouteRosterInitResponse;
import com.haedong.erp.domains.routeroster.dto.RouteRosterResponse;
import com.haedong.erp.domains.routeroster.dto.RouteRosterRow;
import com.haedong.erp.domains.routeroster.dto.RouteRosterSaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 노선 근무표: 노선·월마다 "몇 번째 줄에 어떤 기사"만 저장하고,
 * 날짜별 근무는 근무표(DutyService) 계산을 그대로 가져온다.
 */
@Service
@RequiredArgsConstructor
public class RouteRosterService {

    private final RouteRosterMapper routeRosterMapper;
    private final DutyService dutyService;

    /** 한 노선의 월 근무표 */
    @Transactional(readOnly = true)
    public RouteRosterResponse roster(int year, int month, String routeNo) {
        YearMonth yearMonth = toYearMonth(year, month);
        if (!routeRosterMapper.existsRoute(routeNo)) {
            throw routeNotFound();
        }
        LocalDate rosterMonth = yearMonth.atDay(1);

        List<RouteRosterLine> lines = routeRosterMapper.findLines(rosterMonth, routeNo);
        Set<String> activeDrivers = activeDriverNames();

        List<String> names = lines.stream()
                .map(RouteRosterLine::getEmployeeName)
                .filter(Objects::nonNull)
                .toList();
        Map<String, List<DutyCell>> cellsByName = dutyService.monthCells(names, yearMonth);

        List<RouteRosterRow> rows = lines.stream()
                .map(line -> new RouteRosterRow(
                        line.getSortOrder(),
                        line.getSlotNo(),
                        line.getVehicleNo(),
                        line.getAbLabel(),
                        line.getEmployeeName(),
                        line.getEmployeeName() != null && activeDrivers.contains(line.getEmployeeName()),
                        line.getEmployeeName() == null
                                ? List.of()
                                : cellsByName.getOrDefault(line.getEmployeeName(), List.of())))
                .toList();

        return new RouteRosterResponse(year, month, yearMonth.lengthOfMonth(), routeNo, rows,
                routeRosterMapper.findOtherAssignments(rosterMonth, routeNo));
    }

    /** 한 노선·한 달의 배치를 통째로 바꾼다. 목록 순서가 곧 출력 순서 */
    @Transactional
    public void save(int year, int month, String routeNo, RouteRosterSaveRequest request, LoginUser currentUser) {
        LocalDate rosterMonth = toYearMonth(year, month).atDay(1);
        lockRoute(routeNo);

        List<RouteRosterLine> lines = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (RouteRosterSaveRequest.Item item : request.rows()) {
            String name = blankToNull(NameNormalizer.normalize(item.employeeName()));
            if (name != null && !names.add(name)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "같은 기사가 두 번 들어 있습니다: " + name);
            }
            lines.add(new RouteRosterLine(lines.size() + 1, item.slotNo(),
                    blankToNull(item.vehicleNo() == null ? null : item.vehicleNo().strip()),
                    item.abLabel(), name));
        }

        Set<String> activeDrivers = activeDriverNames();
        List<String> notDrivers = names.stream()
                .filter(name -> !activeDrivers.contains(name))
                .sorted()
                .toList();
        if (!notDrivers.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "전속기사가 아닌 직원이 있습니다: " + String.join(", ", notDrivers));
        }

        List<String> elsewhere = routeRosterMapper.findOtherAssignments(rosterMonth, routeNo).stream()
                .filter(assignment -> names.contains(assignment.getEmployeeName()))
                .map(assignment -> assignment.getEmployeeName() + "(" + assignment.getRouteNo() + ")")
                .toList();
        if (!elsewhere.isEmpty()) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "이번 달 다른 노선에 이미 배치된 기사입니다: " + String.join(", ", elsewhere));
        }

        routeRosterMapper.deleteLines(rosterMonth, routeNo);
        if (!lines.isEmpty()) {
            routeRosterMapper.insertLines(rosterMonth, routeNo, lines, currentUser.getId());
        }
    }

    /**
     * 전달 배치를 복사해 이번 달 배치를 만든다.
     * 퇴사·서열 없는 기사, 이번 달 다른 노선에 이미 있는 기사는 빈자리로 바꾸고 결과에 알려준다.
     */
    @Transactional
    public RouteRosterInitResponse copyPrevious(int year, int month, String routeNo, LoginUser currentUser) {
        YearMonth yearMonth = toYearMonth(year, month);
        LocalDate rosterMonth = yearMonth.atDay(1);
        lockRoute(routeNo);
        rejectIfExists(rosterMonth, routeNo);

        YearMonth previous = yearMonth.minusMonths(1);
        List<RouteRosterLine> previousLines = routeRosterMapper.findLines(previous.atDay(1), routeNo);
        if (previousLines.isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND,
                    previous.getYear() + "년 " + previous.getMonthValue() + "월 배치가 없습니다.");
        }

        Set<String> activeDrivers = activeDriverNames();
        Set<String> elsewhere = routeRosterMapper.findOtherAssignments(rosterMonth, routeNo).stream()
                .map(RouteRosterAssignment::getEmployeeName)
                .collect(Collectors.toSet());

        Set<String> cleared = new LinkedHashSet<>();
        List<RouteRosterLine> lines = previousLines.stream()
                .map(line -> {
                    String name = line.getEmployeeName();
                    if (name != null && (!activeDrivers.contains(name) || elsewhere.contains(name))) {
                        cleared.add(name);
                        name = null;
                    }
                    return new RouteRosterLine(line.getSortOrder(), line.getSlotNo(), line.getVehicleNo(),
                            line.getAbLabel(), name);
                })
                .toList();

        routeRosterMapper.insertLines(rosterMonth, routeNo, lines, currentUser.getId());
        return new RouteRosterInitResponse(lines.size(), List.copyOf(cleared));
    }

    /** 차량 관리의 노선 차량(순번 순)으로 차량마다 A·B 두 줄을 만든다. 기사는 비어 있다 */
    @Transactional
    public RouteRosterInitResponse createFromVehicles(int year, int month, String routeNo, LoginUser currentUser) {
        LocalDate rosterMonth = toYearMonth(year, month).atDay(1);
        lockRoute(routeNo);
        rejectIfExists(rosterMonth, routeNo);

        List<RouteRosterLine> vehicles = routeRosterMapper.findVehicles(routeNo);
        if (vehicles.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이 노선에 배정된 차량이 없습니다.");
        }

        List<RouteRosterLine> lines = new ArrayList<>();
        for (RouteRosterLine vehicle : vehicles) {
            for (String abLabel : List.of("A", "B")) {
                lines.add(new RouteRosterLine(lines.size() + 1, vehicle.getSlotNo(), vehicle.getVehicleNo(),
                        abLabel, null));
            }
        }

        routeRosterMapper.insertLines(rosterMonth, routeNo, lines, currentUser.getId());
        return new RouteRosterInitResponse(lines.size(), List.of());
    }

    /** 직원 이름이 바뀌면 배치의 이름도 함께 바꾼다 (FK 없이 이름으로 연결하므로) */
    @Transactional
    public void renameEmployee(String oldName, String newName) {
        routeRosterMapper.renameEmployee(oldName, newName);
    }

    private Set<String> activeDriverNames() {
        return dutyService.drivers().stream()
                .map(DutyDriverResponse::getName)
                .collect(Collectors.toSet());
    }

    private void lockRoute(String routeNo) {
        if (routeRosterMapper.lockRoute(routeNo) == null) {
            throw routeNotFound();
        }
    }

    private void rejectIfExists(LocalDate rosterMonth, String routeNo) {
        if (routeRosterMapper.countLines(rosterMonth, routeNo) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "이미 이 달의 배치가 있습니다. 새로 만들려면 먼저 배치를 비워 주세요.");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static YearMonth toYearMonth(int year, int month) {
        if (year < 2000 || year > 2100 || month < 1 || month > 12) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "조회할 연월이 올바르지 않습니다.");
        }
        return YearMonth.of(year, month);
    }

    private static BusinessException routeNotFound() {
        return new BusinessException(HttpStatus.NOT_FOUND, "노선을 찾을 수 없습니다.");
    }
}
