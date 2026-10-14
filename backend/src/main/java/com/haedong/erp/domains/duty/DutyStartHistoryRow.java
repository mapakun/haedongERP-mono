package com.haedong.erp.domains.duty;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** 시작점 이력 조회 한 줄 (입력자 이름 포함) */
@Getter
@NoArgsConstructor
public class DutyStartHistoryRow {

    private Long id;
    private LocalDate baseDate;
    private int cycleDay;
    private String createdByName;
    private OffsetDateTime createdAt;

    /** 적용 기간 계산용 */
    public DutyStart toStart(String employeeName) {
        return new DutyStart(id, employeeName, baseDate, cycleDay);
    }
}