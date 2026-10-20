/** 시간표 형태 (백엔드 DayType) */
export type DayType = 'WEEKDAY' | 'HOLIDAY' | 'SATURDAY' | 'VACATION'

/** 오전/오후 (백엔드 TripShift) */
export type TripShift = 'MORNING' | 'AFTERNOON'

/** 노선 위의 위치 (백엔드 PointKey). 노선을 따라가는 순서대로 나열했다 */
export type PointKey = 'START' | 'MID1' | 'MID2' | 'TURN' | 'MID3' | 'MID4' | 'END'

export const dayTypeLabel: Record<DayType, string> = {
  WEEKDAY: '평일',
  HOLIDAY: '공휴일(일요일 포함)',
  SATURDAY: '토요일',
  VACATION: '방학',
}

export const tripShiftLabel: Record<TripShift, string> = {
  MORNING: '오전',
  AFTERNOON: '오후',
}

/** 장소 이름이 아직 없을 때 열 제목으로 쓰는 기본 이름 */
export const pointDefaultLabel: Record<PointKey, string> = {
  START: '출발',
  MID1: '중간지1',
  MID2: '중간지2',
  TURN: '회차',
  MID3: '중간지3',
  MID4: '중간지4',
  END: '도착',
}

/** 시간표 한 회차 (백엔드 TimetableRowResponse). 시각은 0시부터의 분이고, 없는 중간지는 null */
export interface TimetableRow {
  routeNo: string
  dayType: DayType
  slotNo: number
  tripNo: number
  shift: TripShift
  departMin: number
  mid1Min: number | null
  mid2Min: number | null
  turnMin: number
  mid3Min: number | null
  mid4Min: number | null
  arriveMin: number
}

/** 시각 열 하나: 어떤 위치의 이름표인지와 그 값을 꺼내는 속성 이름 */
export interface TimeColumn {
  pointKey: PointKey
  field: 'departMin' | 'mid1Min' | 'mid2Min' | 'turnMin' | 'mid3Min' | 'mid4Min' | 'arriveMin'
  /** 출발·회차·도착은 값이 항상 있어서 비어 있어도 열을 숨기지 않는다 */
  required: boolean
}

/** 시간표의 시각 열 7개 (노선을 따라가는 순서) */
export const timeColumns: TimeColumn[] = [
  { pointKey: 'START', field: 'departMin', required: true },
  { pointKey: 'MID1', field: 'mid1Min', required: false },
  { pointKey: 'MID2', field: 'mid2Min', required: false },
  { pointKey: 'TURN', field: 'turnMin', required: true },
  { pointKey: 'MID3', field: 'mid3Min', required: false },
  { pointKey: 'MID4', field: 'mid4Min', required: false },
  { pointKey: 'END', field: 'arriveMin', required: true },
]

/** 노선의 장소 이름 한 개 (백엔드 RoutePointResponse) */
export interface RoutePoint {
  pointKey: PointKey
  name: string
}

export interface TimetableSearchParams {
  routeNo: string
  dayType: DayType
  shift?: TripShift
  slotNo?: number
}
