package com.haedong.erp.domains.duty.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** 시작점 일괄 입력: 기준일 하나 + 기사별 주기 일차 */
public record DutyStartBulkRequest(
        @NotNull(message = "기준일을 입력해 주세요.")
        LocalDate baseDate,

        @NotEmpty(message = "입력할 기사가 없습니다.")
        List<@Valid Item> items
) {

    public record Item(
            @NotBlank(message = "기사 이름이 비어 있습니다.")
            String employeeName,

            @NotNull(message = "주기 일차를 입력해 주세요.")
            @Min(value = 1, message = "주기 일차는 1~12 사이로 입력해 주세요.")
            @Max(value = 12, message = "주기 일차는 1~12 사이로 입력해 주세요.")
            Integer cycleDay
    ) {
    }
}