package com.haedong.erp.domains.duty.dto;

import com.haedong.erp.domains.duty.Shift;

/** 월 근무표의 한 칸. shift 가 null 이면 근무표 미설정, overridden 이면 수동 수정한 칸 */
public record DutyCell(Shift shift, boolean overridden) {
}
