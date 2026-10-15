package com.haedong.erp.domains.vehicle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/*
차량 등록 요청 inDTO
*/
@Getter
@Setter
public class VehicleCreateRequest {

    @NotBlank(message = "차량 번호를 입력해 주세요.")
    @Pattern(regexp = "^[0-9]{4}$", message = "차량 번호는 숫자 4자리로 입력해 주세요.")
    private String vehicleNo;

    /** 비우면 예비 차량으로 등록 */
    private String routeNo;

    @Min(value = 1, message = "슬롯 번호는 1 이상이어야 합니다.")
    private Integer slotNo;
}