package com.haedong.erp.domains.missedtrip.dto;

import java.util.List;

/** rows 는 상위 N개만일 수 있어서, 조건 전체의 건수·합계를 따로 담는다 */
public record MissedTripStatResponse(List<MissedTripStatRow> rows, long totalCount, long missedSum) {
}