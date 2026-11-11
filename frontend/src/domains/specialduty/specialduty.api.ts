import { http } from '@/shared/api/http'
import type { DayType } from '@/domains/timetable/timetable.types'
import type {
  NightRule,
  NightSlotResponse,
  NightSlotSaveItem,
  NightTrip,
  ShiftSlotResponse,
} from './specialduty.types'

const BASE = '/special-duty'

// ========== 단축 ==========

export async function getShiftSlots(routeNo: string): Promise<ShiftSlotResponse> {
  const { data } = await http.get<ShiftSlotResponse>(`${BASE}/shift-slots`, { params: { routeNo } })
  return data
}

/** 노선의 단축 가능 순번을 통째로 저장한다. 목록에 없는 순번은 꺼진다 */
export async function saveShiftSlots(routeNo: string, slotNos: number[]): Promise<void> {
  await http.put(`${BASE}/shift-slots/${routeNo}`, { slotNos })
}

// ========== 심야 시간표 ==========

export async function getNightTrips(): Promise<NightTrip[]> {
  const { data } = await http.get<NightTrip[]>(`${BASE}/night-trips`)
  return data
}

export async function saveNightTrips(trips: NightTrip[]): Promise<void> {
  await http.put(`${BASE}/night-trips`, { trips })
}

// ========== 노선별 최소 휴식 ==========

export async function getNightRules(): Promise<NightRule[]> {
  const { data } = await http.get<NightRule[]>(`${BASE}/night-rules`)
  return data
}

export async function saveNightRule(routeNo: string, minRestMin: number): Promise<void> {
  await http.put(`${BASE}/night-rules/${routeNo}`, { minRestMin })
}

// ========== 심야 가능 순번 ==========

/** 이 노선이 시간표를 가진 형태들 (토요일 시간표가 없는 노선은 토요일이 빠져 있다) */
export async function getDayTypes(routeNo: string): Promise<DayType[]> {
  const { data } = await http.get<DayType[]>(`${BASE}/day-types`, { params: { routeNo } })
  return data
}

export async function getNightSlots(routeNo: string, dayType: DayType): Promise<NightSlotResponse> {
  const { data } = await http.get<NightSlotResponse>(`${BASE}/night-slots`, {
    params: { routeNo, dayType },
  })
  return data
}

/** 노선·형태의 심야 가능 순번을 통째로 저장한다 */
export async function saveNightSlots(
  routeNo: string,
  dayType: DayType,
  slots: NightSlotSaveItem[],
): Promise<void> {
  await http.put(`${BASE}/night-slots/${routeNo}/${dayType}`, { slots })
}
