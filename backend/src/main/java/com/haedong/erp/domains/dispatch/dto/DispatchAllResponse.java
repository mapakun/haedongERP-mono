package com.haedong.erp.domains.dispatch.dto;

import java.util.List;

/** 전체 노선 통합 순위표 (노선 번호 순) */
public record DispatchAllResponse(int year, int month, int lastDay, List<DispatchTableResponse> routes) {
}