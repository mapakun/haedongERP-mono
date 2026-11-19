import { http } from '@/shared/api/http'
import type { RouteRoster, RouteRosterInitResult, RouteRosterSaveItem } from './routeroster.types'

export async function fetchRouteRoster(year: number, month: number, routeNo: string): Promise<RouteRoster> {
  const { data } = await http.get<RouteRoster>('/route-rosters', { params: { year, month, routeNo } })
  return data
}

/** 한 노선·한 달의 배치를 통째로 저장한다. 빈 배열이면 그달 배치를 비운다 */
export async function saveRouteRoster(
  year: number,
  month: number,
  routeNo: string,
  rows: RouteRosterSaveItem[],
): Promise<void> {
  await http.put('/route-rosters', { rows }, { params: { year, month, routeNo } })
}

export async function copyPreviousRouteRoster(
  year: number,
  month: number,
  routeNo: string,
): Promise<RouteRosterInitResult> {
  const { data } = await http.post<RouteRosterInitResult>('/route-rosters/copy-previous', null, {
    params: { year, month, routeNo },
  })
  return data
}

export async function createRouteRosterFromVehicles(
  year: number,
  month: number,
  routeNo: string,
): Promise<RouteRosterInitResult> {
  const { data } = await http.post<RouteRosterInitResult>('/route-rosters/from-vehicles', null, {
    params: { year, month, routeNo },
  })
  return data
}
