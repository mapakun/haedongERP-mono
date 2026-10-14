package com.haedong.erp.domains.duty.dto;

import java.util.List;

public record DutyRosterRow(String name, Integer seniorityNo, List<DutyCell> cells) {
}
