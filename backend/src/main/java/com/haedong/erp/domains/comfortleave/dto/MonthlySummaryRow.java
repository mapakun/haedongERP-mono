package com.haedong.erp.domains.comfortleave.dto;

/**
 * 정규직의 그달 말일 기준 누계. first 는 한 번 이상 쓴 사람 수, second 는 두 번 이상 쓴 사람 수 ...
 * total 은 사용 건수 합계 (= first + second + third + fourth)
 */
public record MonthlySummaryRow(int month, int first, int second, int third, int fourth, int total) {
}