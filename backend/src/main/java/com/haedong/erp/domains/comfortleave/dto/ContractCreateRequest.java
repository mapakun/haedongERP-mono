package com.haedong.erp.domains.comfortleave.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** 만료일을 비우면 시작일 + 1년 - 1일로 정한다 */
public record ContractCreateRequest(
        @NotNull(message = "계약 시작일을 입력해 주세요.") LocalDate startDate,
        LocalDate endDate
) {
}