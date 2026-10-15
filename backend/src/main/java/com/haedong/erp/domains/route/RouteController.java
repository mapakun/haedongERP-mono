package com.haedong.erp.domains.route;

import com.haedong.erp.domains.route.dto.RouteCreateRequest;
import com.haedong.erp.domains.route.dto.RouteResponse;
import com.haedong.erp.domains.route.dto.RouteUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    public List<RouteResponse> findAll() {
        return routeService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> create(@Valid @RequestBody RouteCreateRequest request) {
        return Map.of("routeNo", routeService.create(request));
    }

    @PutMapping("/{routeNo}")
    public void updateTotalSlots(@PathVariable String routeNo,
                                 @Valid @RequestBody RouteUpdateRequest request) {
        routeService.updateTotalSlots(routeNo, request);
    }

    @DeleteMapping("/{routeNo}")
    public void delete(@PathVariable String routeNo) {
        routeService.delete(routeNo);
    }
}