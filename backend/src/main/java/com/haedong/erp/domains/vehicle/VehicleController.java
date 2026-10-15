package com.haedong.erp.domains.vehicle;

import com.haedong.erp.domains.vehicle.dto.VehicleAssignRequest;
import com.haedong.erp.domains.vehicle.dto.VehicleCreateRequest;
import com.haedong.erp.domains.vehicle.dto.VehicleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public List<VehicleResponse> findAll(@RequestParam(required = false) String routeNo,
                                         @RequestParam(defaultValue = "false") boolean spareOnly) {
        return vehicleService.findAll(routeNo, spareOnly);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> create(@Valid @RequestBody VehicleCreateRequest request) {
        return Map.of("vehicleNo", vehicleService.create(request));
    }

    @PutMapping("/{vehicleNo}")
    public void assign(@PathVariable String vehicleNo,
                       @Valid @RequestBody VehicleAssignRequest request) {
        vehicleService.assign(vehicleNo, request);
    }

    @DeleteMapping("/{vehicleNo}")
    public void delete(@PathVariable String vehicleNo) {
        vehicleService.delete(vehicleNo);
    }
}