package com.haedong.erp.domains.comfortleave.dto;

import com.haedong.erp.domains.comfortleave.EmploymentType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 위로휴가 대상자 한 명 (직원 관리의 이름·퇴사일을 함께 담는다) */
@Getter
@NoArgsConstructor
public class ComfortMemberRow {

    private Long employeeId;
    private String name;
    private EmploymentType employmentType;
    private LocalDate hireDate;
    /** 직원 관리의 퇴사일 (재직 중이면 null) */
    private LocalDate retiredAt;
    private String note;
}