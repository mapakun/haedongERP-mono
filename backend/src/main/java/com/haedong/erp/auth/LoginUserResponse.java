package com.haedong.erp.auth;

import com.haedong.erp.employee.Role;

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