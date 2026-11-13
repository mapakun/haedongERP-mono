package com.haedong.erp.domains.comfortleave;

import com.haedong.erp.domains.comfortleave.dto.ComfortContractRow;
import com.haedong.erp.domains.comfortleave.dto.ComfortMemberRow;
import com.haedong.erp.domains.comfortleave.dto.ComfortUseRow;
import com.haedong.erp.domains.comfortleave.dto.DriverMemberRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ComfortLeaveMapper {

    // ===== 현황 조회 =====

    /** 위로휴가 대상자 전체 (운전직만, 이름순) */
    List<ComfortMemberRow> findMembers();

    List<ComfortContractRow> findContracts();

    /** 사용일이 from ~ to (양끝 포함) 인 사용 기록, 사용일 순 */
    List<ComfortUseRow> findUsesBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // ===== 대상자 관리 =====

    /** 운전직 전체와 대상자 정보 (미등록자가 먼저, 그다음 이름순) */
    List<DriverMemberRow> findDriverMembers();

    /** 운전직 직원 행을 잠그고 이름을 돌려준다 (운전직이 아니거나 없으면 비어 있음) */
    Optional<String> lockDriverName(Long employeeId);

    /** 대상자 행을 잠그고 고용 형태를 돌려준다 (대상자가 아니면 비어 있음) */
    Optional<EmploymentType> lockMemberType(Long employeeId);

    void upsertMember(@Param("employeeId") Long employeeId,
                      @Param("employmentType") EmploymentType employmentType,
                      @Param("hireDate") LocalDate hireDate,
                      @Param("note") String note);

    // ===== 사용 기록 =====

    void insertUse(@Param("employeeId") Long employeeId,
                   @Param("usedOn") LocalDate usedOn,
                   @Param("memo") String memo,
                   @Param("createdBy") String createdBy);

    int deleteUse(Long id);

    int countUsesBetween(@Param("employeeId") Long employeeId,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to);

    // ===== 촉탁 계약 =====

    List<ComfortContractRow> findContractsOf(Long employeeId);

    void insertContract(@Param("employeeId") Long employeeId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

    int deleteContract(@Param("employeeId") Long employeeId, @Param("startDate") LocalDate startDate);
}