package com.haedong.erp.domains.specialduty.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NightRuleSaveRequest {

    @NotNull(message = "최소 휴식 시간을 입력해 주세요.")
    @Min(value = 0, message = "최소 휴식 시간은 0분 이상이어야 합니다.")
    @Max(value = 300, message = "최소 휴식 시간은 300분 이하로 입력해 주세요.")
    private Integer minRestMin;
}