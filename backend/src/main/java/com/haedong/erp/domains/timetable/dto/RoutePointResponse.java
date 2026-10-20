package com.haedong.erp.domains.timetable.dto;

import com.haedong.erp.domains.timetable.PointKey;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 노선 위의 한 장소 이름 */
@Getter
@NoArgsConstructor
public class RoutePointResponse {

    private PointKey pointKey;
    private String name;
}