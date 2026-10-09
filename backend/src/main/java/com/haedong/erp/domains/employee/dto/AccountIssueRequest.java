package com.haedong.erp.domains.employee.dto;

import com.haedong.erp.domains.employee.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 계정 발급: 로그인 아이디는 이름과 같게 자동으로 정해지므로 비밀번호와 권한만 받는다 */
public record AccountIssueRequest(
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 4, max = 64, message = "비밀번호는 4자 이상 64자 이하로 입력해 주세요.")
        String password,

        @NotNull(message = "권한을 선택해 주세요.")
        Role role
) {
}
