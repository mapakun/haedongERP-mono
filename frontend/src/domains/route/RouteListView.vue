<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/domains/auth/auth.store'
import RouteFormModal from './RouteFormModal.vue'
import { getRoutes } from './route.api'
import type { RouteSummary } from './route.types'

const auth = useAuthStore()

const items = ref<RouteSummary[]>([])
const loading = ref(false)
const errorMessage = ref('')

const modalOpen = ref(false)
const editing = ref<RouteSummary | null>(null)

const totalAssigned = computed(() => items.value.reduce((sum, r) => sum + r.assignedCount, 0))

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    items.value = await getRoutes()
  } catch {
    errorMessage.value = '노선 목록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  modalOpen.value = true
}

function openEdit(route: RouteSummary) {
  if (!auth.isAdmin) {
    return
  }
  editing.value = route
  modalOpen.value = true
}

function onSaved() {
  modalOpen.value = false
  load()
}

onMounted(load)
</script>

<template>
  <section>
    <div class="mb-4 flex items-center justify-between gap-2">
      <h2 class="text-xl font-bold">노선 관리</h2>
      <button v-if="auth.isAdmin" type="button" class="btn btn-primary" @click="openCreate">
        + 노선 등록
      </button>
    </div>

    <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    <p v-else-if="loading && items.length === 0" class="text-muted">불러오는 중...</p>

    <template v-else>
      <p class="mb-2 text-muted">노선 {{ items.length }}개 · 배정 차량 {{ totalAssigned }}대</p>

      <div class="overflow-x-auto rounded-md border border-line bg-surface">
        <table class="data-table">
          <thead>
          <tr>
            <th>노선</th>
            <th>배정 현황</th>
            <th>빈 슬롯</th>
          </tr>
          </thead>
          <tbody>
          <tr
            v-for="route in items"
            :key="route.routeNo"
            :class="{ 'cursor-pointer': auth.isAdmin }"
            @click="openEdit(route)"
          >
            <td class="font-semibold">{{ route.routeNo }}</td>
            <td>{{ route.assignedCount }} / {{ route.totalSlots }}</td>
            <td>
              <span v-if="route.assignedCount >= route.totalSlots" class="text-muted">가득 참</span>
              <span v-else>{{ route.totalSlots - route.assignedCount }}</span>
            </td>
          </tr>
          <tr v-if="items.length === 0">
            <td colspan="3" class="py-10 text-muted">등록된 노선이 없습니다.</td>
          </tr>
          </tbody>
        </table>
      </div>
    </template>

    <RouteFormModal
      v-if="auth.isAdmin"
      :open="modalOpen"
      :target="editing"
      @close="modalOpen = false"
      @saved="onSaved"
    />
  </section>
</template>
