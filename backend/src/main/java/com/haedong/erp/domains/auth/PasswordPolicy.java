package com.haedong.erp.domains.auth;

import com.haedong.erp.common.BusinessException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;

/**
 * 비밀번호 규칙. 길이(8자 이상)는 요청 DTO 의 @Size 로 먼저 검사하고, 여기서는 그 밖의 규칙을 검사한다.
 */
public final class PasswordPolicy {

    /** BCrypt 는 72바이트까지만 사용한다. 그 이상이면 뒷부분이 무시되거나 오류가 난다. */
    private static final int MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    public static void validate(String password, String loginId) {
        if (password.equals(loginId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "아이디와 같은 비밀번호는 사용할 수 없습니다.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "비밀번호가 너무 깁니다.");
        }
    }
}
