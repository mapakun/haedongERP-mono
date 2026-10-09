package com.haedong.erp.domains.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 본인이 자기 비밀번호를 바꿀 때. 현재 비밀번호를 한 번 더 확인한다 */
public record MyPasswordChangeRequest(
        @NotBlank(message = "현재 비밀번호를 입력해 주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해 주세요.")
        @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하로 입력해 주세요.")
        String newPassword
) {
}
