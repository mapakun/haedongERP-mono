/** 차량 목록 한 줄 (백엔드 VehicleResponse) */
export interface VehicleSummary {
  vehicleNo: string
  /** 소속 노선 (null 이면 예비 차량) */
  routeNo: string | null
  /** 노선 안 슬롯 번호 (아직 배정 전이면 null) */
  slotNo: number | null
}

/** 목록 조회 조건 (백엔드 VehicleController.findAll 의 쿼리 파라미터) */
export interface VehicleSearchParams {
  routeNo?: string
  spareOnly?: boolean
}

/** 차량 등록 요청 (백엔드 VehicleCreateRequest) */
export interface VehicleCreateRequest {
  vehicleNo: string
  routeNo: string | null
  slotNo: number | null
}

/** 노선·슬롯 배정 변경 요청 (백엔드 VehicleAssignRequest). routeNo 가 null 이면 예비 차량 */
export interface VehicleAssignRequest {
  routeNo: string | null
  slotNo: number | null
}
