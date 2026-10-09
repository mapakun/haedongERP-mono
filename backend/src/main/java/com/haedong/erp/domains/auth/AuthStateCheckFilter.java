package com.haedong.erp.domains.auth;

import com.haedong.erp.domains.employee.EmployeeMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

/**
 * 로그인한 뒤에 계정이 회수되거나 권한·비밀번호가 바뀌었다면, 그 세션을 끊는다.
 * 세션에 저장된 auth_changed_at 과 DB 의 현재 값이 다르면 "로그인 이후 무언가 바뀌었다"는 뜻이다.
 * 시각을 서로 비교하지 않고 "같은 값인지"만 보기 때문에, 서버와 DB 의 시계가 조금 달라도 문제없다.
 */
@RequiredArgsConstructor
public class AuthStateCheckFilter extends OncePerRequestFilter {

    private static final String EXPIRED_BODY = "{\"message\":\"계정 정보가 변경되어 다시 로그인해야 합니다.\"}";

    private final EmployeeMapper employeeMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 로그인·로그아웃 요청은 검사하지 않는다 (끊긴 세션이 남아 있어도 다시 로그인할 수 있어야 하므로)
        String uri = request.getRequestURI();
        return uri.equals("/api/auth/login") || uri.equals("/api/auth/logout");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            OffsetDateTime current = employeeMapper.findAuthChangedAt(loginUser.getId());

            if (current == null || !current.isEqual(loginUser.getAuthChangedAt())) {
                SecurityContextHolder.clearContext();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().write(EXPIRED_BODY);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
