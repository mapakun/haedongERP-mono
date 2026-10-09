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
    private LocalDate birthDate;
    private String mobile;
    private LocalDate retiredAt;
    private String loginId;
    private String password;
    private Role role;
    private boolean enabled;
    private int failedLoginCount;
    private OffsetDateTime lockedUntil;
    /** 지금 잠겨 있는지. 시각 비교는 DB 의 now() 기준으로 SQL 에서 계산해 받는다 */
    private boolean locked;
    /** 계정·권한·비밀번호가 바뀐 시각. 로그인 세션이 아직 유효한지 판단하는 기준 */
    private OffsetDateTime authChangedAt;
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

    public boolean isRetired() {
        return retiredAt != null;
    }

    public boolean hasAccount() {
        return loginId != null;
    }

    public void retire(LocalDate retiredAt) {
        this.retiredAt = retiredAt;
        this.enabled = false;
    }

    public void cancelRetirement() {
        this.retiredAt = null;
        this.enabled = true;
    }

    private void apply(EmployeeSaveRequest request) {
        this.name = request.getName();
        this.jobType = request.getJobType();
        this.birthDate = request.getBirthDate();
        this.mobile = request.getMobile();
    }
}