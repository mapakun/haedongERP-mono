import type { DayType } from '@/domains/timetable/timetable.types'

/** 단축 순번 화면의 한 칸 (백엔드 ShiftSlotRow) */
export interface ShiftSlotItem {
  slotNo: number
  /** 단축 가능으로 켜져 있는지 */
  enabled: boolean
  inWeekday: boolean
  /** 토요일 시간표에 있는지 (토요일 시간표가 없는 노선은 공휴일 시간표 기준) */
  inSaturday: boolean
  /** 일요일·공휴일 시간표에 있는지 */
  inHoliday: boolean
  /** 평일 시간표에서 이 순번의 오전 회차 수 */
  weekdayAmTrips: number
}

/** 단축 순번 조회 응답 (백엔드 ShiftSlotResponse) */
export interface ShiftSlotResponse {
  routeNo: string
  totalSlots: number
  slots: ShiftSlotItem[]
}

/** 심야 시간표 한 줄 (백엔드 NightTripRow). 출발 시각은 0시부터의 분 */
export interface NightTrip {
  nightNo: number
  departMin: number
}

/** 노선별 최소 휴식 시간 (백엔드 NightRuleRow) */
export interface NightRule {
  routeNo: string
  minRestMin: number
}

/** 심야 가능 순번 화면의 한 줄: 순번, 마지막 도착, 체크된 심야 순번 (백엔드 NightSlotResponse.Slot) */
export interface NightSlotItem {
  slotNo: number
  lastArriveMin: number
  nightNos: number[]
}

/** 심야 가능 순번 조회 응답 (백엔드 NightSlotResponse) */
export interface NightSlotResponse {
  routeNo: string
  dayType: DayType
  /** 이 노선의 최소 휴식 시간 (아직 기준이 없으면 null) */
  minRestMin: number | null
  nights: NightTrip[]
  slots: NightSlotItem[]
}

/** 심야 가능 순번 저장 한 줄 (백엔드 NightSlotSaveRequest.Item) */
export interface NightSlotSaveItem {
  slotNo: number
  nightNos: number[]
}
