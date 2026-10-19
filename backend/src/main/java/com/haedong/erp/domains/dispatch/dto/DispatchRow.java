package com.haedong.erp.domains.dispatch.dto;

import java.util.List;

/**
 * 순위표 한 줄
 *
 * @param order          순번
 * @param days           1일 ~ 말일의 차량 번호 (빈 순번이거나 기준일 이전이면 null)
 * @param nextMonthFirst 다음 달 1일의 차량 번호 (미리보기)
 */
public record DispatchRow(int order, List<String> days, String nextMonthFirst) {
}