package com.haedong.erp.domains.duty;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 수동 수정 한 칸: 그 날짜만 패턴 대신 이 근무를 쓴다. hd_duty_override 한 줄 */
@Getter
@NoArgsConstructor
public class DutyOverride {

    private String employeeName;
    private LocalDate workDate;
    private Shift shift;
}