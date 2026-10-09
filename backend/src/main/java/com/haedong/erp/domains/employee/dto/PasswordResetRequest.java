package com.haedong.erp.domains.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 관리자가 다른 직원의 비밀번호를 새로 정할 때 */
public record PasswordResetRequest(
        @NotBlank(message = "새 비밀번호를 입력해 주세요.")
        @Size(min = 4, max = 64, message = "비밀번호는 8자 이상 64자 이하로 입력해 주세요.")
        String password
) {
}
