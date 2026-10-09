package com.haedong.erp.domains.employee.dto;

import com.haedong.erp.domains.employee.JobType;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 일반 사용자에게 보여주는 직원 목록 한 줄. 생년월일·휴대폰·계정 정보는 담지 않는다 */
@Getter
@NoArgsConstructor
public class EmployeeBriefResponse {

    private Long id;
    private String name;
    private JobType jobType;
    private Integer seniorityNo;
}
