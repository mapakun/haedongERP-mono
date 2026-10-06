package com.haedong.erp.employee;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class Employee {

    private Long id;
    private String name;
    private JobType jobType;
    private String loginId;
    private String password;
    private Role role;
    private boolean enabled;
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
