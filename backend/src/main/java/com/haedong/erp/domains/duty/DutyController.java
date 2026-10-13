package com.haedong.erp.domains.duty;

import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.duty.dto.DutyDriverResponse;
import com.haedong.erp.domains.duty.dto.DutyRosterResponse;
import com.haedong.erp.domains.duty.dto.DutyStartBulkRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 기본 근무표 API (관리자 이상: SecurityConfig 에서 /api/duty/** 를 제한) */
@RestController
@RequestMapping("/api/duty")
@RequiredArgsConstructor
public class DutyController {

    private final DutyService dutyService;

    @GetMapping("/drivers")
    public List<DutyDriverResponse> drivers() {
        return dutyService.drivers();
    }

    @GetMapping("/roster")
    public DutyRosterResponse roster(@RequestParam int year, @RequestParam int month) {
        return dutyService.roster(year, month);
    }

    @PostMapping("/starts")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Integer> createStarts(@Valid @RequestBody DutyStartBulkRequest request,
                                             @AuthenticationPrincipal LoginUser loginUser) {
        return Map.of("count", dutyService.createStarts(request, loginUser));
    }
}