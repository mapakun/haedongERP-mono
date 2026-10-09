import { http } from '@/shared/api/http'
import type { LoginRequest, LoginUser, MyPasswordChangeRequest } from './auth.types'

export async function login(request: LoginRequest): Promise<LoginUser> {
  // 로그아웃 직후처럼 CSRF 토큰 쿠키가 없을 수 있어서, 먼저 받아 둔다
  await http.get('/auth/csrf')
  const { data } = await http.post<LoginUser>('/auth/login', request)
  return data
}

export async function fetchMe(): Promise<LoginUser> {
  const { data } = await http.get<LoginUser>('/auth/me')
  return data
}

export async function logout(): Promise<void> {
  await http.post('/auth/logout')
}

export async function changeMyPassword(request: MyPasswordChangeRequest): Promise<void> {
  await http.put('/auth/me/password', request)
}
