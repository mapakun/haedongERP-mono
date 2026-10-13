package com.haedong.erp.domains.duty.dto;

import java.util.List;

/** 월 근무표: rows 는 서열 순, 각 row 의 cells 는 1일~말일 순서 */
public record DutyRosterResponse(int year, int month, int lastDay, List<DutyRosterRow> rows) {
}
