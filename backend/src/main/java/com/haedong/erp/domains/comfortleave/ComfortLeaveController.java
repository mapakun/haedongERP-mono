package com.haedong.erp.domains.comfortleave;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.comfortleave.dto.ComfortLeaveResponse;
import com.haedong.erp.domains.comfortleave.dto.ContractCreateRequest;
import com.haedong.erp.domains.comfortleave.dto.DriverMemberRow;
import com.haedong.erp.domains.comfortleave.dto.MemberSaveRequest;
import com.haedong.erp.domains.comfortleave.dto.UseCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.haedong.erp.domains.comfortleave.dto.ComfortContractRow;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/comfort-leave")
@RequiredArgsConstructor
public class ComfortLeaveController {

    private final ComfortLeaveService comfortLeaveService;

    /** 연도별 위로휴가 현황. 연도를 주지 않으면 올해 */
    @GetMapping
    public ComfortLeaveResponse findYear(@RequestParam(required = false) Integer year) {
        int target = year != null ? year : LocalDate.now().getYear();
        if (target < 2000 || target > 2100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "연도가 올바르지 않습니다.");
        }
        return comfortLeaveService.findYear(target);
    }

    // ===== 사용 기록 =====

    @PostMapping("/uses")
    @ResponseStatus(HttpStatus.CREATED)
    public void addUse(@Valid @RequestBody UseCreateRequest request,
                       @AuthenticationPrincipal LoginUser loginUser) {
        comfortLeaveService.addUse(request, loginUser);
    }

    @DeleteMapping("/uses/{id}")
    public void deleteUse(@PathVariable Long id) {
        comfortLeaveService.deleteUse(id);
    }

    // ===== 대상자 관리 =====

    @GetMapping("/members")
    public List<DriverMemberRow> findDriverMembers() {
        return comfortLeaveService.findDriverMembers();
    }

    @PutMapping("/members/{employeeId}")
    public void saveMember(@PathVariable Long employeeId, @Valid @RequestBody MemberSaveRequest request) {
        comfortLeaveService.saveMember(employeeId, request);
    }

    // ===== 촉탁 계약 =====
    @GetMapping("/members/{employeeId}/contracts")
    public List<ComfortContractRow> findContracts(@PathVariable Long employeeId) {
        return comfortLeaveService.findContractsOf(employeeId);
    }

    @PostMapping("/members/{employeeId}/contracts")
    @ResponseStatus(HttpStatus.CREATED)
    public void addContract(@PathVariable Long employeeId, @Valid @RequestBody ContractCreateRequest request) {
        comfortLeaveService.addContract(employeeId, request);
    }

    @DeleteMapping("/members/{employeeId}/contracts/{startDate}")
    public void deleteContract(@PathVariable Long employeeId,
                               @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        comfortLeaveService.deleteContract(employeeId, startDate);
    }
}