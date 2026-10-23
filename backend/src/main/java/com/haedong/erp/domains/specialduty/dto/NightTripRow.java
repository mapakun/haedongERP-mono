package com.haedong.erp.domains.specialduty.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** 심야 시간표 한 줄. 출발 시각은 0시부터의 분이다 */
@Getter
@NoArgsConstructor
public class NightTripRow {

    private int nightNo;
    private int departMin;
}