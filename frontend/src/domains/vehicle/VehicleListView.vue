<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAuthStore } from '@/domains/auth/auth.store'
import { getRoutes } from '@/domains/route/route.api'
import type { RouteSummary } from '@/domains/route/route.types'
import VehicleFormModal from './VehicleFormModal.vue'
import { getVehicles } from './vehicle.api'
import type { VehicleSearchParams, VehicleSummary } from './vehicle.types'

/** 필터 선택값 중 "예비 차량" 을 뜻하는 값 (노선 번호에는 _ 를 쓸 수 없어서 겹치지 않는다) */
const SPARE = '__spare__'

const auth = useAuthStore()

const items = ref<VehicleSummary[]>([])
const routes = ref<RouteSummary[]>([])
const filter = ref('') // '' = 전체, SPARE = 예비만, 그 밖 = 노선 번호
const loading = ref(false)
const errorMessage = ref('')

const modalOpen = ref(false)
const editing = ref<VehicleSummary | null>(null)

async function load() {
  loading.value = true
  errorMessage.value = ''

  const params: VehicleSearchParams =
    filter.value === SPARE ? { spareOnly: true }
      : filter.value ? { routeNo: filter.value }
        : {}

  try {
    // 차량과 노선(배정 현황)을 동시에 요청하고 둘 다 올 때까지 기다린다
    const [vehicles, routeList] = await Promise.all([getVehicles(params), getRoutes()])
    items.value = vehicles
    routes.value = routeList
  } catch {
    errorMessage.value = '차량 목록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  modalOpen.value = true
}

function openEdit(vehicle: VehicleSummary) {
  if (!auth.isAdmin) {
    return
  }
  editing.value = vehicle
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
      <h2 class="text-xl font-bold">차량 관리</h2>
      <button v-if="auth.isAdmin" type="button" class="btn btn-primary" @click="openCreate">
        + 차량 등록
      </button>
    </div>

    <div class="mb-4 flex items-center gap-2">
      <select v-model="filter" class="input w-auto" @change="load">
        <option value="">전체 노선</option>
        <option :value="SPARE">예비 차량</option>
        <option v-for="route in routes" :key="route.routeNo" :value="route.routeNo">
          {{ route.routeNo }}
        </option>
      </select>
    </div>

    <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    <p v-else-if="loading && items.length === 0" class="text-muted">불러오는 중...</p>

    <template v-else>
      <p class="mb-2 text-muted">{{ items.length }}대</p>

      <div class="overflow-x-auto rounded-md border border-line bg-surface">
        <table class="data-table">
          <thead>
          <tr>
            <th>차량 번호</th>
            <th>노선</th>
            <th>슬롯</th>
          </tr>
          </thead>
          <tbody>
          <tr
            v-for="vehicle in items"
            :key="vehicle.vehicleNo"
            :class="{ 'cursor-pointer': auth.isAdmin }"
            @click="openEdit(vehicle)"
          >
            <td class="font-semibold">{{ vehicle.vehicleNo }}</td>
            <td>
              <span v-if="vehicle.routeNo">{{ vehicle.routeNo }}</span>
              <span v-else class="text-muted">예비</span>
            </td>
            <td>{{ vehicle.slotNo ?? '-' }}</td>
          </tr>
          <tr v-if="items.length === 0">
            <td colspan="3" class="py-10 text-muted">조회 결과가 없습니다.</td>
          </tr>
          </tbody>
        </table>
      </div>
    </template>

    <VehicleFormModal
      v-if="auth.isAdmin"
      :open="modalOpen"
      :target="editing"
      :routes="routes"
      @close="modalOpen = false"
      @saved="onSaved"
    />
  </section>
</template>
