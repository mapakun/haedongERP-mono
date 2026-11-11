package com.haedong.erp.domains.specialduty.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** 심야 시간표의 출발 시각을 저장한다. 심야 순번의 개수와 번호는 바꾸지 않고 시각만 바꾼다 */
public record NightTripSaveRequest(
        @NotEmpty(message = "심야 시간표가 비어 있습니다.") @Valid List<Item> trips
) {

    public record Item(
            @NotNull(message = "심야 순번이 필요합니다.") Integer nightNo,
            @NotNull(message = "출발 시각을 입력해 주세요.")
            @Min(value = 0, message = "시각이 올바르지 않습니다.")
            @Max(value = 2879, message = "시각이 올바르지 않습니다.") Integer departMin
    ) {
    }
}