export type Role = 'ADMIN' | 'USER'

export interface LoginUser {
  id: number
  loginId: string
  name: string
  role: Role
}

export interface LoginRequest {
  loginId: string
  password: string
}
