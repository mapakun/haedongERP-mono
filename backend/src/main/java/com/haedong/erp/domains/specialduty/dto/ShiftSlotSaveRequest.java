package com.haedong.erp.domains.specialduty.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** 노선의 단축 가능 순번을 통째로 저장한다. 목록에 없는 순번은 꺼진다 */
public record ShiftSlotSaveRequest(
        @NotNull(message = "순번 목록이 필요합니다.")
        List<@NotNull(message = "순번이 비어 있습니다.") @Min(value = 1, message = "순번은 1 이상이어야 합니다.") Integer> slotNos
) {
}