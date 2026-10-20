package com.haedong.erp.domains.timetable.dto;

import com.haedong.erp.domains.timetable.DayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** 같은 노선 안에서 한 형태의 시간표를 다른 형태로 복사 (예: 평일 → 방학) */
@Getter
@Setter
public class TimetableCopyRequest {

    @NotBlank(message = "노선을 선택해 주세요.")
    private String routeNo;

    @NotNull(message = "복사할 시간표 형태를 선택해 주세요.")
    private DayType fromDayType;

    @NotNull(message = "복사 대상 형태를 선택해 주세요.")
    private DayType toDayType;

    /** true 면 대상 형태에 이미 있는 시간표를 지우고 덮어쓴다 */
    private boolean overwrite;
}