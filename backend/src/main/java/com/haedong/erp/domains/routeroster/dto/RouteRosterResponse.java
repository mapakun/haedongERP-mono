package com.haedong.erp.domains.routeroster.dto;

import java.util.List;

/**
 * 한 노선의 월 근무표. rows 가 비어 있으면 그달 배치를 아직 만들지 않은 것
 * (화면에서 "전달 복사" / "차량 순번으로 만들기"를 보여준다)
 */
public record RouteRosterResponse(
        int year,
        int month,
        int lastDay,
        String routeNo,
        List<RouteRosterRow> rows,
        List<RouteRosterAssignment> otherAssignments
) {
}
