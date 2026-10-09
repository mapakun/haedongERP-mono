package com.haedong.erp.domains.auth;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.common.NameNormalizer;
import com.haedong.erp.domains.auth.dto.LoginRequest;
import com.haedong.erp.domains.auth.dto.MyPasswordChangeRequest;
import com.haedong.erp.domains.employee.Employee;
import com.haedong.erp.domains.employee.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    static final int MAX_FAILED_ATTEMPTS = 5;
    static final int LOCK_MINUTES = 10;

    private final AuthenticationManager authenticationManager;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로그인. 일부러 @Transactional 을 붙이지 않았다.
     * 실패하면 예외를 던지는데, 트랜잭션 안이었다면 "실패 횟수 증가"까지 롤백되어 버리기 때문이다.
     * 트랜잭션 밖의 MyBatis 호출은 각각 바로 커밋된다.
     */
    public Authentication login(LoginRequest request) {
        String loginId = NameNormalizer.normalize(request.loginId());
        Optional<Employee> employee = employeeMapper.findByLoginId(loginId);

        if (employee.isPresent() && employee.get().isLocked()) {
            throw lockedException(employee.get().getLockedUntil());
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(loginId, request.password())
            );
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            employeeMapper.recordLoginSuccess(loginUser.getId());
            return authentication;
        } catch (BadCredentialsException e) {
            // 계정이 있는 사람이 비밀번호를 틀린 경우에만 센다 (없는 아이디는 셀 대상이 없음)
            if (employee.isPresent()) {
                Long id = employee.get().getId();
                employeeMapper.recordLoginFailure(id, MAX_FAILED_ATTEMPTS, LOCK_MINUTES);
                // 이번 실패로 잠겼다면 바로 잠금 안내를 보여준다
                employeeMapper.findById(id)
                        .filter(Employee::isLocked)
                        .ifPresent(locked -> {
                            throw lockedException(locked.getLockedUntil());
                        });
            }
            throw e;
        }
    }

    /** 본인 비밀번호 변경. 바뀐 정보로 다시 만든 로그인 정보를 돌려준다 (이 세션은 계속 유지하기 위해) */
    @Transactional
    public Authentication changeMyPassword(Long employeeId, MyPasswordChangeRequest request) {
        Employee employee = employeeMapper.findById(employeeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.currentPassword(), employee.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다.");
        }
        if (passwordEncoder.matches(request.newPassword(), employee.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "현재 비밀번호와 다른 비밀번호를 입력해 주세요.");
        }
        PasswordPolicy.validate(request.newPassword(), employee.getLoginId());

        employeeMapper.updatePassword(employeeId, passwordEncoder.encode(request.newPassword()));

        // auth_changed_at 이 바뀌었으므로, 새 값으로 로그인 정보를 다시 만든다
        Employee updated = employeeMapper.findById(employeeId).orElseThrow();
        LoginUser loginUser = new LoginUser(updated);
        loginUser.eraseCredentials();
        return UsernamePasswordAuthenticationToken.authenticated(loginUser, null, loginUser.getAuthorities());
    }

    private BusinessException lockedException(OffsetDateTime lockedUntil) {
        long seconds = Duration.between(OffsetDateTime.now(), lockedUntil).getSeconds();
        long minutes = Math.max(1, (seconds + 59) / 60);
        return new BusinessException(HttpStatus.LOCKED,
                "로그인에 " + MAX_FAILED_ATTEMPTS + "회 실패하여 잠겼습니다. " + minutes + "분 후 다시 시도해 주세요.");
    }
}
