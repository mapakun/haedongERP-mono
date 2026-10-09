import axios from 'axios'

export const http = axios.create({
  baseURL: '/api',
})

let unauthorizedHandler: (() => void) | null = null

/** 로그인이 끊긴 상태(401)로 API를 호출했을 때 실행할 함수를 등록한다 */
export function setUnauthorizedHandler(handler: () => void) {
  unauthorizedHandler = handler
}

// 401이 정상인 요청: 로그인 실패, 처음 접속 시 "로그인했나?" 확인
const IGNORE_401_URLS = ['/auth/login', '/auth/me']

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const url: string = error.config?.url ?? ''
    if (error.response?.status === 401 && !IGNORE_401_URLS.includes(url)) {
      unauthorizedHandler?.()
    }
    return Promise.reject(error)
  },
)
