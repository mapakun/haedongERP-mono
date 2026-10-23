package com.haedong.erp.domains.specialduty;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.domains.specialduty.dto.NightPick;
import com.haedong.erp.domains.specialduty.dto.NightRuleRow;
import com.haedong.erp.domains.specialduty.dto.NightRuleSaveRequest;
import com.haedong.erp.domains.specialduty.dto.NightSlotResponse;
import com.haedong.erp.domains.specialduty.dto.NightSlotRow;
import com.haedong.erp.domains.specialduty.dto.NightSlotSaveRequest;
import com.haedong.erp.domains.specialduty.dto.NightTripRow;
import com.haedong.erp.domains.specialduty.dto.NightTripSaveRequest;
import com.haedong.erp.domains.specialduty.dto.ShiftSlotResponse;
import com.haedong.erp.domains.specialduty.dto.ShiftSlotRow;
import com.haedong.erp.domains.specialduty.dto.ShiftSlotSaveRequest;
import com.haedong.erp.domains.timetable.DayType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialDutyService {

    private final SpecialDutyMapper specialDutyMapper;

    // ===================== 단축 =====================

    @Transactional(readOnly = true)
    public ShiftSlotResponse findShiftSlots(String routeNo) {
        int totalSlots = specialDutyMapper.findRouteTotalSlots(routeNo)
                .orElseThrow(SpecialDutyService::routeNotFound);
        List<ShiftSlotRow> slots = specialDutyMapper.findShiftSlots(routeNo);
        return new ShiftSlotResponse(routeNo, totalSlots, slots);
    }

    /** 노선의 단축 가능 순번을 통째로 저장한다. 평일·토요일·일요일 중 어디에 쓸지는 이 저장값이 정하지 않는다 */
    @Transactional
    public void saveShiftSlots(String routeNo, ShiftSlotSaveRequest request) {
        int totalSlots = lockRouteTotalSlots(routeNo);

        Set<Integer> slotNos = new TreeSet<>(request.slotNos());
        for (int slotNo : slotNos) {
            if (slotNo > totalSlots) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "순번은 1 ~ " + totalSlots + " 사이여야 합니다. (" + slotNo + "번)");
            }
        }

        specialDutyMapper.deleteShiftSlots(routeNo);
        for (int slotNo : slotNos) {
            specialDutyMapper.insertShiftSlot(routeNo, slotNo);
        }
    }

    // ===================== 심야 시간표 =====================

    @Transactional(readOnly = true)
    public List<NightTripRow> findNightTrips() {
        return specialDutyMapper.findNightTrips();
    }

    /** 심야 출발 시각을 저장한다. 심야 순번의 개수와 번호는 그대로 두고 시각만 바꾼다 */
    @Transactional
    public void saveNightTrips(NightTripSaveRequest request) {
        Map<Integer, Integer> departByNightNo = new TreeMap<>();
        for (NightTripSaveRequest.Item item : request.trips()) {
            if (departByNightNo.put(item.nightNo(), item.departMin()) != null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "같은 심야 순번이 두 번 들어 있습니다.");
            }
        }

        Set<Integer> existing = specialDutyMapper.findNightTrips().stream()
                .map(NightTripRow::getNightNo)
                .collect(Collectors.toSet());
        if (!departByNightNo.keySet().equals(existing)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "심야 순번 " + existing + " 의 시각을 모두 한 번씩 보내 주세요.");
        }

        int previous = -1;
        for (int departMin : departByNightNo.values()) {
            if (departMin <= previous) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "심야 출발 시각은 1순번부터 차례로 늦어져야 합니다.");
            }
            previous = departMin;
        }

        departByNightNo.forEach((nightNo, departMin) -> specialDutyMapper.updateNightTrip(nightNo, departMin));
    }

    // ===================== 노선별 최소 휴식 =====================

    @Transactional(readOnly = true)
    public List<NightRuleRow> findNightRules() {
        return specialDutyMapper.findNightRules();
    }

    @Transactional
    public void saveNightRule(String routeNo, NightRuleSaveRequest request) {
        lockRouteTotalSlots(routeNo);
        specialDutyMapper.upsertNightRule(routeNo, request.getMinRestMin());
    }

    // ===================== 심야 가능 순번 =====================

    @Transactional(readOnly = true)
    public List<DayType> findDayTypes(String routeNo) {
        specialDutyMapper.findRouteTotalSlots(routeNo).orElseThrow(SpecialDutyService::routeNotFound);
        return specialDutyMapper.findDayTypes(routeNo);
    }

    /** 노선·형태의 시간표에 있는 순번마다 마지막 도착 시각과, 체크되어 있는 심야 순번을 돌려준다 */
    @Transactional(readOnly = true)
    public NightSlotResponse findNightSlots(String routeNo, DayType dayType) {
        requireTimetable(routeNo, dayType);

        Map<Integer, List<Integer>> picksBySlot = new HashMap<>();
        for (NightPick pick : specialDutyMapper.findNightPicks(routeNo, dayType)) {
            picksBySlot.computeIfAbsent(pick.getSlotNo(), key -> new ArrayList<>()).add(pick.getNightNo());
        }

        List<NightSlotResponse.Slot> slots = specialDutyMapper.findTimetableSlots(routeNo, dayType).stream()
                .map(row -> new NightSlotResponse.Slot(
                        row.getSlotNo(), row.getLastArriveMin(), picksBySlot.getOrDefault(row.getSlotNo(), List.of())))
                .toList();

        return new NightSlotResponse(routeNo, dayType,
                specialDutyMapper.findMinRestMin(routeNo).orElse(null),
                specialDutyMapper.findNightTrips(), slots);
    }

    /** 노선·형태의 심야 가능 순번을 통째로 저장한다 */
    @Transactional
    public void saveNightSlots(String routeNo, DayType dayType, NightSlotSaveRequest request) {
        lockRouteTotalSlots(routeNo);
        requireTimetable(routeNo, dayType);

        Set<Integer> timetableSlots = specialDutyMapper.findTimetableSlots(routeNo, dayType).stream()
                .map(NightSlotRow::getSlotNo)
                .collect(Collectors.toSet());
        Set<Integer> nightNos = specialDutyMapper.findNightTrips().stream()
                .map(NightTripRow::getNightNo)
                .collect(Collectors.toSet());

        // (순번, 심야 순번) 쌍을 모으면서 하나씩 검사한다. 같은 쌍이 여러 번 와도 한 번만 남는다
        Set<Pair> pairs = new LinkedHashSet<>();
        for (NightSlotSaveRequest.Item item : request.slots()) {
            if (!timetableSlots.contains(item.slotNo())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        item.slotNo() + "번 순번은 이 시간표에 없습니다.");
            }
            for (Integer nightNo : item.nightNos()) {
                if (!nightNos.contains(nightNo)) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "심야 " + nightNo + "순번은 없습니다.");
                }
                pairs.add(new Pair(item.slotNo(), nightNo));
            }
        }

        specialDutyMapper.deleteNightPicks(routeNo, dayType);
        for (Pair pair : pairs) {
            specialDutyMapper.insertNightPick(routeNo, dayType, pair.slotNo(), pair.nightNo());
        }
    }

    // ===================== 공통 =====================

    /** 노선이 그 형태의 시간표를 가지고 있는지 확인한다 */
    private void requireTimetable(String routeNo, DayType dayType) {
        if (specialDutyMapper.findDayTypes(routeNo).contains(dayType)) {
            return;
        }
        if (dayType == DayType.SATURDAY) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "이 노선은 토요일 시간표가 없어 일요일·공휴일 시간표를 씁니다. 공휴일 형태로 설정해 주세요.");
        }
        throw new BusinessException(HttpStatus.BAD_REQUEST, "이 노선에는 해당 형태의 시간표가 없습니다.");
    }

    /** 노선이 있는지 확인하면서 잠그고, 총 슬롯 수를 돌려준다 */
    private int lockRouteTotalSlots(String routeNo) {
        return specialDutyMapper.lockRouteTotalSlots(routeNo).orElseThrow(SpecialDutyService::routeNotFound);
    }

    private static BusinessException routeNotFound() {
        return new BusinessException(HttpStatus.NOT_FOUND, "노선을 찾을 수 없습니다.");
    }

    private record Pair(int slotNo, int nightNo) {
    }
}