/** 오늘 날짜를 'YYYY-MM-DD' 형식으로 (브라우저의 현지 시간 기준) */
export function todayString(): string {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
