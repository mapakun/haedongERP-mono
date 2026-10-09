package com.haedong.erp.domains.auth;

import com.haedong.erp.domains.auth.dto.LoginRequest;
import com.haedong.erp.domains.auth.dto.LoginUserResponse;
import com.haedong.erp.domains.auth.dto.MyPasswordChangeRequest;
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
        saveToSession(authentication, httpRequest, httpResponse);

        return LoginUserResponse.from((LoginUser) authentication.getPrincipal());
    }

    @GetMapping("/me")
    public LoginUserResponse me(@AuthenticationPrincipal LoginUser loginUser) {
        return LoginUserResponse.from(loginUser);
    }

    /** 내 비밀번호 변경. 다른 기기의 세션은 끊기고, 지금 이 세션은 새 정보로 유지된다 */
    @PutMapping("/me/password")
    public void changeMyPassword(@Valid @RequestBody MyPasswordChangeRequest request,
                                 @AuthenticationPrincipal LoginUser loginUser,
                                 HttpServletRequest httpRequest,
                                 HttpServletResponse httpResponse) {
        Authentication authentication = authService.changeMyPassword(loginUser.getId(), request);
        saveToSession(authentication, httpRequest, httpResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleAuthenticationException(AuthenticationException e) {
        return Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    /** 로그인 정보를 세션에 저장 → 다음 요청부터 JSESSIONID 로 로그인 상태 유지 */
    private void saveToSession(Authentication authentication,
                               HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }
}
