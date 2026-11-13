package com.haedong.erp.domains.missedtrip;

import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.missedtrip.dto.MissedTripPageResponse;
import com.haedong.erp.domains.missedtrip.dto.MissedTripSaveRequest;
import com.haedong.erp.domains.missedtrip.dto.MissedTripSearchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/missed-trips")
@RequiredArgsConstructor
public class MissedTripController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final MissedTripService missedTripService;

    /** 목록(한 페이지) + 조건 전체의 건수·결행 횟수 합계. 로그인한 누구나 */
    @GetMapping
    public MissedTripPageResponse search(MissedTripSearchRequest request) {
        return missedTripService.search(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody MissedTripSaveRequest request,
                       @AuthenticationPrincipal LoginUser loginUser) {
        missedTripService.create(request, loginUser);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @Valid @RequestBody MissedTripSaveRequest request) {
        missedTripService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        missedTripService.delete(id);
    }

    /** 엑셀 내보내기. 기간 필수, 관리자만 (SecurityConfig 에서 막는다) */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(MissedTripSearchRequest request) {
        byte[] file = missedTripService.export(request);

        // 한글 파일명은 인코딩해서 보내야 깨지지 않는다
        String fileName = "결행목록_" + request.getFromDate() + "_" + request.getToDate() + ".xlsx";
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(XLSX)
                .body(file);
    }
}