package com.haedong.erp.domains.duty;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.common.NameNormalizer;
import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.duty.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DutyService {

    private final DutyMapper dutyMapper;

    @Transactional(readOnly = true)
    public List<DutyDriverResponse> drivers() {
        return dutyMapper.findDrivers();
    }

    /** 월 근무표: 전속기사(서열 순) × 1일~말일 */
    @Transactional(readOnly = true)
    public DutyRosterResponse roster(int year, int month) {
        YearMonth yearMonth = toYearMonth(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        Map<String, List<DutyStart>> startsByName = dutyMapper.findStartsUntil(to).stream()
                .collect(Collectors.groupingBy(DutyStart::getEmployeeName));
        Map<String, Map<LocalDate, Shift>> overridesByName = dutyMapper.findOverrides(from, to).stream()
                .collect(Collectors.groupingBy(DutyOverride::getEmployeeName,
                        Collectors.toMap(DutyOverride::getWorkDate, DutyOverride::getShift)));

        List<DutyRosterRow> rows = dutyMapper.findDrivers().stream()
                .map(driver -> new DutyRosterRow(
                        driver.getName(),
                        driver.getSeniorityNo(),
                        cells(from, to,
                                startsByName.getOrDefault(driver.getName(), List.of()),
                                overridesByName.getOrDefault(driver.getName(), Map.of()))))
                .toList();

        return new DutyRosterResponse(year, month, yearMonth.lengthOfMonth(), rows);
    }

    /** 한 기사의 시작점 이력과 각 시작점의 적용 기간 (최근 입력 순) */
    @Transactional(readOnly = true)
    public List<DutyStartHistoryResponse> startHistory(String employeeName) {
        String name = NameNormalizer.normalize(employeeName);
        List<DutyStartHistoryRow> rows = dutyMapper.findStartHistory(name);
        List<DutyStart> starts = rows.stream().map(row -> row.toStart(name)).toList();

        return rows.stream()
                .map(row -> {
                    DutyStart start = row.toStart(name);
                    boolean superseded = DutyPattern.isSuperseded(start, starts);
                    LocalDate appliedUntil = superseded
                            ? null
                            : DutyPattern.appliedUntil(start, starts).orElse(null);
                    return new DutyStartHistoryResponse(row.getId(), row.getBaseDate(), row.getCycleDay(),
                            row.getCreatedByName(), row.getCreatedAt(), superseded, appliedUntil);
                })
                .toList();
    }

    /** 시작점 일괄 입력. 덮어쓰지 않고 이력으로 추가한다 */
    @Transactional
    public int createStarts(DutyStartBulkRequest request, LoginUser currentUser) {
        List<DutyStartBulkRequest.Item> items = request.items().stream()
                .map(item -> new DutyStartBulkRequest.Item(
                        NameNormalizer.normalize(item.employeeName()), item.cycleDay()))
                .toList();

        Set<String> names = new HashSet<>();
        for (DutyStartBulkRequest.Item item : items) {
            if (!names.add(item.employeeName())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "같은 기사가 두 번 들어 있습니다: " + item.employeeName());
            }
        }

        Set<String> driverNames = dutyMapper.findDrivers().stream()
                .map(DutyDriverResponse::getName)
                .collect(Collectors.toSet());
        List<String> notDrivers = names.stream()
                .filter(name -> !driverNames.contains(name))
                .sorted()
                .toList();
        if (!notDrivers.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "전속기사가 아닌 직원이 있습니다: " + String.join(", ", notDrivers));
        }

        dutyMapper.insertStarts(request.baseDate(), items, currentUser.getId());
        return items.size();
    }

    /** 직원 이름이 바뀌면 근무표 데이터의 이름도 함께 바꾼다 (FK 없이 이름으로 연결하므로) */
    @Transactional
    public void renameEmployee(String oldName, String newName) {
        dutyMapper.renameStarts(oldName, newName);
        dutyMapper.renameOverrides(oldName, newName);
        dutyMapper.renameOverrideLogs(oldName, newName);
    }

    /** 날짜별 근무: ① 수동 수정 → ② 시작점으로 계산한 패턴 → ③ 미설정(null) */
    private List<DutyCell> cells(LocalDate from, LocalDate to,
                                 List<DutyStart> starts, Map<LocalDate, Shift> overrides) {
        return from.datesUntil(to.plusDays(1))
                .map(date -> {
                    Shift overridden = overrides.get(date);
                    if (overridden != null) {
                        return new DutyCell(overridden, true);
                    }
                    return new DutyCell(DutyPattern.shiftOn(date, starts).orElse(null), false);
                })
                .toList();
    }

    private static YearMonth toYearMonth(int year, int month) {
        if (year < 2000 || year > 2100 || month < 1 || month > 12) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "조회할 연월이 올바르지 않습니다.");
        }
        return YearMonth.of(year, month);
    }
}