package com.haedong.erp.domains.route.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
/*
노선 등록요청 inDTO
 */
public class RouteCreateRequest {
    @NotBlank(message = "노선 번호를 입력해 주세요.")
    @Size(max = 20, message = "노선 번호는 20자 이내로 입력해 주세요.")
    @Pattern (regexp = "^[0-9A-Za-z-]*$", message = "노선 번호는 숫자, 영문, 하이픈(-)만 사용할 수 있습니다.")
    private String routeNo;

    @NotNull (message = "총 슬롯 수를 입력해 주세요.")
    @Min(value = 1, message = "총 슬롯 수는 1 이상이어야 합니다.")
    @Max(value = 100, message = "총 슬롯 수는 100 이하로 입력해 주세요.")
    private Integer totalSlots;
}
