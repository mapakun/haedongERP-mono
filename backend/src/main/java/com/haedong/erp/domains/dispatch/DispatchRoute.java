package com.haedong.erp.domains.dispatch;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 순위표 계산에 필요한 노선 정보 */
@Getter
@NoArgsConstructor
public class DispatchRoute {

    private String routeNo;
    /** 순번 수 */
    private int totalSlots;
    /** 이 날의 순번 = 슬롯 번호. null 이면 순위표 미설정 */
    private LocalDate baseDate;
}