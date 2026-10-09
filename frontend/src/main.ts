import '@/shared/styles/base.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { setUnauthorizedHandler } from '@/shared/api/http'
import { useAuthStore } from '@/domains/auth/auth.store'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 로그인이 끊긴 상태로 API를 호출하면 로그인 화면으로 보낸다
setUnauthorizedHandler(() => {
  const auth = useAuthStore()
  if (!auth.isLoggedIn) {
    return
  }
  auth.clear()
  router.push({
    name: 'login',
    query: { expired: '1', redirect: router.currentRoute.value.fullPath },
  })
})

app.mount('#app')
