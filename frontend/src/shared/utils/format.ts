export function formatMobile(mobile: string | null): string {
  if (!mobile) {
    return '-'
  }
  const digits = mobile.replace(/\D/g, '')
  if (digits.length === 11) {
    return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
  }
  if (digits.length === 10) {
    return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`
  }
  return mobile
}


/** 0시부터의 분을 "HH:MM"으로 바꾼다. 자정을 넘는 시각은 24:10처럼 24시 이후로 표시한다 */
export function formatMinutes(minutes: number | null): string {
  if (minutes === null) {
    return '-'
  }
  const hour = Math.floor(minutes / 60)
  const minute = minutes % 60
  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
}
