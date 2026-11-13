package com.haedong.erp.domains.missedtrip;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.missedtrip.dto.MissedTripPageResponse;
import com.haedong.erp.domains.missedtrip.dto.MissedTripRow;
import com.haedong.erp.domains.missedtrip.dto.MissedTripSaveRequest;
import com.haedong.erp.domains.missedtrip.dto.MissedTripSearchRequest;
import com.haedong.erp.domains.missedtrip.dto.MissedTripSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissedTripService {

    /** 엑셀 내보내기 한 번에 받을 수 있는 기간(일)과 건수. 너무 큰 조회로 서버에 부담을 주지 않기 위한 한도 */
    static final int EXPORT_MAX_DAYS = 366;
    static final int EXPORT_MAX_ROWS = 10_000;

    private final MissedTripMapper missedTripMapper;
    private final MissedTripExcelWriter excelWriter;

    // ===================== 목록 =====================

    @Transactional(readOnly = true)
    public MissedTripPageResponse search(MissedTripSearchRequest request) {
        checkDateOrder(request);
        MissedTripSummary summary = missedTripMapper.summarize(request);
        List<MissedTripRow> items = summary.getTotalCount() == 0 ? List.of() : missedTripMapper.search(request);
        return new MissedTripPageResponse(items, summary.getTotalCount(), summary.getMissedSum(),
                request.getPage(), request.getSize());
    }

    // ===================== 입력·수정·삭제 =====================

    @Transactional
    public void create(MissedTripSaveRequest request, LoginUser currentUser) {
        checkRoute(request.getRouteNo());
        missedTripMapper.insert(request, currentUser.getName());
    }

    @Transactional
    public void update(Long id, MissedTripSaveRequest request) {
        checkRoute(request.getRouteNo());
        if (missedTripMapper.update(id, request) == 0) {
            throw notFound();
        }
    }

    @Transactional
    public void delete(Long id) {
        if (missedTripMapper.delete(id) == 0) {
            throw notFound();
        }
    }

    // ===================== 엑셀 내보내기 =====================

    /** 기간(시작일·종료일)이 필수이고, 기간과 건수가 한도를 넘으면 파일을 만들기 전에 거절한다 */
    @Transactional(readOnly = true)
    public byte[] export(MissedTripSearchRequest request) {
        if (request.getFromDate() == null || request.getToDate() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "내보낼 기간(시작일·종료일)을 입력해 주세요.");
        }
        checkDateOrder(request);

        long days = ChronoUnit.DAYS.between(request.getFromDate(), request.getToDate()) + 1;
        if (days > EXPORT_MAX_DAYS) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "내보내기는 한 번에 최대 " + EXPORT_MAX_DAYS + "일까지 가능합니다. 기간을 줄여 주세요.");
        }

        long count = missedTripMapper.summarize(request).getTotalCount();
        if (count > EXPORT_MAX_ROWS) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "조회 결과가 " + count + "건이라 내보낼 수 없습니다. (최대 " + EXPORT_MAX_ROWS + "건) 기간을 줄여 주세요.");
        }

        return excelWriter.write(missedTripMapper.findForExport(request));
    }

    // ===================== 공통 =====================

    private void checkDateOrder(MissedTripSearchRequest request) {
        if (request.getFromDate() != null && request.getToDate() != null
                && request.getFromDate().isAfter(request.getToDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "시작일이 종료일보다 늦을 수 없습니다.");
        }
    }

    /** 노선은 외래키로 묶지 않았으므로 실제 있는 노선인지 서버가 확인한다 */
    private void checkRoute(String routeNo) {
        if (!missedTripMapper.existsRoute(routeNo)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 노선입니다.");
        }
    }

    private static BusinessException notFound() {
        return new BusinessException(HttpStatus.NOT_FOUND, "결행 기록을 찾을 수 없습니다.");
    }
}