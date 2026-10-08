package com.haedong.erp.domains.employee.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record EmployeeRetireRequest(
        @NotNull(message = "퇴사일을 입력해 주세요.")
        @PastOrPresent(message = "퇴사일은 오늘 또는 이전 날짜여야 합니다.")
        LocalDate retiredAt
) {
}