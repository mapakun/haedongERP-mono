package com.haedong.erp.domains.specialduty.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** 단축 순번 화면의 한 칸. 켜짐 여부와, 그 순번이 시간표에 있는지를 참고로 담는다 */
@Getter
@NoArgsConstructor
public class ShiftSlotRow {

    private int slotNo;
    /** 단축 가능으로 켜져 있는지 */
    private boolean enabled;
    private boolean inWeekday;
    /** 토요일 시간표에 있는지 (토요일 시간표가 없는 노선은 공휴일 시간표 기준) */
    private boolean inSaturday;
    /** 일요일·공휴일 시간표에 있는지 */
    private boolean inHoliday;
    /** 평일 시간표에서 이 순번의 오전 회차 수 (단축 순번은 보통 1) */
    private int weekdayAmTrips;
}