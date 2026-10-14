export type Shift = 'MORNING' | 'AFTERNOON' | 'OFF'

export const shiftLabel: Record<Shift, string> = {
  MORNING: '오전',
  AFTERNOON: '오후',
  OFF: '휴무',
}

/** 월 근무표 칸에 쓰는 짧은 글자 */
export const shiftShortLabel: Record<Shift, string> = {
  MORNING: '전',
  AFTERNOON: '후',
  OFF: '휴',
}

/** 근무별 칸 색 */
export const shiftCellClass: Record<Shift, string> = {
  MORNING: 'bg-sky-100 text-sky-800',
  AFTERNOON: 'bg-amber-100 text-amber-800',
  OFF: 'bg-rose-100 text-rose-700',
}

export interface DutyDriver {
  name: string
  seniorityNo: number
  latestBaseDate: string | null
  latestCycleDay: number | null
}

export interface DutyStartItem {
  employeeName: string
  cycleDay: number
}

export interface DutyStartBulkRequest {
  baseDate: string
  items: DutyStartItem[]
}

/**
 * shift 가 null 이면 근무표 미설정. overridden 이면 수동 수정한 칸.
 * cycleDay 는 시작점으로 계산한 그날의 주기 일차, startChanged 면 이날부터 새 시작점
 */
export interface DutyCell {
  shift: Shift | null
  overridden: boolean
  cycleDay: number | null
  startChanged: boolean
}

export interface DutyRosterRow {
  name: string
  seniorityNo: number
  cells: DutyCell[]
}

export interface DutyRoster {
  year: number
  month: number
  lastDay: number
  rows: DutyRosterRow[]
}

/**
 * 주기 일차(1~12)의 근무. 입력 화면의 미리보기용.
 * 실제 근무표는 서버(DutyPattern)가 계산하므로, 패턴이 바뀌면 두 곳을 함께 고친다.
 */
export function shiftOfCycleDay(cycleDay: number): Shift {
  if (cycleDay === 6 || cycleDay === 12) {
    return 'OFF'
  }
  return cycleDay <= 5 ? 'MORNING' : 'AFTERNOON'
}

/** superseded 면 나중 입력에 덮어써짐, 아니면 baseDate ~ appliedUntil 동안 적용 (null 이면 계속) */
export interface DutyStartHistory {
  id: number
  baseDate: string
  cycleDay: number
  createdByName: string | null
  createdAt: string
  superseded: boolean
  appliedUntil: string | null
}
