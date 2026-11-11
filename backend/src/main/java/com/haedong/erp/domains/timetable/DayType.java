package com.haedong.erp.domains.timetable;

/** 시간표 형태: 그날이 어떤 날이냐에 따라 쓰는 시간표가 다르다 */
public enum DayType {
    WEEKDAY,   // 평일
    HOLIDAY,   // 공휴일(법정 공휴일 + 일요일)
    SATURDAY,  // 토요일
    VACATION   // 방학
}