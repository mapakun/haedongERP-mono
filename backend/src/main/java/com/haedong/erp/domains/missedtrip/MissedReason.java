package com.haedong.erp.domains.missedtrip;

/** 결행 사유 (화면에는 한글 이름으로 보여 준다) */
public enum MissedReason {
    TRAFFIC,    // 체증
    BREAKDOWN,  // 고장
    ACCIDENT,   // 사고
    SICK,       // 병가
    ETC         // 기타
}