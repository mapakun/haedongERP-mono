package com.haedong.erp.domains.employee;

import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.employee.dto.AccountIssueRequest;
import com.haedong.erp.domains.employee.dto.AccountRoleChangeRequest;
import com.haedong.erp.domains.employee.dto.PasswordResetRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** 직원 로그인 계정 관리 API (관리자 전용: SecurityConfig 에서 POST·PUT·DELETE 를 관리자로 제한) */
@RestController
@RequestMapping("/api/employees/{id}/account")
@RequiredArgsConstructor
public class EmployeeAccountController {

    private final EmployeeAccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void issue(@PathVariable Long id, @Valid @RequestBody AccountIssueRequest request) {
        accountService.issue(id, request.password(), request.role());
    }

    @DeleteMapping
    public void revoke(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        accountService.revoke(id, loginUser.getId());
    }

    @PutMapping("/role")
    public void changeRole(@PathVariable Long id,
                           @Valid @RequestBody AccountRoleChangeRequest request,
                           @AuthenticationPrincipal LoginUser loginUser) {
        accountService.changeRole(id, request.role(), loginUser.getId());
    }

    @PutMapping("/password")
    public void resetPassword(@PathVariable Long id,
                              @Valid @RequestBody PasswordResetRequest request,
                              @AuthenticationPrincipal LoginUser loginUser) {
        accountService.resetPassword(id, request.password(), loginUser.getId());
    }

    @PostMapping("/unlock")
    public void unlock(@PathVariable Long id) {
        accountService.unlock(id);
    }
}
