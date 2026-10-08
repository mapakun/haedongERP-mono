package com.haedong.erp.domains.employee;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EmployeeMapper {

    Optional<Employee> findByLoginId(String loginId);

    void updateLastLoginAt(Long id);

    List<EmployeeSummaryResponse> search(EmployeeSearchRequest request);

    long count(EmployeeSearchRequest request);

    Optional<Employee> findById(Long id);

    Optional<EmployeeDetailResponse> findDetailById(Long id);

    boolean existsByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    boolean existsBySeniorityNo(@Param("seniorityNo") Integer seniorityNo,
                                @Param("excludeName") String excludeName);

    void insert(Employee employee);

    void update(Employee employee);

    void upsertDriver(@Param("employeeName") String employeeName,
                      @Param("seniorityNo") Integer seniorityNo);

    void renameDriver(@Param("oldName") String oldName, @Param("newName") String newName);

    void deleteDriver(String employeeName);
}
