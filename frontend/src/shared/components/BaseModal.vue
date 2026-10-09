<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'

const props = defineProps<{
  open: boolean
  title: string
}>()

const emit = defineEmits<{
  close: []
}>()

function onKeydown(event: KeyboardEvent) {
  if (props.open && event.key === 'Escape') {
    emit('close')
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onBeforeUnmount(() => window.removeEventListener('keydown', onKeydown))
</script>

<template>
  <Teleport to="body">
    <!-- 휴대폰: 화면 아래에 붙는 시트 / 넓은 화면: 가운데 창 -->
    <div
      v-if="open"
      class="fixed inset-0 z-50 flex items-end justify-center bg-black/40 md:items-center md:p-4"
    >
      <div
        class="flex max-h-[90dvh] w-full flex-col rounded-t-xl bg-surface shadow-xl md:max-w-md md:rounded-xl"
        role="dialog"
        aria-modal="true"
        :aria-label="title"
      >
        <header class="flex items-center justify-between border-b border-line px-5 py-4">
          <h3 class="text-base font-semibold">{{ title }}</h3>
          <button
            type="button"
            class="cursor-pointer text-2xl leading-none text-muted hover:text-ink"
            aria-label="닫기"
            @click="emit('close')"
          >
            ×
          </button>
        </header>

        <div class="overflow-y-auto p-5">
          <slot />
        </div>

        <footer v-if="$slots.footer" class="flex justify-end gap-2 border-t border-line px-5 py-3">
          <slot name="footer" />
        </footer>
      </div>
    </div>
  </Teleport>
</template>
