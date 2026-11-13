package com.haedong.erp.domains.comfortleave.dto;

import com.haedong.erp.domains.comfortleave.EmploymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MemberSaveRequest(
        @NotNull(message = "고용 형태를 선택해 주세요.") EmploymentType employmentType,
        @NotNull(message = "입사일자를 입력해 주세요.") LocalDate hireDate,
        @Size(max = 100, message = "메모는 100자 이내로 입력해 주세요.") String note
) {
}