package com.haedong.erp.domains.comfortleave.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 촉탁직 계약 한 건 (시작일·만료일 모두 포함) */
@Getter
@NoArgsConstructor
public class ComfortContractRow {

    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
}