import { http } from '@/shared/api/http'
import type {
  VehicleAssignRequest,
  VehicleCreateRequest,
  VehicleSearchParams,
  VehicleSummary,
} from './vehicle.types'

export async function getVehicles(params: VehicleSearchParams): Promise<VehicleSummary[]> {
  const { data } = await http.get<VehicleSummary[]>('/vehicles', { params })
  return data
}

export async function createVehicle(request: VehicleCreateRequest): Promise<string> {
  const { data } = await http.post<{ vehicleNo: string }>('/vehicles', request)
  return data.vehicleNo
}

/** 노선·슬롯 배정 변경 */
export async function assignVehicle(
  vehicleNo: string,
  request: VehicleAssignRequest,
): Promise<void> {
  await http.put(`/vehicles/${vehicleNo}`, request)
}

export async function deleteVehicle(vehicleNo: string): Promise<void> {
  await http.delete(`/vehicles/${vehicleNo}`)
}
