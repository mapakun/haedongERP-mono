package com.haedong.erp.domains.employee.dto;

import com.haedong.erp.domains.employee.JobType;
import com.haedong.erp.domains.employee.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class EmployeeSummaryResponse {

    private Long id;
    private String name;
    private JobType jobType;
    private LocalDate birthDate;
    private String mobile;
    private Integer seniorityNo;
    private Role role;
    private boolean enabled;
    private boolean hasAccount;
}