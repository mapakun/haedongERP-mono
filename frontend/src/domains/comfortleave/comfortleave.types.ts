/** 고용 형태 (백엔드 EmploymentType) */
export type EmploymentType = 'REGULAR' | 'CONTRACT'

export const employmentTypeLabel: Record<EmploymentType, string> = {
  REGULAR: '정규직',
  CONTRACT: '촉탁직',
}

/** 사용 기록 한 건 (백엔드 ComfortUseRow) */
export interface ComfortUse {
  id: number
  employeeId: number
  usedOn: string
  memo: string | null
  /** 기록한 관리자 이름 (엑셀 이관분은 "엑셀 이관") */
  createdBy: string | null
}

/** 정규직 표 한 줄 (백엔드 RegularLeaveRow) */
export interface RegularLeave {
  employeeId: number
  name: string
  hireDate: string | null
  retiredAt: string | null
  granted: number
  used: number
  /** 발생 - 사용 (미리 쓰면 음수) */
  remaining: number
  uses: ComfortUse[]
}

/** 촉탁직 표 한 줄 = 계약 한 건 (백엔드 ContractLeaveRow) */
export interface ContractLeave {
  employeeId: number
  name: string
  startDate: string
  endDate: string
  retiredAt: string | null
  granted: number
  used: number
  remaining: number
  uses: ComfortUse[]
}

/** 정규직 월말 누계 한 줄 (백엔드 MonthlySummaryRow) */
export interface MonthlySummary {
  month: number
  first: number
  second: number
  third: number
  fourth: number
  total: number
}

/** 연도별 현황 (백엔드 ComfortLeaveResponse) */
export interface ComfortLeaveResponse {
  year: number
  /** 촉탁직 발생 개수의 기준일 */
  asOf: string
  regulars: RegularLeave[]
  contracts: ContractLeave[]
  monthly: MonthlySummary[]
}

/** 대상자 관리 목록 한 줄 (백엔드 DriverMemberRow). 미등록이면 고용 형태·입사일자가 null */
export interface DriverMember {
  employeeId: number
  name: string
  retiredAt: string | null
  employmentType: EmploymentType | null
  hireDate: string | null
  note: string | null
}

/** 촉탁 계약 한 건 (백엔드 ComfortContractRow) */
export interface ComfortContract {
  employeeId: number
  startDate: string
  endDate: string
}

export interface UseCreateRequest {
  employeeId: number
  usedOn: string
  memo: string | null
}

export interface MemberSaveRequest {
  employmentType: EmploymentType
  hireDate: string | null
  note: string | null
}

export interface ContractCreateRequest {
  startDate: string
  /** 비우면 서버가 시작일 + 1년 - 1일로 정한다 */
  endDate: string | null
}

/** 사용 기록 추가 창에 넘기는 대상 (촉탁직은 계약 기간 안의 날짜만 고를 수 있다) */
export interface UseTarget {
  employeeId: number
  name: string
  periodLabel: string
  minDate?: string
  maxDate?: string
}
