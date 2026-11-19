package com.haedong.erp.domains.missedtrip.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** 통계 한 줄: 묶은 기준값(label), 건수, 결행 횟수 합계 */
@Getter
@NoArgsConstructor
public class MissedTripStatRow {
    /** 사유별이면 영문 코드(TRAFFIC 등), 승무원이 없으면 null */
    private String label;
    private long tripCount;
    private long missedSum;
}