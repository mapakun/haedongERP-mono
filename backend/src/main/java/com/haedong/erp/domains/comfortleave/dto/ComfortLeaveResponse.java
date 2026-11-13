package com.haedong.erp.domains.comfortleave.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 연도별 위로휴가 현황.
 *
 * @param asOf 촉탁직 발생 개수를 계산한 기준일 (오늘)
 */
public record ComfortLeaveResponse(
        int year,
        LocalDate asOf,
        List<RegularLeaveRow> regulars,
        List<ContractLeaveRow> contracts,
        List<MonthlySummaryRow> monthly
) {
}