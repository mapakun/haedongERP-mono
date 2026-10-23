package com.haedong.erp.domains.specialduty.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** 저장되어 있는 "이 순번은 이 심야를 뛸 수 있다" 한 건 */
@Getter
@NoArgsConstructor
public class NightPick {

    private int slotNo;
    private int nightNo;
}