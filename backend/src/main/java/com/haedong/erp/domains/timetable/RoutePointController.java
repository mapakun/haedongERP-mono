package com.haedong.erp.domains.timetable;

import com.haedong.erp.domains.timetable.dto.RoutePointResponse;
import com.haedong.erp.domains.timetable.dto.RoutePointSaveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/route-points")
@RequiredArgsConstructor
public class RoutePointController {

    private final RoutePointService routePointService;

    @GetMapping("/{routeNo}")
    public List<RoutePointResponse> findByRouteNo(@PathVariable String routeNo) {
        return routePointService.findByRouteNo(routeNo);
    }

    @PutMapping("/{routeNo}")
    public void save(@PathVariable String routeNo, @Valid @RequestBody RoutePointSaveRequest request) {
        routePointService.save(routeNo, request);
    }
}