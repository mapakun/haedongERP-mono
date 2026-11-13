<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getErrorMessage } from '@/shared/api/error'
import ComfortMemberModal from './ComfortMemberModal.vue'
import { getDriverMembers } from './comfortleave.api'
import { employmentTypeLabel, type DriverMember } from './comfortleave.types'

const members = ref<DriverMember[]>([])
const keyword = ref('')
const onlyUnregistered = ref(false)
const hideRetired = ref(true)
const loading = ref(false)
const errorMessage = ref('')
const editing = ref<DriverMember | null>(null)

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    members.value = await getDriverMembers()
    // 편집 창이 열려 있으면 새로 받은 목록의 같은 사람으로 바꿔 끼운다
    if (editing.value) {
      const id = editing.value.employeeId
      editing.value = members.value.find((member) => member.employeeId === id) ?? null
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '대상자 목록을 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
}

const filtered = computed(() => {
  const word = keyword.value.trim()
  return members.value.filter(
    (member) =>
      (!onlyUnregistered.value || member.employmentType === null) &&
      (!hideRetired.value || member.retiredAt === null) &&
      (!word || member.name.includes(word)),
  )
})

/** 재직 중인데 아직 위로휴가 대상자로 등록하지 않은 기사 수 */
const unregisteredCount = computed(
  () => members.value.filter((member) => member.employmentType === null && member.retiredAt === null).length,
)

onMounted(load)
</script>

<template>
  <section>
    <div class="mb-4 flex flex-wrap items-center justify-between gap-2">
      <h2 class="text-xl font-bold">위로휴가 대상자 관리</h2>
      <RouterLink :to="{ name: 'comfort-leave' }" class="btn">← 위로휴가 현황</RouterLink>
    </div>

    <p v-if="unregisteredCount > 0" class="mb-3 text-danger">
      재직 중인데 아직 등록되지 않은 기사가 {{ unregisteredCount }}명 있습니다. 편집을 눌러 고용 형태와 입사일자를 저장하세요.
    </p>

    <div class="mb-4 flex flex-wrap items-center gap-3">
      <input v-model="keyword" type="text" class="input sm:w-48" placeholder="이름 검색" />
      <label class="flex cursor-pointer items-center gap-1">
        <input v-model="onlyUnregistered" type="checkbox" class="accent-primary" />
        미등록만 보기
      </label>
      <label class="flex cursor-pointer items-center gap-1">
        <input v-model="hideRetired" type="checkbox" class="accent-primary" />
        퇴사자 숨기기
      </label>
    </div>

    <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    <p v-else-if="loading && members.length === 0" class="text-muted">불러오는 중...</p>

    <template v-else>
      <p class="mb-2 text-muted">{{ filtered.length }}명</p>
      <div class="overflow-x-auto rounded-md border border-line bg-surface">
        <table class="data-table">
          <thead>
            <tr>
              <th>이름</th>
              <th>고용 형태</th>
              <th>입사일자</th>
              <th>메모</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="member in filtered" :key="member.employeeId">
              <td class="font-semibold">
                {{ member.name }}
                <span v-if="member.retiredAt" class="ml-1 text-xs font-normal text-muted">
                  퇴사 {{ member.retiredAt }}
                </span>
              </td>
              <td>
                <span v-if="member.employmentType">{{ employmentTypeLabel[member.employmentType] }}</span>
                <span v-else class="text-danger">미등록</span>
              </td>
              <td>{{ member.hireDate ?? '-' }}</td>
              <td class="text-left">{{ member.note ?? '' }}</td>
              <td>
                <button type="button" class="btn px-2.5 py-1" @click="editing = member">편집</button>
              </td>
            </tr>
            <tr v-if="filtered.length === 0">
              <td colspan="5" class="py-10 text-muted">조회 결과가 없습니다.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <ComfortMemberModal :member="editing" @close="editing = null" @changed="load" />
  </section>
</template>
