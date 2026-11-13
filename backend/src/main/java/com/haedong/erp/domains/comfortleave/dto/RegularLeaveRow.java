package com.haedong.erp.domains.comfortleave.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 정규직 위로휴가 한 줄 (그해 기준).
 *
 * @param granted   그해 발생 개수 (입사한 해는 0, 그 다음 해부터 4)
 * @param remaining 발생 - 사용 (미리 쓰면 음수)
 */
public record RegularLeaveRow(
        Long employeeId,
        String name,
        LocalDate hireDate,
        LocalDate retiredAt,
        int granted,
        int used,
        int remaining,
        List<ComfortUseRow> uses
) {
}