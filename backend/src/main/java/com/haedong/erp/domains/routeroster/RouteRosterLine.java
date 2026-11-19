package com.haedong.erp.domains.routeroster;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 노선 근무표의 한 줄 (= 기사 한 명 자리). hd_route_roster 한 줄에서 월·노선을 뺀 값.
 * employeeName 이 null 이면 빈자리
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteRosterLine {

    private int sortOrder;
    private Integer slotNo;
    private String vehicleNo;
    private String abLabel;
    private String employeeName;
}
