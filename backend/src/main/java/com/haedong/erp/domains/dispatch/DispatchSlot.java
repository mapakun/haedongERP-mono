package com.haedong.erp.domains.dispatch;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** 노선 안 슬롯에 배정된 차량 (슬롯이 비어 있는 차량은 조회하지 않는다) */
@Getter
@NoArgsConstructor
public class DispatchSlot {

    private String routeNo;
    private int slotNo;
    private String vehicleNo;
}