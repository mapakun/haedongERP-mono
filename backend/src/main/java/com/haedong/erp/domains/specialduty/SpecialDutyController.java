package com.haedong.erp.domains.specialduty;

import com.haedong.erp.domains.specialduty.dto.NightRuleRow;
import com.haedong.erp.domains.specialduty.dto.NightRuleSaveRequest;
import com.haedong.erp.domains.specialduty.dto.NightSlotResponse;
import com.haedong.erp.domains.specialduty.dto.NightSlotSaveRequest;
import com.haedong.erp.domains.specialduty.dto.NightTripRow;
import com.haedong.erp.domains.specialduty.dto.NightTripSaveRequest;
import com.haedong.erp.domains.specialduty.dto.ShiftSlotResponse;
import com.haedong.erp.domains.specialduty.dto.ShiftSlotSaveRequest;
import com.haedong.erp.domains.timetable.DayType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/special-duty")
@RequiredArgsConstructor
public class SpecialDutyController {

    private final SpecialDutyService specialDutyService;

    // ===== 단축 =====

    @GetMapping("/shift-slots")
    public ShiftSlotResponse findShiftSlots(@RequestParam String routeNo) {
        return specialDutyService.findShiftSlots(routeNo);
    }

    @PutMapping("/shift-slots/{routeNo}")
    public void saveShiftSlots(@PathVariable String routeNo, @Valid @RequestBody ShiftSlotSaveRequest request) {
        specialDutyService.saveShiftSlots(routeNo, request);
    }

    // ===== 심야 시간표 =====

    @GetMapping("/night-trips")
    public List<NightTripRow> findNightTrips() {
        return specialDutyService.findNightTrips();
    }

    @PutMapping("/night-trips")
    public void saveNightTrips(@Valid @RequestBody NightTripSaveRequest request) {
        specialDutyService.saveNightTrips(request);
    }

    // ===== 노선별 최소 휴식 =====

    @GetMapping("/night-rules")
    public List<NightRuleRow> findNightRules() {
        return specialDutyService.findNightRules();
    }

    @PutMapping("/night-rules/{routeNo}")
    public void saveNightRule(@PathVariable String routeNo, @Valid @RequestBody NightRuleSaveRequest request) {
        specialDutyService.saveNightRule(routeNo, request);
    }

    // ===== 심야 가능 순번 =====

    /** 이 노선이 시간표를 가진 형태들 (화면의 형태 선택지) */
    @GetMapping("/day-types")
    public List<DayType> findDayTypes(@RequestParam String routeNo) {
        return specialDutyService.findDayTypes(routeNo);
    }

    @GetMapping("/night-slots")
    public NightSlotResponse findNightSlots(@RequestParam String routeNo, @RequestParam DayType dayType) {
        return specialDutyService.findNightSlots(routeNo, dayType);
    }

    @PutMapping("/night-slots/{routeNo}/{dayType}")
    public void saveNightSlots(@PathVariable String routeNo,
                               @PathVariable DayType dayType,
                               @Valid @RequestBody NightSlotSaveRequest request) {
        specialDutyService.saveNightSlots(routeNo, dayType, request);
    }
}