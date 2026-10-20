import { http } from '@/shared/api/http'
import type {
  RoutePoint,
  TimetableCreateRequest,
  TimetableKey,
  TimetableRow,
  TimetableSearchParams,
  TimetableUpdateRequest,
} from './timetable.types'

/** 노선+형태는 필수. 오전/오후와 순번을 주면 그 기사가 탈 회차만 돌아온다 */
export async function getTimetable(params: TimetableSearchParams): Promise<TimetableRow[]> {
  const { data } = await http.get<TimetableRow[]>('/timetables', { params })
  return data
}

/** 노선의 장소 이름 (출발, 중간지, 회차, 도착). 등록된 것만 돌아온다 */
export async function getRoutePoints(routeNo: string): Promise<RoutePoint[]> {
  const { data } = await http.get<RoutePoint[]>(`/route-points/${routeNo}`)
  return data
}

export async function createTimetableRow(request: TimetableCreateRequest): Promise<void> {
  await http.post('/timetables', request)
}

export async function updateTimetableRow(
  key: TimetableKey,
  request: TimetableUpdateRequest,
): Promise<void> {
  await http.put(`/timetables/${key.routeNo}/${key.dayType}/${key.slotNo}/${key.tripNo}`, request)
}

export async function deleteTimetableRow(key: TimetableKey): Promise<void> {
  await http.delete(`/timetables/${key.routeNo}/${key.dayType}/${key.slotNo}/${key.tripNo}`)
}
