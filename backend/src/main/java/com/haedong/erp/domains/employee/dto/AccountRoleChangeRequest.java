package com.haedong.erp.domains.employee.dto;

import com.haedong.erp.domains.employee.Role;
import jakarta.validation.constraints.NotNull;

public record AccountRoleChangeRequest(
        @NotNull(message = "권한을 선택해 주세요.")
        Role role
) {
}
