package com.haedong.erp.domains.vehicle;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.domains.vehicle.dto.VehicleAssignRequest;
import com.haedong.erp.domains.vehicle.dto.VehicleCreateRequest;
import com.haedong.erp.domains.vehicle.dto.VehicleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleMapper vehicleMapper;

    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll(String routeNo, boolean spareOnly) {
        return vehicleMapper.findAll(routeNo, spareOnly);
    }

    @Transactional
    public String create(VehicleCreateRequest request) {
        String vehicleNo = request.getVehicleNo();
        if (vehicleMapper.existsByVehicleNo(vehicleNo)) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 등록된 차량 번호입니다.");
        }

        String routeNo = blankToNull(request.getRouteNo());
        checkAssignment(vehicleNo, null, routeNo, request.getSlotNo());

        vehicleMapper.insert(vehicleNo, routeNo, request.getSlotNo());
        return vehicleNo;
    }

    /** 노선·슬롯 배정 변경. 노선을 비우면 예비 차량이 된다 */
    @Transactional
    public void assign(String vehicleNo, VehicleAssignRequest request) {
        VehicleResponse current = vehicleMapper.findByVehicleNo(vehicleNo)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "차량을 찾을 수 없습니다."));

        String routeNo = blankToNull(request.getRouteNo());
        checkAssignment(vehicleNo, current.getRouteNo(), routeNo, request.getSlotNo());

        vehicleMapper.updateAssignment(vehicleNo, routeNo, request.getSlotNo());
    }

    @Transactional
    public void delete(String vehicleNo) {
        if (vehicleMapper.delete(vehicleNo) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "차량을 찾을 수 없습니다.");
        }
    }

    /**
     * 차량을 routeNo 노선의 slotNo 슬롯에 둘 수 있는지 검사한다.
     * 노선 행을 잠근 상태에서 검사하므로, 같은 노선의 다른 배정·슬롯 수 변경은 이 트랜잭션이 끝날 때까지 기다린다.
     *
     * @param currentRouteNo 지금 소속 노선 (신규 등록이면 null)
     */
    private void checkAssignment(String vehicleNo, String currentRouteNo, String routeNo, Integer slotNo) {
        if (routeNo == null) {
            if (slotNo != null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "슬롯을 지정하려면 노선을 선택해 주세요.");
            }
            return; // 예비 차량
        }

        int totalSlots = vehicleMapper.lockRouteTotalSlots(routeNo)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "노선을 찾을 수 없습니다."));

        if (slotNo != null) {
            if (slotNo > totalSlots) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "슬롯 번호는 1 ~ " + totalSlots + " 사이로 입력해 주세요.");
            }
            vehicleMapper.findVehicleNoBySlot(routeNo, slotNo)
                    .filter(owner -> !owner.equals(vehicleNo)) // 자기 자리 그대로면 통과
                    .ifPresent(owner -> {
                        throw new BusinessException(HttpStatus.CONFLICT,
                                slotNo + "번 슬롯은 이미 " + owner + " 차량이 사용 중입니다.");
                    });
        }

        // 다른 노선(또는 예비)에서 들어올 때만 대수가 늘어난다
        if (!routeNo.equals(currentRouteNo)) {
            int assigned = vehicleMapper.countByRoute(routeNo);
            if (assigned >= totalSlots) {
                throw new BusinessException(HttpStatus.CONFLICT,
                        "노선이 가득 찼습니다(" + assigned + "/" + totalSlots + "). 총 슬롯 수를 늘리거나 다른 차량을 빼 주세요.");
            }
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}