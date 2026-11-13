import { http } from '@/shared/api/http'
import type {
  ComfortContract,
  ComfortLeaveResponse,
  ContractCreateRequest,
  DriverMember,
  MemberSaveRequest,
  UseCreateRequest,
} from './comfortleave.types'

const BASE = '/comfort-leave'

export async function getComfortLeave(year: number): Promise<ComfortLeaveResponse> {
  const { data } = await http.get<ComfortLeaveResponse>(BASE, { params: { year } })
  return data
}

// ========== 사용 기록 ==========

export async function addComfortUse(request: UseCreateRequest): Promise<void> {
  await http.post(`${BASE}/uses`, request)
}

export async function deleteComfortUse(id: number): Promise<void> {
  await http.delete(`${BASE}/uses/${id}`)
}

// ========== 대상자 관리 ==========

export async function getDriverMembers(): Promise<DriverMember[]> {
  const { data } = await http.get<DriverMember[]>(`${BASE}/members`)
  return data
}

export async function saveMember(employeeId: number, request: MemberSaveRequest): Promise<void> {
  await http.put(`${BASE}/members/${employeeId}`, request)
}

// ========== 촉탁 계약 ==========
export async function getContracts(employeeId: number): Promise<ComfortContract[]> {
  const { data } = await http.get<ComfortContract[]>(`${BASE}/members/${employeeId}/contracts`)
  return data
}

export async function addContract(employeeId: number, request: ContractCreateRequest): Promise<void> {
  await http.post(`${BASE}/members/${employeeId}/contracts`, request)
}

export async function deleteContract(employeeId: number, startDate: string): Promise<void> {
  await http.delete(`${BASE}/members/${employeeId}/contracts/${startDate}`)
}
