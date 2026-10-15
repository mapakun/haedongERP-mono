package com.haedong.erp.domains.vehicle.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/*
차량 노선·슬롯 배정 변경 요청 inDTO
routeNo 를 비우면 예비 차량이 된다
*/
@Getter
@Setter
public class VehicleAssignRequest {

    private String routeNo;

    @Min(value = 1, message = "슬롯 번호는 1 이상이어야 합니다.")
    private Integer slotNo;
}