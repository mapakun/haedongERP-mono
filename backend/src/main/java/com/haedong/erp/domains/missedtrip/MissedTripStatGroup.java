package com.haedong.erp.domains.missedtrip;

/** 통계를 무엇을 기준으로 묶을지 */
public enum MissedTripStatGroup {
    REASON,   // 사유별
    ROUTE,    // 노선별
    MONTH,    // 월별
    VEHICLE,  // 차량별 (상위)
    DRIVER,   // 승무원별 (상위)
    KEYWORD   // 비고에 들어간 단어별
}