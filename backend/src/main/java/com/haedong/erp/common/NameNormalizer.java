package com.haedong.erp.common;

import java.text.Normalizer;

/**
 * 이름(= 로그인 아이디)을 같은 기준으로 맞춘다.
 * 앞뒤 공백을 지우고, Mac 등에서 자모가 분리되어 들어온 한글을 완성형(NFC)으로 합친다.
 */
public final class NameNormalizer {

    private NameNormalizer() {
    }

    public static String normalize(String value) {
        return value == null ? null : Normalizer.normalize(value.strip(), Normalizer.Form.NFC);
    }
}
