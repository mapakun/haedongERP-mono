package com.haedong.erp.domains.comfortleave.dto;

import com.haedong.erp.domains.comfortleave.EmploymentType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 운전직 한 명과 위로휴가 대상자 정보 (아직 등록 안 됐으면 고용 형태·입사일자가 null) */
@Getter
@NoArgsConstructor
public class DriverMemberRow {

    private Long employeeId;
    private String name;
    private LocalDate retiredAt;
    private EmploymentType employmentType;
    private LocalDate hireDate;
    private String note;
}