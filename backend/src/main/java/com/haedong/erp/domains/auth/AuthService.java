package com.haedong.erp.domains.auth;

import com.haedong.erp.domains.auth.dto.LoginRequest;
import com.haedong.erp.domains.employee.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeMapper employeeMapper;

    @Transactional
    /*
    * 인증처리기
    * MAPA
    * */
    public Authentication login(LoginRequest request) {
        /* 로그인 검증 */
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.loginId(), request.password())
        );

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        employeeMapper.updateLastLoginAt(loginUser.getId());

        return authentication;
    }
}