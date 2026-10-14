package com.haedong.erp.domains.duty;

import com.haedong.erp.domains.duty.dto.DutyDriverResponse;
import com.haedong.erp.domains.duty.dto.DutyStartBulkRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DutyMapper {

    // 전속기사 (서열 순) + 가장 최근 시작점
    List<DutyDriverResponse> findDrivers();

    // 시작점: toDate 이전의 모든 이력
    List<DutyStart> findStartsUntil(@Param("toDate") LocalDate toDate);

    void insertStarts(@Param("baseDate") LocalDate baseDate,
                      @Param("items") List<DutyStartBulkRequest.Item> items,
                      @Param("createdBy") Long createdBy);

    // 수동 수정: 기간 안의 칸
    List<DutyOverride> findOverrides(@Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate);

    // 직원 이름이 바뀌면 근무표 데이터도 따라간다
    void renameStarts(@Param("oldName") String oldName, @Param("newName") String newName);

    void renameOverrides(@Param("oldName") String oldName, @Param("newName") String newName);

    void renameOverrideLogs(@Param("oldName") String oldName, @Param("newName") String newName);

    List<DutyStartHistoryRow> findStartHistory(@Param("employeeName") String employeeName);
}