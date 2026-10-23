<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getRoutes } from '@/domains/route/route.api'
import type { RouteSummary } from '@/domains/route/route.types'
import { getErrorMessage } from '@/shared/api/error'
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

    <template v-else>
      <div class="mb-4 flex items-center gap-2">
        <select v-model="routeNo" class="input w-auto">
          <option v-for="route in routes" :key="route.routeNo" :value="route.routeNo">
            {{ route.routeNo }}번
          </option>
        </select>
      </div>

      <ShiftSlotSection v-if="routeNo" :route-no="routeNo" />
    </template>
  </section>
</template>
