package com.haedong.erp.domains.dispatch;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DispatchMapper {

    /** routeNo 가 null 이면 전체 노선 (노선 번호 순) */
    List<DispatchRoute> findRoutes(@Param("routeNo") String routeNo);

    /** 슬롯이 배정된 차량. routeNo 가 null 이면 전체 노선 */
    List<DispatchSlot> findSlots(@Param("routeNo") String routeNo);

    /** 회전 값 이력 (적용 시작일 순). routeNo 가 null 이면 전체 노선 */
    List<RouteRotation> findRotations(@Param("routeNo") String routeNo);

    /** 노선 행을 잠그고 순번 수·기준일을 읽는다. 노선이 없으면 빈 값 */
    Optional<DispatchRoute> lockRoute(String routeNo);

    boolean existsRotation(@Param("routeNo") String routeNo,
                           @Param("effectiveFrom") LocalDate effectiveFrom);

    void insertRotation(@Param("routeNo") String routeNo,
                        @Param("effectiveFrom") LocalDate effectiveFrom,
                        @Param("dailyRotation") int dailyRotation,
                        @Param("monthlyRotation") int monthlyRotation,
                        @Param("createdBy") String createdBy);
}