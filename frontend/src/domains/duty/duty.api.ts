import { http } from '@/shared/api/http'
import type { DutyDriver, DutyRoster, DutyStartBulkRequest } from './duty.types'

export async function fetchDutyDrivers(): Promise<DutyDriver[]> {
  const { data } = await http.get<DutyDriver[]>('/duty/drivers')
  return data
}

/** 시작점 일괄 입력. 저장한 인원 수를 돌려준다 */
export async function createDutyStarts(request: DutyStartBulkRequest): Promise<number> {
  const { data } = await http.post<{ count: number }>('/duty/starts', request)
  return data.count
}

export async function fetchDutyRoster(year: number, month: number): Promise<DutyRoster> {
  const { data } = await http.get<DutyRoster>('/duty/roster', { params: { year, month } })
  return data
}
