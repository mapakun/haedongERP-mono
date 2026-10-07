package com.haedong.erp.domains.employee;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeSearchRequest {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private String keyword;
    private JobType jobType;
    private Integer page;
    private Integer size;

    public int getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public int getSize() {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    public int getOffset() {
        return (getPage() - 1) * getSize();
    }

    public String getKeyword() {
        return keyword == null || keyword.isBlank() ? null : keyword.strip();
    }
}