package com.haedong.erp.domains.specialduty;

import com.haedong.erp.domains.specialduty.dto.NightPick;
import com.haedong.erp.domains.specialduty.dto.NightRuleRow;
import com.haedong.erp.domains.specialduty.dto.NightSlotRow;
import com.haedong.erp.domains.specialduty.dto.NightTripRow;
import com.haedong.erp.domains.specialduty.dto.ShiftSlotRow;
import com.haedong.erp.domains.timetable.DayType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SpecialDutyMapper {

    // ===== 노선 =====

    /** 노선의 총 슬롯 수 (노선이 없으면 비어 있음) */
    Optional<Integer> findRouteTotalSlots(String routeNo);

    /** 노선 행을 잠그고 총 슬롯 수를 돌려준다. 같은 노선의 저장이 겹치지 않게 한다 */
    Optional<Integer> lockRouteTotalSlots(String routeNo);

    // ===== 단축 =====

    /** 노선의 순번 전체(1 ~ 총 슬롯 수)와 단축 켜짐 여부, 시간표에 있는지 여부 */
    List<ShiftSlotRow> findShiftSlots(String routeNo);

    void deleteShiftSlots(String routeNo);

    void insertShiftSlot(@Param("routeNo") String routeNo, @Param("slotNo") int slotNo);

    // ===== 심야 시간표 =====

    List<NightTripRow> findNightTrips();

    void updateNightTrip(@Param("nightNo") int nightNo, @Param("departMin") int departMin);

    // ===== 노선별 최소 휴식 =====

    List<NightRuleRow> findNightRules();

    Optional<Integer> findMinRestMin(String routeNo);

    void upsertNightRule(@Param("routeNo") String routeNo, @Param("minRestMin") int minRestMin);

    // ===== 심야 가능 순번 =====

    /** 이 노선이 시간표를 가진 형태들 */
    List<DayType> findDayTypes(String routeNo);

    /** 노선·형태의 시간표에 있는 순번과 마지막 도착 시각 */
    List<NightSlotRow> findTimetableSlots(@Param("routeNo") String routeNo, @Param("dayType") DayType dayType);

    List<NightPick> findNightPicks(@Param("routeNo") String routeNo, @Param("dayType") DayType dayType);

    void deleteNightPicks(@Param("routeNo") String routeNo, @Param("dayType") DayType dayType);

    void insertNightPick(@Param("routeNo") String routeNo,
                         @Param("dayType") DayType dayType,
                         @Param("slotNo") int slotNo,
                         @Param("nightNo") int nightNo);
}