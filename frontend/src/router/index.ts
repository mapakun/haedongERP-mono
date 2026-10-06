import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/domains/auth/auth.store'
import { authRoutes } from '@/domains/auth/auth.routes'
import { mainRoutes } from '@/domains/main/main.routes'
import AppLayout from '@/layout/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [...authRoutes,
    {
      path: '/',
      component: AppLayout,
      children: [...mainRoutes],
    },
    ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (!auth.checked) {
    await auth.loadMe()
  }

  if (to.meta.public) {
    return auth.isLoggedIn && to.name === 'login' ? { name: 'main' } : true
  }

  return auth.isLoggedIn ? true : { name: 'login' }
})

export default router
