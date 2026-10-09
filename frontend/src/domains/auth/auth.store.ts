import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from './auth.api'
import type { LoginRequest, LoginUser } from './auth.types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<LoginUser | null>(null)
  const checked = ref(false)

  const isLoggedIn = computed(() => user.value !== null)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function loadMe() {
    try {
      user.value = await authApi.fetchMe()
    } catch {
      user.value = null
    } finally {
      checked.value = true
    }
  }

  async function login(request: LoginRequest) {
    user.value = await authApi.login(request)
    checked.value = true
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      user.value = null
    }
  }

  function clear() {
    user.value = null
  }

  return { user, checked, isLoggedIn, isAdmin, loadMe, login, logout, clear }
})
