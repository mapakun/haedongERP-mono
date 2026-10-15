package com.haedong.erp.domains.vehicle;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.haedong.erp.domains.vehicle.dto.VehicleResponse;

@Mapper
public interface VehicleMapper {

    /** routeNo 가 있으면 그 노선만, spareOnly 가 true 면 예비 차량(노선 없음)만 */
    List<VehicleResponse> findAll(@Param("routeNo") String routeNo,
                                  @Param("spareOnly") boolean spareOnly);

    Optional<VehicleResponse> findByVehicleNo(String vehicleNo);

    boolean existsByVehicleNo(String vehicleNo);

    /** 노선 행을 잠그고 총 슬롯 수를 돌려준다. 노선이 없으면 빈 값 */
    Optional<Integer> lockRouteTotalSlots(String routeNo);

    int countByRoute(String routeNo);

    /** 이 노선의 이 슬롯을 쓰고 있는 차량 번호 (없으면 빈 값) */
    Optional<String> findVehicleNoBySlot(@Param("routeNo") String routeNo,
                                         @Param("slotNo") int slotNo);

    void insert(@Param("vehicleNo") String vehicleNo,
                @Param("routeNo") String routeNo,
                @Param("slotNo") Integer slotNo);

    void updateAssignment(@Param("vehicleNo") String vehicleNo,
                          @Param("routeNo") String routeNo,
                          @Param("slotNo") Integer slotNo);

    /** 삭제된 행 수 (0 이면 없는 차량) */
    int delete(String vehicleNo);
}