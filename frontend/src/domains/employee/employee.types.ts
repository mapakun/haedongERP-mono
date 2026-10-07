import type { Role } from '@/domains/auth/auth.types'

export type JobType = 'DRIVER' | 'OFFICE'

export const jobTypeLabel: Record<JobType, string> = {
  DRIVER: '기사',
  OFFICE: '사무',
}

export interface EmployeeSummary {
  id: number
  name: string
  jobType: JobType
  birthDate: string | null
  mobile: string | null
  seniorityNo: number | null
  role: Role
  enabled: boolean
  hasAccount: boolean
}

export interface EmployeeSearchParams {
  keyword?: string
  jobType?: JobType
  page?: number
  size?: number
}
