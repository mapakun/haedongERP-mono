package com.haedong.erp.domains.route;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.haedong.erp.domains.route.dto.RouteResponse;

@Mapper 
public interface RouteMapper {
    List<RouteResponse> findAll();

    boolean existsByRouteNo(String routeNo);

    /** 노선 행을 잠그고 존재 여부를 확인한다. 같은 트랜잭션이 끝날 때까지 다른 요청은 기다린다 */
    Optional<String> lockByRouteNo(String routeNo);

    int countVehicles(String routeNo);

    int countTimetables(String routeNo);

    /** 이 노선에서 사용 중인 가장 큰 슬롯 번호 (사용 중인 슬롯이 없으면 null) */
    Integer findMaxSlotNo(String routeNo);

    void insert(@Param ("routeNo") String routeNo, @Param("totalSlots") int totalSlots);

    void updateTotalSlots(@Param("routeNo") String routeNo, @Param("totalSlots") int totalSlots);

    void delete(String routeNo);
}
