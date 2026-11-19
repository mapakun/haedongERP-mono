package com.haedong.erp.domains.routeroster.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** 같은 달 다른 노선에 이미 배치된 기사 (기사 고를 때 참고용) */
@Getter
@NoArgsConstructor
public class RouteRosterAssignment {

    private String employeeName;
    private String routeNo;
}
