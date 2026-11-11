package com.haedong.erp.domains.timetable.dto;

import com.haedong.erp.domains.timetable.PointKey;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 노선의 장소 이름을 한꺼번에 저장한다. 목록에 없거나 이름이 비어 있는 위치는 지워진다.
 */
public record RoutePointSaveRequest(@NotNull(message = "장소 목록이 필요합니다.") @Valid List<Item> points) {

    public record Item(
            @NotNull(message = "위치를 선택해 주세요.") PointKey pointKey,
            @Size(max = 50, message = "장소 이름은 50자 이내로 입력해 주세요.") String name
    ) {
    }
}