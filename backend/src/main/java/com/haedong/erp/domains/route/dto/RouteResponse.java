package com.haedong.erp.domains.route;

import lombok.Getter;
import lombok.NoArgsConstructor;

/*
노선 목록 outDTO
*/
@Getter 
@NoArgsConstructor 
public class RouteResponse {

    private String routeNo;
    private int totalSlots;
    /** 이 노선에 배정된 차량 수 (슬롯 번호가 아직 비어 있는 차량도 포함) */
    private int assignedCount;
}