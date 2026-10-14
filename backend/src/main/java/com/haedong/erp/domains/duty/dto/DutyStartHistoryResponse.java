package com.haedong.erp.domains.duty.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 시작점 이력 한 건.
 * superseded 면 나중 입력에 완전히 덮어써짐, 아니면 baseDate ~ appliedUntil 동안 적용 (appliedUntil 이 null 이면 계속)
 */
public record DutyStartHistoryResponse(
        Long id,
        LocalDate baseDate,
        int cycleDay,
        String createdByName,
        OffsetDateTime createdAt,
        boolean superseded,
        LocalDate appliedUntil
) {
}