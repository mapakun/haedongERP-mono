package com.haedong.erp.domains.specialduty.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** 노선별 심야 투입 기준: 마지막 도착 후 이 시간(분)이 지나야 심야 출발이 가능하다 */
@Getter
@NoArgsConstructor
public class NightRuleRow {

    private String routeNo;
    private int minRestMin;
}