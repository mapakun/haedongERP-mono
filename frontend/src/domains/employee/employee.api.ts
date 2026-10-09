import { http } from '@/shared/api/http'
import type { PageResponse } from '@/shared/api/page'
import type {
  EmployeeBrief,
  EmployeeDetail,
  EmployeeSaveRequest,
  EmployeeSearchParams,
  EmployeeSummary,
} from './employee.types'

export async function searchEmployees(
  params: EmployeeSearchParams,
): Promise<PageResponse<EmployeeSummary>> {
  const { data } = await http.get<PageResponse<EmployeeSummary>>('/employees', { params })
  return data
}

/** 일반 사용자용: 같은 주소지만 서버가 이름·직종·서열만 돌려준다 */
export async function searchEmployeeBriefs(
  params: EmployeeSearchParams,
): Promise<PageResponse<EmployeeBrief>> {
  const { data } = await http.get<PageResponse<EmployeeBrief>>('/employees', { params })
  return data
}

export async function getEmployee(id: number): Promise<EmployeeDetail> {
  const { data } = await http.get<EmployeeDetail>(`/employees/${id}`)
  return data
}

export async function createEmployee(request: EmployeeSaveRequest): Promise<number> {
  const { data } = await http.post<{ id: number }>('/employees', request)
  return data.id
}

export async function updateEmployee(id: number, request: EmployeeSaveRequest): Promise<void> {
  await http.put(`/employees/${id}`, request)
}

export async function retireEmployee(id: number, retiredAt: string): Promise<void> {
  await http.post(`/employees/${id}/retire`, { retiredAt })
}

export async function cancelRetirement(id: number): Promise<void> {
  await http.post(`/employees/${id}/cancel-retirement`)
}
