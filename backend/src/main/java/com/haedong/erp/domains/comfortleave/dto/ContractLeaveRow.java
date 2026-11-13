package com.haedong.erp.domains.comfortleave.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 촉탁직 위로휴가 한 줄 (계약 한 건 기준).
 *
 * @param granted   오늘(계약이 끝났으면 만료일) 기준 발생 개수
 * @param remaining 발생 - 사용 (미리 쓰면 음수)
 */
public record ContractLeaveRow(
        Long employeeId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate retiredAt,
        int granted,
        int used,
        int remaining,
        List<ComfortUseRow> uses
) {
}