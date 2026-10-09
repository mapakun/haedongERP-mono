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

export const roleLabel: Record<Role, string> = {
  ADMIN: '관리자',
  USER: '일반',
}

export interface MyPasswordChangeRequest {
  currentPassword: string
  newPassword: string
}
