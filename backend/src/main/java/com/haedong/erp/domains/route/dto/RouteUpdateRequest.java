package com.haedong.erp.domains.route;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/*
노선 슬롯 변경 요청 inDTO
*/
@Getter
@Setter
public class RouteUpdateRequest {
    @NotNull (message = "총 슬롯 수를 입력해 주세요.")
    @Min(value = 1, message = "총 슬롯 수는 1 이상이어야 합니다.")
    @Max(value = 100, message = "총 슬롯 수는 100 이하로 입력해 주세요.")
    private Integer totalSlots;
}
