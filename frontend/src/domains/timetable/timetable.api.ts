import { http } from '@/shared/api/http'
import type {
  RoutePoint,
  TimetableCopyRequest,
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

/** 같은 노선 안에서 한 형태의 시간표를 다른 형태로 복사하고, 복사된 행 수를 돌려준다 */
export async function copyTimetable(request: TimetableCopyRequest): Promise<number> {
  const { data } = await http.post<{ copied: number }>('/timetables/copy', request)
  return data.copied
}

/** 노선의 장소 이름을 통째로 저장한다. 이름이 비어 있는 위치는 지워진다 */
export async function saveRoutePoints(routeNo: string, points: RoutePoint[]): Promise<void> {
  await http.put(`/route-points/${routeNo}`, { points })
}
