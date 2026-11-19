package com.haedong.erp.domains.routeroster.dto;

import com.haedong.erp.domains.duty.dto.DutyCell;

import java.util.List;

/**
 * 노선 근무표 화면의 한 줄.
 * - activeDriver: 지금 재직 중인 전속기사인지 (false 면 퇴사·서열 없음 → 화면에서 표시)
 * - cells: 1일~말일 근무. 근무표 메뉴와 같은 계산(시작점 + 수동 수정). 빈자리면 빈 목록
 */
public record RouteRosterRow(
        int sortOrder,
        Integer slotNo,
        String vehicleNo,
        String abLabel,
        String employeeName,
        boolean activeDriver,
        List<DutyCell> cells
) {
}
