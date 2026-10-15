import { http } from '@/shared/api/http'
import type { RouteCreateRequest, RouteSummary } from './route.types'

export async function getRoutes(): Promise<RouteSummary[]> {
  const { data } = await http.get<RouteSummary[]>('/routes')
  return data
}

export async function createRoute(request: RouteCreateRequest): Promise<string> {
  const { data } = await http.post<{ routeNo: string }>('/routes', request)
  return data.routeNo
}

export async function updateRouteSlots(routeNo: string, totalSlots: number): Promise<void> {
  await http.put(`/routes/${routeNo}`, { totalSlots })
}

export async function deleteRoute(routeNo: string): Promise<void> {
  await http.delete(`/routes/${routeNo}`)
}
