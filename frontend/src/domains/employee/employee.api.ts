import { http } from '@/shared/api/http'
import type { PageResponse } from '@/shared/api/page'
import type { EmployeeSearchParams, EmployeeSummary } from './employee.types'

export async function searchEmployees(
  params: EmployeeSearchParams,
): Promise<PageResponse<EmployeeSummary>> {
  const { data } = await http.get<PageResponse<EmployeeSummary>>('/employees', { params })
  return data
}
