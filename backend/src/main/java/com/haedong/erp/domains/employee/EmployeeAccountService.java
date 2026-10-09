package com.haedong.erp.domains.employee;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.domains.auth.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자가 직원의 로그인 계정을 관리한다. (발급, 회수, 권한 변경, 비밀번호 재설정, 잠금 해제)
 * 관리자 본인의 계정은 여기서 바꿀 수 없다. 관리자가 한 명도 없게 되는 사고를 막기 위해서다.
 */
@Service
@RequiredArgsConstructor
public class EmployeeAccountService {

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void issue(Long id, String password, Role role) {
        Employee employee = findEmployee(id);

        if (employee.isRetired()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "퇴사한 직원에게는 계정을 발급할 수 없습니다.");
        }
        if (employee.hasAccount()) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 계정이 발급된 직원입니다.");
        }
        // 로그인 아이디는 항상 이름과 같다
        PasswordPolicy.validate(password, employee.getName());

        employeeMapper.issueAccount(id, employee.getName(), passwordEncoder.encode(password), role);
    }

    @Transactional
    public void revoke(Long id, Long currentUserId) {
        Employee employee = findAccountOwner(id);
        rejectSelf(employee, currentUserId, "본인 계정은 회수할 수 없습니다.");

        employeeMapper.revokeAccount(id);
    }

    @Transactional
    public void changeRole(Long id, Role role, Long currentUserId) {
        Employee employee = findAccountOwner(id);
        rejectSelf(employee, currentUserId, "본인 권한은 변경할 수 없습니다.");

        employeeMapper.updateRole(id, role);
    }

    @Transactional
    public void resetPassword(Long id, String password, Long currentUserId) {
        Employee employee = findAccountOwner(id);
        rejectSelf(employee, currentUserId, "본인 비밀번호는 [내 비밀번호 변경]에서 바꿔 주세요.");
        PasswordPolicy.validate(password, employee.getLoginId());

        employeeMapper.updatePassword(id, passwordEncoder.encode(password));
    }

    @Transactional
    public void unlock(Long id) {
        findAccountOwner(id);
        employeeMapper.unlock(id);
    }

    private Employee findEmployee(Long id) {
        return employeeMapper.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다."));
    }

    private Employee findAccountOwner(Long id) {
        Employee employee = findEmployee(id);
        if (!employee.hasAccount()) {
            throw new BusinessException(HttpStatus.CONFLICT, "계정이 발급되지 않은 직원입니다.");
        }
        return employee;
    }

    private void rejectSelf(Employee employee, Long currentUserId, String message) {
        if (employee.getId().equals(currentUserId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
