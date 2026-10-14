package com.haedong.erp.domains.duty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 기본 근무표 시작점: "기준일 = 주기 몇 일차". hd_duty_start 한 줄 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DutyStart {
    private Long id;
    private String employeeName;
    private LocalDate baseDate;
    private int cycleDay;
}
