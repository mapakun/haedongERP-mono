import { http } from '@/shared/api/http'
import type { LoginRequest, LoginUser, MyPasswordChangeRequest } from './auth.types'

export async function login(request: LoginRequest): Promise<LoginUser> {
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
