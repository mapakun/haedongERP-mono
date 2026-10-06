package com.haedong.erp.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @PostMapping("/login")
    public LoginUserResponse login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse) {
        Authentication authentication = authService.login(request);

        // 세션 고정 공격 방지: 로그인 전 세션이 있었다면 세션 ID를 새로 바꾼다
        if (httpRequest.getSession(false) != null) {
            httpRequest.changeSessionId();
        }

        // 로그인 정보를 세션에 저장 → 다음 요청부터 JSESSIONID로 로그인 상태 유지
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return LoginUserResponse.from((LoginUser) authentication.getPrincipal());
    }

    @GetMapping("/me")
    public LoginUserResponse me(@AuthenticationPrincipal LoginUser loginUser) {
        return LoginUserResponse.from(loginUser);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleAuthenticationException(AuthenticationException e) {
        return Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다.");
    }
}