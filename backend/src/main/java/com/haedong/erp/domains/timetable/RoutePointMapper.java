package com.haedong.erp.domains.timetable;

import com.haedong.erp.domains.timetable.dto.RoutePointResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RoutePointMapper {

    /** 출발 → 중간지1 → 중간지2 → 회차 → 중간지3 → 중간지4 → 도착 순서로 돌려준다 */
    List<RoutePointResponse> findByRouteNo(String routeNo);

    /** 노선 행을 잠그고 존재 여부를 확인한다 */
    Optional<String> lockRoute(String routeNo);

    void deleteByRouteNo(String routeNo);

    void insert(@Param("routeNo") String routeNo,
                @Param("pointKey") PointKey pointKey,
                @Param("name") String name);
}