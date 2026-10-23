package com.haedong.erp.domains.specialduty.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** 노선·형태의 심야 가능 순번을 통째로 저장한다. 목록에 없거나 심야 순번이 비어 있는 순번은 "불가"가 된다 */
public record NightSlotSaveRequest(
        @NotNull(message = "순번 목록이 필요합니다.") @Valid List<Item> slots
) {

    public record Item(
            @NotNull(message = "순번이 필요합니다.") @Min(value = 1, message = "순번은 1 이상이어야 합니다.") Integer slotNo,
            @NotNull(message = "심야 순번 목록이 필요합니다.")
            List<@NotNull(message = "심야 순번이 비어 있습니다.") Integer> nightNos
    ) {
    }
}