package com.haedong.erp.domains.timetable;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.domains.timetable.dto.TimetableCopyRequest;
import com.haedong.erp.domains.timetable.dto.TimetableCreateRequest;
import com.haedong.erp.domains.timetable.dto.TimetableRowResponse;
import com.haedong.erp.domains.timetable.dto.TimetableUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableMapper timetableMapper;

    @Transactional(readOnly = true)
    public List<TimetableRowResponse> search(String routeNo, DayType dayType, TripShift shift, Integer slotNo) {
        return timetableMapper.search(routeNo, dayType, shift, slotNo);
    }

    @Transactional
    public void create(TimetableCreateRequest request) {
        int totalSlots = lockRouteTotalSlots(request.getRouteNo());
        if (request.getSlotNo() > totalSlots) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "순번은 1 ~ " + totalSlots + " 사이로 입력해 주세요.");
        }
        checkTimes(request);

        if (timetableMapper.exists(request.getRouteNo(), request.getDayType(),
                request.getSlotNo(), request.getTripNo())) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 등록된 회차입니다.");
        }
        timetableMapper.insert(request);
    }

    @Transactional
    public void update(String routeNo, DayType dayType, int slotNo, int tripNo, TimetableUpdateRequest request) {
        checkTimes(request);
        if (timetableMapper.update(routeNo, dayType, slotNo, tripNo, request) == 0) {
            throw notFound();
        }
    }

    @Transactional
    public void delete(String routeNo, DayType dayType, int slotNo, int tripNo) {
        if (timetableMapper.delete(routeNo, dayType, slotNo, tripNo) == 0) {
            throw notFound();
        }
    }

    /** 같은 노선 안에서 한 형태의 시간표를 다른 형태로 복사한다. 복사된 행 수를 돌려준다 */
    @Transactional
    public int copy(TimetableCopyRequest request) {
        String routeNo = request.getRouteNo();
        if (request.getFromDayType() == request.getToDayType()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "같은 형태로는 복사할 수 없습니다.");
        }
        lockRouteTotalSlots(routeNo);

        if (timetableMapper.countByRouteAndDayType(routeNo, request.getFromDayType()) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "복사할 시간표가 없습니다.");
        }

        int existing = timetableMapper.countByRouteAndDayType(routeNo, request.getToDayType());
        if (existing > 0) {
            if (!request.isOverwrite()) {
                throw new BusinessException(HttpStatus.CONFLICT,
                        "대상 형태에 이미 시간표가 " + existing + "행 있습니다. 덮어쓰려면 덮어쓰기를 선택해 주세요.");
            }
            timetableMapper.deleteByRouteAndDayType(routeNo, request.getToDayType());
        }

        return timetableMapper.copyDayType(routeNo, request.getFromDayType(), request.getToDayType());
    }

    /** 시각이 출발 → 중간지1 → 중간지2 → 회차 → 중간지3 → 중간지4 → 도착 순서로 이어지는지 확인한다 */
    private void checkTimes(TimetableUpdateRequest request) {
        Integer[] inOrder = {
                request.getDepartMin(), request.getMid1Min(), request.getMid2Min(), request.getTurnMin(),
                request.getMid3Min(), request.getMid4Min(), request.getArriveMin()
        };
        int previous = -1;
        for (Integer time : inOrder) {
            if (time == null) {
                continue; // 없는 중간지는 건너뛴다
            }
            if (time < previous) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "시각은 출발 → 중간지 → 회차 → 도착 순서로 이어져야 합니다.");
            }
            previous = time;
        }
        if (request.getDepartMin() >= request.getArriveMin()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "도착 시각은 출발 시각보다 늦어야 합니다.");
        }
    }

    /** 노선이 있는지 확인하면서 잠그고, 총 슬롯 수를 돌려준다 */
    private int lockRouteTotalSlots(String routeNo) {
        return timetableMapper.lockRouteTotalSlots(routeNo)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "노선을 찾을 수 없습니다."));
    }

    private BusinessException notFound() {
        return new BusinessException(HttpStatus.NOT_FOUND, "해당 회차를 찾을 수 없습니다.");
    }
}