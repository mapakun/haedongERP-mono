package com.haedong.erp.domains.route;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.domains.route.dto.RouteCreateRequest;
import com.haedong.erp.domains.route.dto.RouteResponse;
import com.haedong.erp.domains.route.dto.RouteUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteMapper routeMapper;

    @Transactional(readOnly = true)
    public List<RouteResponse> findAll() {
        return routeMapper.findAll();
    }

    @Transactional
    public String create(RouteCreateRequest request) {
        if (routeMapper.existsByRouteNo(request.getRouteNo())) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 등록된 노선 번호입니다.");
        }
        routeMapper.insert(request.getRouteNo(), request.getTotalSlots());
        return request.getRouteNo();
    }

    /** 총 슬롯 수 변경. 이미 배정된 차량이 새 슬롯 수에 맞지 않으면 막는다 */
    @Transactional
    public void updateTotalSlots(String routeNo, RouteUpdateRequest request) {
        lockOrThrow(routeNo);
        int newTotal = request.getTotalSlots();

        int assigned = routeMapper.countVehicles(routeNo);
        if (newTotal < assigned) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "현재 " + assigned + "대가 배정되어 있어 슬롯 수를 " + newTotal + "개로 줄일 수 없습니다.");
        }

        Integer maxSlotNo = routeMapper.findMaxSlotNo(routeNo);
        if (maxSlotNo != null && newTotal < maxSlotNo) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    maxSlotNo + "번 슬롯을 사용 중인 차량이 있어 슬롯 수를 " + newTotal + "개로 줄일 수 없습니다.");
        }

        routeMapper.updateTotalSlots(routeNo, newTotal);
    }

    @Transactional
    public void delete(String routeNo) {
        lockOrThrow(routeNo);

        if (routeMapper.countVehicles(routeNo) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "차량이 배정된 노선은 삭제할 수 없습니다. 차량을 다른 노선으로 옮기거나 예비 차량으로 바꿔 주세요.");
        }
        routeMapper.delete(routeNo);
    }

    private void lockOrThrow(String routeNo) {
        routeMapper.lockByRouteNo(routeNo)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "노선을 찾을 수 없습니다."));
    }
}