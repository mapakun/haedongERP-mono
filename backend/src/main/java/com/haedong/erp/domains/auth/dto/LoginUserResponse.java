package com.haedong.erp.domains.auth.dto;

import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.employee.Role;

public record LoginUserResponse(
        Long id,
        String loginId,
        String name,
        Role role
) {
    public static LoginUserResponse from(LoginUser loginUser) {
        return new LoginUserResponse(
                loginUser.getId(),
                loginUser.getLoginId(),
                loginUser.getName(),
                loginUser.getRole()
        );
    }
}