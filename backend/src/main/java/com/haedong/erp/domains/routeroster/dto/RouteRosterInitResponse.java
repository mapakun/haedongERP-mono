package com.haedong.erp.domains.routeroster.dto;

import java.util.List;

/**
 * 전달 복사 / 차량 순번으로 만들기 결과.
 * clearedNames: 복사하면서 빈자리로 바꾼 기사 (퇴사·서열 없음, 또는 이번 달 다른 노선에 이미 배치됨)
 */
public record RouteRosterInitResponse(int count, List<String> clearedNames) {
}
