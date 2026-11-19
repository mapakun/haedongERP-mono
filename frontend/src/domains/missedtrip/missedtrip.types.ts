/** 결행 사유 (백엔드 MissedReason) */
export type MissedReason = 'TRAFFIC' | 'BREAKDOWN' | 'ACCIDENT' | 'SICK' | 'ETC'

export const reasonLabel: Record<MissedReason, string> = {
  TRAFFIC: '체증',
  BREAKDOWN: '고장',
  ACCIDENT: '사고',
  SICK: '병가',
  ETC: '기타',
}

export const reasonOptions = (Object.keys(reasonLabel) as MissedReason[]).map((value) => ({
  value,
  label: reasonLabel[value],
}))

/** 목록 한 줄 (백엔드 MissedTripRow) */
export interface MissedTrip {
  id: number
  missedOn: string
  routeNo: string
  vehicleNo: string
  tripLabel: string
  driverName: string | null
  missedCount: number
  reason: MissedReason
  note: string | null
  createdBy: string
}

/** 목록 응답: 한 페이지 + 조건 전체의 건수·결행 횟수 합계 (백엔드 MissedTripPageResponse) */
export interface MissedTripPage {
  items: MissedTrip[]
  totalCount: number
  missedSum: number
  page: number
  size: number
}

/** 목록·내보내기 조회 조건. 비어 있는 값은 보내지 않는다 */
export interface MissedTripSearchParams {
  fromDate?: string
  toDate?: string
  routeNo?: string
  reason?: MissedReason
  keyword?: string
  page?: number
  size?: number
}

/** 입력·수정 요청 (백엔드 MissedTripSaveRequest) */
export interface MissedTripSaveRequest {
  missedOn: string
  routeNo: string
  vehicleNo: string
  tripLabel: string
  driverName: string | null
  missedCount: number
  reason: MissedReason
  note: string | null
}

/** 통계를 무엇을 기준으로 묶을지 (백엔드 MissedTripStatGroup) */
export type StatGroup = 'REASON' | 'ROUTE' | 'MONTH' | 'VEHICLE' | 'DRIVER' | 'KEYWORD'

export const statGroupOptions: { value: StatGroup; label: string }[] = [
  { value: 'REASON', label: '사유별' },
  { value: 'ROUTE', label: '노선별' },
  { value: 'MONTH', label: '월별' },
  { value: 'VEHICLE', label: '차량별 (상위 20)' },
  { value: 'DRIVER', label: '승무원별 (상위 20)' },
  { value: 'KEYWORD', label: '비고 단어별' },
]

export interface MissedTripStatParams {
  fromDate: string
  toDate: string
  group: StatGroup
  routeNo?: string
  reason?: MissedReason
  /** 비고 단어별일 때 쉼표로 구분한 단어들 */
  keywords?: string
}

/** 통계 한 줄 (백엔드 MissedTripStatRow) */
export interface MissedTripStatRow {
  label: string | null
  tripCount: number
  missedSum: number
}

export interface MissedTripStatResponse {
  rows: MissedTripStatRow[]
  totalCount: number
  missedSum: number
}
