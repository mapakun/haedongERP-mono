package com.haedong.erp.domains.routeroster.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 한 노선·한 달의 배치를 통째로 저장한다. 목록 순서가 곧 출력 순서.
 * 빈 목록을 보내면 그달 배치를 비운다.
 */
public record RouteRosterSaveRequest(
        @NotNull(message = "배치 목록이 필요합니다.")
        @Size(max = 100, message = "한 노선에 100줄까지 저장할 수 있습니다.")
        List<@NotNull(message = "빈 줄이 있습니다.") @Valid Item> rows
) {

    public record Item(
            @Min(value = 1, message = "구분 번호는 1 이상이어야 합니다.")
            Integer slotNo,

            @Size(max = 20, message = "차량번호는 20자 이하로 입력해 주세요.")
            String vehicleNo,

            @Pattern(regexp = "[AB]", message = "A/B 는 A 또는 B 만 입력할 수 있습니다.")
            String abLabel,

            // 비우면 빈자리
            String employeeName
    ) {
    }
}
