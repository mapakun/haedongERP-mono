package com.haedong.erp;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/*
* 해시 생성용 테스트 클래스
* 비밀번호 새ㅑㅇ성기
* */

public class PasswordHashGenerator {
    @Test
    void generate() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        System.out.println(encoder.encode("1234"));
    }
}
