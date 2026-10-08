package com.haedong.erp.domains.employee;

import com.haedong.erp.domains.employee.dto.EmployeeSaveRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class Employee {

    private Long id;
    private String name;
    private JobType jobType;
    private LocalDate birthDate;    // 추가
    private String mobile;          // 추가
    private String loginId;
    private String password;
    private Role role;
    private boolean enabled;
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static Employee create(EmployeeSaveRequest request) {
        Employee employee = new Employee();
        employee.apply(request);
        return employee;
    }

    public void update(EmployeeSaveRequest request) {
        apply(request);
    }

    private void apply(EmployeeSaveRequest request) {
        this.name = request.getName();
        this.jobType = request.getJobType();
        this.birthDate = request.getBirthDate();
        this.mobile = request.getMobile();
    }
}
