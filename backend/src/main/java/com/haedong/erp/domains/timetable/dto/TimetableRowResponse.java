package com.haedong.erp.domains.timetable.dto;

import com.haedong.erp.domains.timetable.DayType;
import com.haedong.erp.domains.timetable.TripShift;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 시간표 한 회차. 시각은 0시부터의 분이고, 없는 중간지는 null 이다 */
@Getter
@NoArgsConstructor
public class TimetableRowResponse {

    private String routeNo;
    private DayType dayType;
    private int slotNo;
    private int tripNo;
    private TripShift shift;
    private int departMin;
    private Integer mid1Min;
    private Integer mid2Min;
    private int turnMin;
    private Integer mid3Min;
    private Integer mid4Min;
    private int arriveMin;
}