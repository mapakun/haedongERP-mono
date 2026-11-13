import { http } from '@/shared/api/http'
import type { MissedTripPage, MissedTripSaveRequest, MissedTripSearchParams } from './missedtrip.types'

const BASE = '/missed-trips'

export async function searchMissedTrips(params: MissedTripSearchParams): Promise<MissedTripPage> {
  const { data } = await http.get<MissedTripPage>(BASE, { params })
  return data
}

export async function createMissedTrip(request: MissedTripSaveRequest): Promise<void> {
  await http.post(BASE, request)
}

export async function updateMissedTrip(id: number, request: MissedTripSaveRequest): Promise<void> {
  await http.put(`${BASE}/${id}`, request)
}

export async function deleteMissedTrip(id: number): Promise<void> {
  await http.delete(`${BASE}/${id}`)
}

/** 엑셀 파일을 받아서 브라우저 다운로드로 저장한다 (관리자만) */
export async function downloadMissedTripExcel(params: MissedTripSearchParams): Promise<void> {
  const response = await http.get<Blob>(`${BASE}/export`, { params, responseType: 'blob' })

  const url = URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = url
  link.download = `결행목록_${params.fromDate}_${params.toDate}.xlsx`
  link.click()
  URL.revokeObjectURL(url)
}
