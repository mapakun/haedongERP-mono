import type { DutyCell } from '@/domains/duty/duty.types'

/** 노선 근무표 한 줄 (백엔드 RouteRosterRow). employeeName 이 null 이면 빈자리 */
export interface RouteRosterRow {
  sortOrder: number
  slotNo: number | null
  vehicleNo: string | null
  abLabel: 'A' | 'B' | null
  employeeName: string | null
  /** 지금 재직 중인 전속기사인지 (false 면 퇴사·서열 없음) */
  activeDriver: boolean
  /** 1일~말일 근무. 근무표 메뉴와 같은 계산. 빈자리면 빈 배열 */
  cells: DutyCell[]
}

/** 같은 달 다른 노선에 배치된 기사 (백엔드 RouteRosterAssignment) */
export interface RouteRosterAssignment {
  employeeName: string
  routeNo: string
}

/** 한 노선의 월 근무표 (백엔드 RouteRosterResponse). rows 가 비면 그달 배치가 아직 없음 */
export interface RouteRoster {
  year: number
  month: number
  lastDay: number
  routeNo: string
  rows: RouteRosterRow[]
  otherAssignments: RouteRosterAssignment[]
}

/** 저장할 한 줄 (백엔드 RouteRosterSaveRequest.Item). 목록 순서가 곧 출력 순서 */
export interface RouteRosterSaveItem {
  slotNo: number | null
  vehicleNo: string | null
  abLabel: 'A' | 'B' | null
  employeeName: string | null
}

/** 전달 복사 / 차량 순번으로 만들기 결과 (백엔드 RouteRosterInitResponse) */
export interface RouteRosterInitResult {
  count: number
  /** 복사하면서 빈자리로 바꾼 기사 (퇴사·서열 없음, 또는 이번 달 다른 노선에 이미 배치됨) */
  clearedNames: string[]
}
