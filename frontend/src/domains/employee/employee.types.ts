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
  retiredAt: string | null
}

/** 일반 사용자가 받는 직원 목록 한 줄 (백엔드 EmployeeBriefResponse) */
export interface EmployeeBrief {
  id: number
  name: string
  jobType: JobType
  seniorityNo: number | null
}

export interface EmployeeSearchParams {
  keyword?: string
  jobType?: JobType
  page?: number
  size?: number
  includeRetired?: boolean
}

/** 상세 응답 = 목록 항목 + 잠금 정보 (백엔드 EmployeeDetailResponse) */
export interface EmployeeDetail extends EmployeeSummary {
  locked: boolean
  lockedUntil: string | null
}

export interface EmployeeSaveRequest {
  name: string
  jobType: JobType
  birthDate: string | null
  mobile: string | null
  seniorityNo: number | null
}

/** 퇴사 확인창에 넘기는 대상 직원 정보 */
export interface RetireTarget {
  id: number
  name: string
  seniorityNo: number | null
}

export interface AccountIssueRequest {
  password: string
  role: Role
}
