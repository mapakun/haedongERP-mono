package com.haedong.erp.domains.dispatch;

import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.dispatch.dto.RouteRotationRequest;
import com.haedong.erp.domains.dispatch.dto.RouteRotationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes/{routeNo}/rotations")
@RequiredArgsConstructor
public class RouteRotationController {

    private final DispatchService dispatchService;

    @GetMapping
    public List<RouteRotationResponse> rotations(@PathVariable String routeNo) {
        return dispatchService.rotations(routeNo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable String routeNo,
                       @Valid @RequestBody RouteRotationRequest request,
                       @AuthenticationPrincipal LoginUser loginUser) {
        dispatchService.createRotation(routeNo, request, loginUser);
    }
}