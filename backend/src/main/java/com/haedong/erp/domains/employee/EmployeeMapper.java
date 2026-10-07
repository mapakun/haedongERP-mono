package com.haedong.erp.domains.employee;

import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface EmployeeMapper {

    Optional<Employee> findByLoginId(String loginId);

    void updateLastLoginAt(Long id);
}
