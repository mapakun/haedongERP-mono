export interface PageResponse<T> {
  items: T[]
  totalCount: number
  page: number
  size: number
}
