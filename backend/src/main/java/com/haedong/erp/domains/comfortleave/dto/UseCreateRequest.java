package com.haedong.erp.domains.comfortleave.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UseCreateRequest(
        @NotNull(message = "직원을 선택해 주세요.") Long employeeId,
        @NotNull(message = "사용일을 입력해 주세요.") LocalDate usedOn,
        @Size(max = 100, message = "메모는 100자 이내로 입력해 주세요.") String memo
) {
}