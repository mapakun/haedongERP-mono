package com.haedong.erp.domains.missedtrip.dto;

import java.util.List;

/**
 * 결행 목록 한 페이지.
 *
 * @param totalCount 필터에 맞는 전체 건수 (페이지 수를 계산하는 데 쓴다)
 * @param missedSum  필터에 맞는 전체 결행 횟수 합계
 */
public record MissedTripPageResponse(
        List<MissedTripRow> items,
        long totalCount,
        long missedSum,
        int page,
        int size
) {
}