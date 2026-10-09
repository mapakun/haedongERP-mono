package com.haedong.erp.domains.employee.dto;

import com.haedong.erp.domains.employee.JobType;
import com.haedong.erp.domains.employee.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class EmployeeDetailResponse {

    private Long id;
    private String name;
    private JobType jobType;
    private LocalDate birthDate;
    private String mobile;
    private LocalDate retiredAt;
    private Integer seniorityNo;
    private Role role;
    private boolean enabled;
    private boolean hasAccount;
    private boolean locked;
    private OffsetDateTime lockedUntil;
}