package com.haedong.erp.domains.vehicle.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/*
차량 목록 outDTO
*/
@Getter
@NoArgsConstructor
public class VehicleResponse {

    private String vehicleNo;
    /** 소속 노선 (null 이면 예비 차량) */
    private String routeNo;
    /** 노선 안 슬롯 번호 (아직 배정 전이면 null) */
    private Integer slotNo;
}