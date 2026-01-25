<script lang="ts" setup>
import type { SortOption } from '@/types/common'
import { computed } from 'vue'
import { useManualTheme } from '@/composables/useManualTheme'

interface Props {
  modelValue: boolean
  options: SortOption[]
  currentValue: string
  title?: string
  subtitle?: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'select', option: SortOption): void
  (e: 'close'): void
}

const _props = withDefaults(defineProps<Props>(), {
  title: '排序方式',
  subtitle: '选择您偏好的内容展示顺序',
})

const emit = defineEmits<Emits>()

const { isDark, themeVars } = useManualTheme()
const activeColor = computed(() => (themeVars.value as any).colorTheme || '#4D7FFF')

function handleClose () {
  emit('update:modelValue', false)
  emit('close')
}

function handleSelect (option: SortOption) {
  emit('select', option)
  emit('update:modelValue', false)
}
</script>

<template>
  <wd-popup
    :model-value="modelValue"
    position="bottom"
    round
    custom-class="rounded-t-[32rpx] overflow-hidden"
    :z-index="10001"
    safe-area-inset-bottom
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div
      class="px-6 pb-2 backdrop-blur-md transition-colors duration-300"
      :class="isDark ? 'bg-gray-900/95 text-white' : 'bg-white/95 text-gray-900'"
    >
      <!-- 顶部装饰条 -->
      <div class="flex justify-center py-3">
        <div
          class="h-1 w-10 rounded-full"
          :class="isDark ? 'bg-gray-700' : 'bg-gray-200'"
        />
      </div>

      <!-- 头部 -->
      <div class="flex items-center justify-between pb-4 pt-2">
        <div class="flex flex-col">
          <span class="text-lg font-bold" :class="isDark ? 'text-white' : 'text-gray-900'">{{ title }}</span>
          <span class="mt-0.5 text-xs text-gray-400">{{ subtitle }}</span>
        </div>
        <div
          class="h-8 w-8 flex items-center justify-center rounded-full transition-colors"
          :class="isDark ? 'bg-gray-800 active:bg-gray-700' : 'bg-gray-100 active:bg-gray-200'"
          @tap="handleClose"
        >
          <wd-icon name="close" size="16px" :color="isDark ? '#999' : '#666'" />
        </div>
      </div>

      <!-- 选项列表 -->
      <div class="py-2 space-y-2">
        <div
          v-for="item in options"
          :key="item.value"
          class="group flex items-center justify-between rounded-2xl p-4 transition-all"
          :class="[
            currentValue === item.value
              ? (isDark ? 'bg-primary/20' : 'bg-primary/10')
              : (isDark ? 'bg-gray-800/50' : 'bg-gray-50/50')
          ]"
          @tap="handleSelect(item)"
        >
          <div class="flex items-center gap-3">
            <div
              class="h-10 w-10 flex items-center justify-center rounded-xl transition-colors"
              :class="[currentValue === item.value ? 'text-white' : (isDark ? 'bg-gray-700 text-gray-400' : 'bg-white text-gray-400')]"
              :style="{ backgroundColor: currentValue === item.value ? activeColor : undefined }"
            >
              <div class="text-xl" :class="[item.icon || 'i-solar-sort-vertical-bold-duotone']" />
            </div>
            <div class="flex flex-col">
              <span
                class="text-[15px] transition-colors"
                :class="[currentValue === item.value ? 'font-semibold' : (isDark ? 'text-gray-200' : 'text-gray-700')]"
                :style="{ color: currentValue === item.value ? activeColor : undefined }"
              >
                {{ item.name }}
              </span>
              <span
                class="text-xs transition-colors"
                :class="[currentValue === item.value ? 'opacity-80' : 'text-gray-400']"
                :style="{ color: currentValue === item.value ? activeColor : undefined }"
              >
                {{ item.subname }}
              </span>
            </div>
          </div>
          <div
            v-if="currentValue === item.value"
            class="i-solar-check-read-bold text-xl"
            :style="{ color: activeColor }"
          />
        </div>
      </div>

      <!-- 底部完成按钮 -->
      <div class="py-6">
        <div
          class="w-full rounded-2xl py-4 text-center text-[16px] text-white font-bold shadow-lg transition-all active:scale-[0.98] active:opacity-90"
          :class="isDark ? 'bg-gray-800 shadow-black/20' : 'bg-gray-900 shadow-gray-200'"
          @tap="handleClose"
        >
          完成
        </div>
      </div>
    </div>
  </wd-popup>
</template>
