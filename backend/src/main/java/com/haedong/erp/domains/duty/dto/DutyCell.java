package com.haedong.erp.domains.duty.dto;

import com.haedong.erp.domains.duty.Shift;

/**
 * 월 근무표의 한 칸.
 * - shift: 근무 (null 이면 근무표 미설정). 수동 수정이 있으면 수정 값
 * - overridden: 수동 수정한 칸
 * - cycleDay: 시작점으로 계산한 그날의 주기 일차 (미설정이면 null)
 * - startChanged: 이날부터 새 시작점이 적용됨 (전날과 적용 시작점이 다름)
 */
public record DutyCell(Shift shift, boolean overridden, Integer cycleDay, boolean startChanged) {
}
