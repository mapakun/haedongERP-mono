package com.haedong.erp.domains.dispatch.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** 회전 값 이력 한 줄 */
public record RouteRotationResponse(
        LocalDate effectiveFrom,
        int dailyRotation,
        int monthlyRotation,
        /** 등록한 직원 이름 (초기 설정이면 '초기설정') */
        String createdBy,
        OffsetDateTime createdAt
) {
}