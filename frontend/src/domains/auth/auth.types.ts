export type Role = 'MASTER' | 'ADMIN' | 'USER'

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
  MASTER: '최고 관리자',
  ADMIN: '관리자',
  USER: '일반',
}

export interface MyPasswordChangeRequest {
  currentPassword: string
  newPassword: string
}
