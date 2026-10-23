<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getRoutes } from '@/domains/route/route.api'
import type { RouteSummary } from '@/domains/route/route.types'
import { getErrorMessage } from '@/shared/api/error'
import NightSlotSection from './NightSlotSection.vue'
import NightTripSection from './NightTripSection.vue'
import ShiftSlotSection from './ShiftSlotSection.vue'

const routes = ref<RouteSummary[]>([])
const routeNo = ref('')
const errorMessage = ref('')

onMounted(async () => {
  try {
    routes.value = await getRoutes()
    routeNo.value = routes.value[0]?.routeNo ?? ''
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '노선 목록을 불러오지 못했습니다.')
  }
})
</script>

<template>
  <section>
    <div class="mb-4 flex items-center justify-between gap-2">
      <h2 class="text-xl font-bold">단축·심야</h2>
    </div>

    <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>

    <template v-else-if="routes.length > 0">
      <!-- 전체 노선에 공통인 심야 기준 -->
      <NightTripSection :routes="routes" />

      <!-- 여기서부터는 노선을 골라서 본다 -->
      <div class="mb-4 flex items-center gap-2 border-t border-line pt-6">
        <span class="font-semibold">노선</span>
        <select v-model="routeNo" class="input w-auto">
          <option v-for="route in routes" :key="route.routeNo" :value="route.routeNo">
            {{ route.routeNo }}번
          </option>
        </select>
      </div>

      <ShiftSlotSection v-if="routeNo" :route-no="routeNo" />
      <NightSlotSection v-if="routeNo" :route-no="routeNo" />
    </template>
  </section>
</template>
