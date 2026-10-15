/** 노선 목록 한 줄 (백엔드 RouteResponse) */
export interface RouteSummary {
  routeNo: string
  totalSlots: number
  /** 이 노선에 배정된 차량 수 (슬롯 번호가 비어 있는 차량도 포함) */
  assignedCount: number
}

/** 노선 등록 요청 (백엔드 RouteCreateRequest) */
export interface RouteCreateRequest {
  routeNo: string
  totalSlots: number
}
