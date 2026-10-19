package com.haedong.erp.domains.dispatch;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.domains.dispatch.dto.DispatchAllResponse;
import com.haedong.erp.domains.dispatch.dto.DispatchRow;
import com.haedong.erp.domains.dispatch.dto.DispatchTableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private final DispatchMapper dispatchMapper;

    /** 노선별 월 순위표 */
    @Transactional(readOnly = true)
    public DispatchTableResponse routeTable(String routeNo, int year, int month) {
        YearMonth yearMonth = toYearMonth(year, month);
        DispatchRoute route = dispatchMapper.findRoutes(routeNo).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "노선을 찾을 수 없습니다."));

        return buildTable(route, dispatchMapper.findSlots(routeNo), dispatchMapper.findRotations(routeNo), yearMonth);
    }

    /** 전체 노선 통합 순위표. 쿼리 3번으로 모든 노선을 읽고 노선별로 나눠 계산한다 */
    @Transactional(readOnly = true)
    public DispatchAllResponse allTables(int year, int month) {
        YearMonth yearMonth = toYearMonth(year, month);
        Map<String, List<DispatchSlot>> slotsByRoute = dispatchMapper.findSlots(null).stream()
                .collect(Collectors.groupingBy(DispatchSlot::getRouteNo));
        Map<String, List<RouteRotation>> rotationsByRoute = dispatchMapper.findRotations(null).stream()
                .collect(Collectors.groupingBy(RouteRotation::getRouteNo));

        List<DispatchTableResponse> tables = dispatchMapper.findRoutes(null).stream()
                .map(route -> buildTable(route,
                        slotsByRoute.getOrDefault(route.getRouteNo(), List.of()),
                        rotationsByRoute.getOrDefault(route.getRouteNo(), List.of()),
                        yearMonth))
                .toList();

        return new DispatchAllResponse(year, month, yearMonth.lengthOfMonth(), tables);
    }

    private DispatchTableResponse buildTable(DispatchRoute route, List<DispatchSlot> slots,
                                             List<RouteRotation> rotations, YearMonth yearMonth) {
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        int lastDay = yearMonth.lengthOfMonth();

        String reason = unavailableReason(route, slots, rotations, to);
        if (reason != null) {
            return new DispatchTableResponse(route.getRouteNo(), yearMonth.getYear(), yearMonth.getMonthValue(),
                    lastDay, route.getTotalSlots(), route.getBaseDate(), reason, List.of());
        }

        // 기준일이 이번 달 중간이면 기준일부터만 계산하고 그 앞은 비워 둔다
        LocalDate start = from.isBefore(route.getBaseDate()) ? route.getBaseDate() : from;
        int skippedDays = (int) ChronoUnit.DAYS.between(from, start);

        Map<Integer, String> vehicleBySlot = slots.stream()
                .collect(Collectors.toMap(DispatchSlot::getSlotNo, DispatchSlot::getVehicleNo));
        List<DispatchRotation.Rule> rules = rotations.stream().map(RouteRotation::toRule).toList();

        // 다음 달 1일까지 한 번에 계산한다 (마지막 열 = 다음 달 1일)
        String[][] table = DispatchRotation.table(route.getBaseDate(), route.getTotalSlots(), vehicleBySlot,
                rules, start, to.plusDays(1));

        List<DispatchRow> rows = new ArrayList<>();
        for (int i = 0; i < route.getTotalSlots(); i++) {
            List<String> days = new ArrayList<>(lastDay);
            for (int d = 0; d < skippedDays; d++) {
                days.add(null);
            }
            String[] computed = table[i];
            days.addAll(Arrays.asList(computed).subList(0, computed.length - 1));
            rows.add(new DispatchRow(i + 1, days, computed[computed.length - 1]));
        }

        return new DispatchTableResponse(route.getRouteNo(), yearMonth.getYear(), yearMonth.getMonthValue(),
                lastDay, route.getTotalSlots(), route.getBaseDate(), null, rows);
    }

    /** 순위표를 만들 수 없으면 그 이유, 만들 수 있으면 null */
    private String unavailableReason(DispatchRoute route, List<DispatchSlot> slots,
                                     List<RouteRotation> rotations, LocalDate monthEnd) {
        LocalDate baseDate = route.getBaseDate();
        if (baseDate == null) {
            return "순위표 기준일이 설정되지 않은 노선입니다.";
        }
        if (monthEnd.isBefore(baseDate)) {
            return "기준일(" + baseDate + ") 이전 달이라 표시할 수 없습니다.";
        }
        // 기준일 다음 날로 넘어갈 때부터 회전 값이 필요하다
        boolean hasRule = rotations.stream()
                .anyMatch(rotation -> !rotation.getEffectiveFrom().isAfter(baseDate.plusDays(1)));
        if (!hasRule) {
            return "기준일 다음 날부터 적용되는 회전 값이 없습니다.";
        }
        boolean slotOutOfRange = slots.stream()
                .anyMatch(slot -> slot.getSlotNo() > route.getTotalSlots());
        if (slotOutOfRange) {
            return "순번 수보다 큰 슬롯 번호를 가진 차량이 있습니다. 차량 관리에서 슬롯을 확인해 주세요.";
        }
        return null;
    }

    private static YearMonth toYearMonth(int year, int month) {
        if (year < 2000 || year > 2100 || month < 1 || month > 12) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "조회할 연월이 올바르지 않습니다.");
        }
        return YearMonth.of(year, month);
    }
}