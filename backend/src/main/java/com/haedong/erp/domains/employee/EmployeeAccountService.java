package com.haedong.erp.domains.employee;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.auth.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자가 직원의 로그인 계정을 관리한다. (발급, 회수, 권한 변경, 비밀번호 재설정, 잠금 해제)
 * - 본인 계정은 여기서 바꿀 수 없다 (관리자가 아무도 없게 되는 사고 방지)
 * - 최고 관리자(MASTER)의 계정은 최고 관리자만 바꿀 수 있다
 * - MASTER 권한을 주거나 빼는 것은 화면·API 로 할 수 없다 (DB 에서만)
 */
@Service
@RequiredArgsConstructor
public class EmployeeAccountService {

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void issue(Long id, String password, Role role) {
        Employee employee = findEmployee(id);
        rejectMasterRole(role);

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
    public void revoke(Long id, LoginUser currentUser) {
        Employee employee = findAccountOwner(id);
        rejectSelf(employee, currentUser, "본인 계정은 회수할 수 없습니다.");
        rejectIfProtectedMaster(employee, currentUser);

        employeeMapper.revokeAccount(id);
    }

    @Transactional
    public void changeRole(Long id, Role role, LoginUser currentUser) {
        Employee employee = findAccountOwner(id);
        rejectSelf(employee, currentUser, "본인 권한은 변경할 수 없습니다.");
        rejectMasterRole(role);
        if (employee.getRole() == Role.MASTER) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "최고 관리자의 권한은 화면에서 변경할 수 없습니다.");
        }

        employeeMapper.updateRole(id, role);
    }

    @Transactional
    public void resetPassword(Long id, String password, LoginUser currentUser) {
        Employee employee = findAccountOwner(id);
        rejectSelf(employee, currentUser, "본인 비밀번호는 [내 비밀번호 변경]에서 바꿔 주세요.");
        rejectIfProtectedMaster(employee, currentUser);
        PasswordPolicy.validate(password, employee.getLoginId());

        employeeMapper.updatePassword(id, passwordEncoder.encode(password));
    }

    @Transactional
    public void unlock(Long id, LoginUser currentUser) {
        Employee employee = findAccountOwner(id);
        rejectIfProtectedMaster(employee, currentUser);

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

    private void rejectSelf(Employee employee, LoginUser currentUser, String message) {
        if (employee.getId().equals(currentUser.getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
    }

    /** MASTER 권한은 화면·API 로 줄 수 없다 (DB 에서만 지정) */
    private void rejectMasterRole(Role role) {
        if (role == Role.MASTER) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "최고 관리자 권한은 화면에서 부여할 수 없습니다.");
        }
    }

    /** 최고 관리자의 계정은 최고 관리자만 바꿀 수 있다 */
    private void rejectIfProtectedMaster(Employee target, LoginUser currentUser) {
        if (target.getRole() == Role.MASTER && !currentUser.isMaster()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "최고 관리자의 계정은 최고 관리자만 변경할 수 있습니다.");
        }
    }
}