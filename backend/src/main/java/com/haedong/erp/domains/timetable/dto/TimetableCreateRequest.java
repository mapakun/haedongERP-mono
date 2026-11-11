package com.haedong.erp.domains.timetable.dto;

import com.haedong.erp.domains.timetable.DayType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** 새 회차 추가: 어느 칸(노선·형태·순번·회차)인지 + 값 */
@Getter
@Setter
public class TimetableCreateRequest extends TimetableUpdateRequest {

    @NotBlank(message = "노선을 선택해 주세요.")
    private String routeNo;

    @NotNull(message = "시간표 형태를 선택해 주세요.")
    private DayType dayType;

    @NotNull(message = "순번을 입력해 주세요.")
    @Min(value = 1, message = "순번은 1 이상이어야 합니다.")
    private Integer slotNo;

    @NotNull(message = "회차 번호를 입력해 주세요.")
    @Min(value = 1, message = "회차 번호는 1 이상이어야 합니다.")
    private Integer tripNo;
}