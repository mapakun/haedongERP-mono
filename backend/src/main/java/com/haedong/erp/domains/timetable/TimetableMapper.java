package com.haedong.erp.domains.timetable;

import com.haedong.erp.domains.timetable.dto.TimetableCreateRequest;
import com.haedong.erp.domains.timetable.dto.TimetableRowResponse;
import com.haedong.erp.domains.timetable.dto.TimetableUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TimetableMapper {

    /** 노선+형태는 필수, 오전/오후와 순번은 주어졌을 때만 거른다 */
    List<TimetableRowResponse> search(@Param("routeNo") String routeNo,
                                      @Param("dayType") DayType dayType,
                                      @Param("shift") TripShift shift,
                                      @Param("slotNo") Integer slotNo);

    boolean exists(@Param("routeNo") String routeNo,
                   @Param("dayType") DayType dayType,
                   @Param("slotNo") int slotNo,
                   @Param("tripNo") int tripNo);

    /** 노선 행을 잠그고 총 슬롯 수를 돌려준다 (노선이 없으면 비어 있음) */
    Optional<Integer> lockRouteTotalSlots(String routeNo);

    void insert(TimetableCreateRequest request);

    /** 바뀐 행 수를 돌려준다 (0이면 그런 회차가 없다는 뜻) */
    int update(@Param("routeNo") String routeNo,
               @Param("dayType") DayType dayType,
               @Param("slotNo") int slotNo,
               @Param("tripNo") int tripNo,
               @Param("v") TimetableUpdateRequest values);

    int delete(@Param("routeNo") String routeNo,
               @Param("dayType") DayType dayType,
               @Param("slotNo") int slotNo,
               @Param("tripNo") int tripNo);

    int countByRouteAndDayType(@Param("routeNo") String routeNo, @Param("dayType") DayType dayType);

    int deleteByRouteAndDayType(@Param("routeNo") String routeNo, @Param("dayType") DayType dayType);

    /** 한 형태의 시간표를 통째로 다른 형태로 복사하고, 복사된 행 수를 돌려준다 */
    int copyDayType(@Param("routeNo") String routeNo,
                    @Param("fromDayType") DayType fromDayType,
                    @Param("toDayType") DayType toDayType);
}