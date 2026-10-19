package com.haedong.erp.domains.dispatch.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 노선별 월 순위표
 *
 * @param unavailableReason 순위표를 만들 수 없는 이유 (만들 수 있으면 null, 이때 rows 는 빈 목록)
 */
public record DispatchTableResponse(
        String routeNo,
        int year,
        int month,
        int lastDay,
        int totalSlots,
        LocalDate baseDate,
        String unavailableReason,
        List<DispatchRow> rows
) {
}