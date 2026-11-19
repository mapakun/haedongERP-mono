package com.haedong.erp.domains.routeroster;

import com.haedong.erp.domains.routeroster.dto.RouteRosterAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface RouteRosterMapper {

    boolean existsRoute(@Param("routeNo") String routeNo);

    // 같은 노선을 두 사람이 동시에 저장하지 못하게 노선 줄을 잠근다 (없는 노선이면 null)
    String lockRoute(@Param("routeNo") String routeNo);

    // 한 노선·한 달의 배치 (출력 순서대로)
    List<RouteRosterLine> findLines(@Param("rosterMonth") LocalDate rosterMonth,
                                    @Param("routeNo") String routeNo);

    // 같은 달 다른 노선에 배치된 기사
    List<RouteRosterAssignment> findOtherAssignments(@Param("rosterMonth") LocalDate rosterMonth,
                                                     @Param("routeNo") String routeNo);

    int countLines(@Param("rosterMonth") LocalDate rosterMonth, @Param("routeNo") String routeNo);

    void deleteLines(@Param("rosterMonth") LocalDate rosterMonth, @Param("routeNo") String routeNo);

    void insertLines(@Param("rosterMonth") LocalDate rosterMonth,
                     @Param("routeNo") String routeNo,
                     @Param("lines") List<RouteRosterLine> lines,
                     @Param("createdBy") Long createdBy);

    // 차량 관리의 노선 차량 (순번 순)
    List<RouteRosterLine> findVehicles(@Param("routeNo") String routeNo);

    // 직원 이름이 바뀌면 배치의 이름도 따라간다
    void renameEmployee(@Param("oldName") String oldName, @Param("newName") String newName);
}
