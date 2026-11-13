package com.haedong.erp.domains.comfortleave.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 위로휴가 사용 기록 한 건 */
@Getter
@NoArgsConstructor
public class ComfortUseRow {

    private Long id;
    private Long employeeId;
    private LocalDate usedOn;
    private String memo;
    /** 기록한 관리자 이름 (엑셀 이관분은 "엑셀 이관") */
    private String createdBy;
}