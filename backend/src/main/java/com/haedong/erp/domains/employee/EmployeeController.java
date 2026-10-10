package com.haedong.erp.domains.employee;

import com.haedong.erp.common.PageResponse;
import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.employee.dto.EmployeeDetailResponse;
import com.haedong.erp.domains.employee.dto.EmployeeRetireRequest;
import com.haedong.erp.domains.employee.dto.EmployeeSaveRequest;
import com.haedong.erp.domains.employee.dto.EmployeeSearchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final RoleHierarchy roleHierarchy;

    /** 관리자 이상(ADMIN·MASTER)은 전체 정보, 일반 사용자는 이름·직종·서열만 받는다 */
    @GetMapping
    public PageResponse<?> search(@ModelAttribute EmployeeSearchRequest request, Authentication authentication) {
        boolean admin = roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities()).stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return admin ? employeeService.search(request) : employeeService.searchBrief(request);
    }

    @GetMapping("/{id}")
    public EmployeeDetailResponse get(@PathVariable Long id) {
        return employeeService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@Valid @RequestBody EmployeeSaveRequest request) {
        return Map.of("id", employeeService.create(request));
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id,
                       @Valid @RequestBody EmployeeSaveRequest request,
                       @AuthenticationPrincipal LoginUser loginUser) {
        employeeService.update(id, request, loginUser);
    }

    @PostMapping("/{id}/retire")
    public void retire(@PathVariable Long id,
                       @Valid @RequestBody EmployeeRetireRequest request,
                       @AuthenticationPrincipal LoginUser loginUser) {
        employeeService.retire(id, request, loginUser);
    }

    @PostMapping("/{id}/cancel-retirement")
    public void cancelRetirement(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        employeeService.cancelRetirement(id, loginUser);
    }
}