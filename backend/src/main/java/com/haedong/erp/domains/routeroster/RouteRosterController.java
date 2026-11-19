package com.haedong.erp.domains.routeroster;

import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.routeroster.dto.RouteRosterInitResponse;
import com.haedong.erp.domains.routeroster.dto.RouteRosterResponse;
import com.haedong.erp.domains.routeroster.dto.RouteRosterSaveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 노선 근무표 API (관리자 이상: SecurityConfig 에서 /api/route-rosters/** 를 제한).
 * 시작점 변경은 근무표와 같은 데이터를 쓰므로 기존 POST /api/duty/starts 를 그대로 쓴다.
 */
@RestController
@RequestMapping("/api/route-rosters")
@RequiredArgsConstructor
public class RouteRosterController {

    private final RouteRosterService routeRosterService;

    @GetMapping
    public RouteRosterResponse roster(@RequestParam int year, @RequestParam int month,
                                      @RequestParam String routeNo) {
        return routeRosterService.roster(year, month, routeNo);
    }

    @PutMapping
    public void save(@RequestParam int year, @RequestParam int month, @RequestParam String routeNo,
                     @Valid @RequestBody RouteRosterSaveRequest request,
                     @AuthenticationPrincipal LoginUser loginUser) {
        routeRosterService.save(year, month, routeNo, request, loginUser);
    }

    @PostMapping("/copy-previous")
    @ResponseStatus(HttpStatus.CREATED)
    public RouteRosterInitResponse copyPrevious(@RequestParam int year, @RequestParam int month,
                                                @RequestParam String routeNo,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        return routeRosterService.copyPrevious(year, month, routeNo, loginUser);
    }

    @PostMapping("/from-vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    public RouteRosterInitResponse createFromVehicles(@RequestParam int year, @RequestParam int month,
                                                      @RequestParam String routeNo,
                                                      @AuthenticationPrincipal LoginUser loginUser) {
        return routeRosterService.createFromVehicles(year, month, routeNo, loginUser);
    }
}
