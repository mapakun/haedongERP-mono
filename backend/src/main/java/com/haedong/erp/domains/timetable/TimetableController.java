package com.haedong.erp.domains.timetable;

import com.haedong.erp.domains.timetable.dto.TimetableCopyRequest;
import com.haedong.erp.domains.timetable.dto.TimetableCreateRequest;
import com.haedong.erp.domains.timetable.dto.TimetableRowResponse;
import com.haedong.erp.domains.timetable.dto.TimetableUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timetables")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    /** 노선+형태는 필수. 오전/오후와 순번까지 주면 그 기사가 탈 회차만 돌려준다 */
    @GetMapping
    public List<TimetableRowResponse> search(@RequestParam String routeNo,
                                             @RequestParam DayType dayType,
                                             @RequestParam(required = false) TripShift shift,
                                             @RequestParam(required = false) Integer slotNo) {
        return timetableService.search(routeNo, dayType, shift, slotNo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody TimetableCreateRequest request) {
        timetableService.create(request);
    }

    @PutMapping("/{routeNo}/{dayType}/{slotNo}/{tripNo}")
    public void update(@PathVariable String routeNo,
                       @PathVariable DayType dayType,
                       @PathVariable int slotNo,
                       @PathVariable int tripNo,
                       @Valid @RequestBody TimetableUpdateRequest request) {
        timetableService.update(routeNo, dayType, slotNo, tripNo, request);
    }

    @DeleteMapping("/{routeNo}/{dayType}/{slotNo}/{tripNo}")
    public void delete(@PathVariable String routeNo,
                       @PathVariable DayType dayType,
                       @PathVariable int slotNo,
                       @PathVariable int tripNo) {
        timetableService.delete(routeNo, dayType, slotNo, tripNo);
    }

    @PostMapping("/copy")
    public Map<String, Integer> copy(@Valid @RequestBody TimetableCopyRequest request) {
        return Map.of("copied", timetableService.copy(request));
    }
}