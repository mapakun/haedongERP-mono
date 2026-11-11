package com.haedong.erp.domains.specialduty.dto;

import com.haedong.erp.domains.timetable.DayType;

import java.util.List;

/**
 * 심야 가능 순번 화면 한 장에 필요한 정보.
 *
 * @param minRestMin 이 노선의 최소 휴식 시간 (아직 등록된 기준이 없으면 null)
 * @param nights     심야 시간표 (출발 시각)
 * @param slots      시간표에 있는 순번들과, 각 순번이 뛸 수 있다고 체크된 심야 순번
 */
public record NightSlotResponse(
        String routeNo,
        DayType dayType,
        Integer minRestMin,
        List<NightTripRow> nights,
        List<Slot> slots
) {

    public record Slot(int slotNo, int lastArriveMin, List<Integer> nightNos) {
    }
}