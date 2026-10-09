<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import BasePagination from '@/shared/components/BasePagination.vue'
import type { PageResponse } from '@/shared/api/page'
import { useAuthStore } from '@/domains/auth/auth.store'
import EmployeeAdminTable from './EmployeeAdminTable.vue'
import EmployeeBriefTable from './EmployeeBriefTable.vue'
import EmployeeFormModal from './EmployeeFormModal.vue'
import EmployeeRetireModal from './EmployeeRetireModal.vue'
import { searchEmployeeBriefs, searchEmployees } from './employee.api'
import type {
  EmployeeBrief,
  EmployeeSearchParams,
  EmployeeSummary,
  JobType,
  RetireTarget,
} from './employee.types'

const PAGE_SIZE = 20

const auth = useAuthStore()

const condition = reactive<{ keyword: string; jobType: JobType | ''; includeRetired: boolean }>({
  keyword: '',
  jobType: '',
  includeRetired: false,
})

// 권한에 따라 받는 데이터 모양이 달라서 따로 보관한다
const adminPage = ref<PageResponse<EmployeeSummary> | null>(null)
const briefPage = ref<PageResponse<EmployeeBrief> | null>(null)
// 전체 인원·페이지 이동처럼 둘 다 공통인 부분에 쓰는 "지금 보고 있는 결과"
const currentPage = computed(() => (auth.isAdmin ? adminPage.value : briefPage.value))

const loading = ref(false)
const errorMessage = ref('')

const modalOpen = ref(false)
const editingId = ref<number | null>(null)
const retireTarget = ref<RetireTarget | null>(null)

async function load(page: number) {
  loading.value = true
  errorMessage.value = ''

  const params: EmployeeSearchParams = {
    keyword: condition.keyword || undefined,
    jobType: condition.jobType || undefined,
    page,
    size: PAGE_SIZE,
  }

  try {
    if (auth.isAdmin) {
      adminPage.value = await searchEmployees({
        ...params,
        includeRetired: condition.includeRetired || undefined,
      })
    } else {
      briefPage.value = await searchEmployeeBriefs(params)
    }
  } catch {
    errorMessage.value = '직원 목록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

function onSearch() {
  load(1)
}

function reloadCurrentPage() {
  load(currentPage.value?.page ?? 1)
}

function openCreate() {
  editingId.value = null
  modalOpen.value = true
}

function openEdit(id: number) {
  editingId.value = id
  modalOpen.value = true
}

function onSaved() {
  modalOpen.value = false
  reloadCurrentPage()
}

function onRetireRequest(target: RetireTarget) {
  modalOpen.value = false
  retireTarget.value = target
}

function onRetired() {
  retireTarget.value = null
  reloadCurrentPage()
}

onMounted(() => {
  load(1)
})
</script>

<template>
  <section>
    <div class="mb-4 flex items-center justify-between gap-2">
      <h2 class="text-xl font-bold">직원 관리</h2>
      <button v-if="auth.isAdmin" type="button" class="btn btn-primary" @click="openCreate">
        + 직원 등록
      </button>
    </div>

    <form class="mb-4 flex flex-wrap items-center gap-2" @submit.prevent="onSearch">
      <input
        v-model="condition.keyword"
        type="text"
        class="input sm:w-60"
        :placeholder="auth.isAdmin ? '이름 또는 휴대폰 번호' : '이름'"
      />
      <select v-model="condition.jobType" class="input w-auto">
        <option value="">전체</option>
        <option value="DRIVER">기사</option>
        <option value="OFFICE">사무</option>
      </select>
      <label v-if="auth.isAdmin" class="flex cursor-pointer items-center gap-1 whitespace-nowrap">
        <input
          v-model="condition.includeRetired"
          type="checkbox"
          class="accent-primary"
          @change="onSearch"
        />
        퇴사자 포함
      </label>
      <button type="submit" class="btn btn-primary" :disabled="loading">조회</button>
    </form>

    <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>

    <template v-else-if="currentPage">
      <p class="mb-2 text-muted">전체 {{ currentPage.totalCount }}명</p>

      <EmployeeAdminTable v-if="adminPage" :items="adminPage.items" @select="openEdit" />
      <EmployeeBriefTable v-else-if="briefPage" :items="briefPage.items" />

      <BasePagination
        :page="currentPage.page"
        :size="currentPage.size"
        :total-count="currentPage.totalCount"
        @change="load"
      />
    </template>

    <!-- 관리자만 쓰는 모달 -->
    <template v-if="auth.isAdmin">
      <EmployeeFormModal
        :open="modalOpen"
        :employee-id="editingId"
        @close="modalOpen = false"
        @saved="onSaved"
        @retire="onRetireRequest"
        @account-changed="reloadCurrentPage"
      />
      <EmployeeRetireModal
        :target="retireTarget"
        @close="retireTarget = null"
        @retired="onRetired"
      />
    </template>
  </section>
</template>
