package com.haedong.erp.domains.dispatch;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DispatchMapper {

    /** routeNo 가 null 이면 전체 노선 (노선 번호 순) */
    List<DispatchRoute> findRoutes(@Param("routeNo") String routeNo);

    /** 슬롯이 배정된 차량. routeNo 가 null 이면 전체 노선 */
    List<DispatchSlot> findSlots(@Param("routeNo") String routeNo);

    /** 회전 값 이력 (적용 시작일 순). routeNo 가 null 이면 전체 노선 */
    List<RouteRotation> findRotations(@Param("routeNo") String routeNo);
}