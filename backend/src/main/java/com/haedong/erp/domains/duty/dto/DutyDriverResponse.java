package com.haedong.erp.domains.duty.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 전속기사 한 명과 가장 최근 시작점 (시작점이 없으면 latest 값은 null = 근무표 미설정) */
@Getter
@NoArgsConstructor
public class DutyDriverResponse {

    private String name;
    private Integer seniorityNo;
    private LocalDate latestBaseDate;
    private Integer latestCycleDay;
}