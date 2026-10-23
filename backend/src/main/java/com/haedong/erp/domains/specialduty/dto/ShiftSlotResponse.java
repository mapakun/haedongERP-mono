package com.haedong.erp.domains.specialduty.dto;

import java.util.List;

public record ShiftSlotResponse(String routeNo, int totalSlots, List<ShiftSlotRow> slots) {
}