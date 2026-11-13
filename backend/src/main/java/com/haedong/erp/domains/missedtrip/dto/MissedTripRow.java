package com.haedong.erp.domains.missedtrip.dto;

import com.haedong.erp.domains.missedtrip.MissedReason;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 결행 기록 한 건 */
@Getter
@NoArgsConstructor
public class MissedTripRow {

    private Long id;
    private LocalDate missedOn;
    private String routeNo;
    private String vehicleNo;
    /** 순번: 그날 그 노선의 몇 번째 운행인지 (예: 40, 64, 85, 종일) */
    private String tripLabel;
    /** 승무원 이름 (종일 결행 등으로 없으면 null) */
    private String driverName;
    private int missedCount;
    private MissedReason reason;
    private String note;
    /** 기록한 관리자 이름 (엑셀 이관분은 "엑셀 이관") */
    private String createdBy;
}