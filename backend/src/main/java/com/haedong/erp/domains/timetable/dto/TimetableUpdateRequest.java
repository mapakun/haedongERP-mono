package com.haedong.erp.domains.timetable.dto;

import com.haedong.erp.domains.timetable.TripShift;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** 시간표 한 회차의 값. 시각은 0시부터의 분(24시 이후는 1440 이상)이다 */
@Getter
@Setter
public class TimetableUpdateRequest {

    static final String TIME_RANGE = "시각이 올바르지 않습니다.";

    @NotNull(message = "오전/오후를 선택해 주세요.")
    private TripShift shift;

    @NotNull(message = "출발 시각을 입력해 주세요.")
    @Min(value = 0, message = TIME_RANGE)
    @Max(value = 2879, message = TIME_RANGE)
    private Integer departMin;

    @Min(value = 0, message = TIME_RANGE)
    @Max(value = 2879, message = TIME_RANGE)
    private Integer mid1Min;

    @Min(value = 0, message = TIME_RANGE)
    @Max(value = 2879, message = TIME_RANGE)
    private Integer mid2Min;

    @NotNull(message = "회차 시각을 입력해 주세요.")
    @Min(value = 0, message = TIME_RANGE)
    @Max(value = 2879, message = TIME_RANGE)
    private Integer turnMin;

    @Min(value = 0, message = TIME_RANGE)
    @Max(value = 2879, message = TIME_RANGE)
    private Integer mid3Min;

    @Min(value = 0, message = TIME_RANGE)
    @Max(value = 2879, message = TIME_RANGE)
    private Integer mid4Min;

    @NotNull(message = "도착 시각을 입력해 주세요.")
    @Min(value = 0, message = TIME_RANGE)
    @Max(value = 2879, message = TIME_RANGE)
    private Integer arriveMin;
}