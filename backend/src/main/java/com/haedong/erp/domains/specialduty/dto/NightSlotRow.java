package com.haedong.erp.domains.specialduty.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** 시간표에 있는 순번과 그 순번의 마지막 도착 시각(0시부터의 분) */
@Getter
@NoArgsConstructor
public class NightSlotRow {

    private int slotNo;
    private int lastArriveMin;
}