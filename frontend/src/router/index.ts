import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/domains/auth/auth.store'
import { authRoutes } from '@/domains/auth/auth.routes'
import { mainRoutes } from '@/domains/main/main.routes'
import AppLayout from '@/layout/AppLayout.vue'
import { employeeRoutes } from '@/domains/employee/employee.routes'
import { dutyRoutes } from '@/domains/duty/duty.routes'
import { routeRoutes } from '@/domains/route/route.routes'
import { vehicleRoutes } from '@/domains/vehicle/vehicle.routes'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [...authRoutes,
    {
      path: '/',
      component: AppLayout,
      children: [...mainRoutes, ...employeeRoutes, ...dutyRoutes, ...routeRoutes, ...vehicleRoutes],
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

  if (!auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  // 관리자 전용 화면: 일반 사용자는 홈으로
  if (to.meta.adminOnly && !auth.isAdmin) {
    return { name: 'main' }
  }

  return true
})

export default router
