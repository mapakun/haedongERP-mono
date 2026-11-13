package com.haedong.erp.domains.missedtrip.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** 걸러진 결과 전체의 건수와 결행 횟수 합계 (한 페이지가 아니라 필터에 맞는 전부) */
@Getter
@NoArgsConstructor
public class MissedTripSummary {

    private long totalCount;
    private long missedSum;
}