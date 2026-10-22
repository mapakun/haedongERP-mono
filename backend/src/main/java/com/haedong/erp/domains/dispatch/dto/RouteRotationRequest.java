package com.haedong.erp.domains.dispatch.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** 회전 값 등록: 적용 시작일부터 새 값을 쓴다 */
public record RouteRotationRequest(
        @NotNull(message = "적용 시작일을 입력해 주세요.")
        LocalDate effectiveFrom,

        @NotNull(message = "일회전을 입력해 주세요.")
        @Min(value = 0, message = "일회전은 0~100 사이로 입력해 주세요.")
        @Max(value = 100, message = "일회전은 0~100 사이로 입력해 주세요.")
        Integer dailyRotation,

        @NotNull(message = "월회전을 입력해 주세요.")
        @Min(value = 0, message = "월회전은 0~100 사이로 입력해 주세요.")
        @Max(value = 100, message = "월회전은 0~100 사이로 입력해 주세요.")
        Integer monthlyRotation
) {
}