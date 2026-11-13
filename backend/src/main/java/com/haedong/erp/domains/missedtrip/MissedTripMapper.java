package com.haedong.erp.domains.missedtrip;

import com.haedong.erp.domains.missedtrip.dto.MissedTripRow;
import com.haedong.erp.domains.missedtrip.dto.MissedTripSaveRequest;
import com.haedong.erp.domains.missedtrip.dto.MissedTripSearchRequest;
import com.haedong.erp.domains.missedtrip.dto.MissedTripSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MissedTripMapper {

    /** 조건에 맞는 한 페이지 (최신 일자 순) */
    List<MissedTripRow> search(MissedTripSearchRequest request);

    /** 조건에 맞는 전체 건수와 결행 횟수 합계 */
    MissedTripSummary summarize(MissedTripSearchRequest request);

    /** 내보내기용: 페이지 없이 조건에 맞는 전부 (호출하기 전에 건수 제한을 확인한다) */
    List<MissedTripRow> findForExport(MissedTripSearchRequest request);

    boolean existsRoute(String routeNo);

    void insert(@Param("v") MissedTripSaveRequest request, @Param("createdBy") String createdBy);

    /** 바뀐 행 수를 돌려준다 (0이면 그런 기록이 없다는 뜻) */
    int update(@Param("id") Long id, @Param("v") MissedTripSaveRequest request);

    int delete(Long id);
}