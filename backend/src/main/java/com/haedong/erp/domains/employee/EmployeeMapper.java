package com.haedong.erp.domains.employee;

import com.haedong.erp.domains.employee.dto.EmployeeBriefResponse;
import com.haedong.erp.domains.employee.dto.EmployeeDetailResponse;
import com.haedong.erp.domains.employee.dto.EmployeeSearchRequest;
import com.haedong.erp.domains.employee.dto.EmployeeSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface EmployeeMapper {

    // 로그인
    Optional<Employee> findByLoginId(String loginId);

    void recordLoginFailure(@Param("id") Long id,
                            @Param("maxAttempts") int maxAttempts,
                            @Param("lockMinutes") int lockMinutes);

    void recordLoginSuccess(Long id);

    OffsetDateTime findAuthChangedAt(Long id);

    // 목록
    List<EmployeeSummaryResponse> search(EmployeeSearchRequest request);

    long count(EmployeeSearchRequest request);

    List<EmployeeBriefResponse> searchBrief(EmployeeSearchRequest request);

    long countBrief(EmployeeSearchRequest request);

    // 상세
    Optional<Employee> findById(Long id);

    Optional<EmployeeDetailResponse> findDetailById(Long id);

    // 중복 검사
    boolean existsByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    boolean existsBySeniorityNo(@Param("seniorityNo") Integer seniorityNo,
                                @Param("excludeName") String excludeName);

    // 직원 저장
    void insert(Employee employee);

    void update(Employee employee);

    // 기사 정보
    void upsertDriver(@Param("employeeName") String employeeName,
                      @Param("seniorityNo") Integer seniorityNo);

    void renameDriver(@Param("oldName") String oldName, @Param("newName") String newName);

    void deleteDriver(String employeeName);

    // 퇴사
    void updateRetirement(Employee employee);

    void clearSeniority(String employeeName);

    // 계정 관리
    void issueAccount(@Param("id") Long id,
                      @Param("loginId") String loginId,
                      @Param("passwordHash") String passwordHash,
                      @Param("role") Role role);

    void revokeAccount(Long id);

    void updateRole(@Param("id") Long id, @Param("role") Role role);

    void updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    void unlock(Long id);
}
