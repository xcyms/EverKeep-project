<script lang="ts" setup>
import type { SortOption } from '@/types/common'

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

const props = withDefaults(defineProps<Props>(), {
  title: '排序方式',
  subtitle: '选择您偏好的内容展示顺序',
})

const emit = defineEmits<Emits>()

const handleClose = () => {
  emit('update:modelValue', false)
  emit('close')
}

const handleSelect = (option: SortOption) => {
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
    <div class="bg-white/95 px-6 pb-2 backdrop-blur-md">
      <!-- 顶部装饰条 -->
      <div class="flex justify-center py-3">
        <div class="h-1 w-10 rounded-full bg-gray-200" />
      </div>

      <!-- 头部 -->
      <div class="flex items-center justify-between pb-4 pt-2">
        <div class="flex flex-col">
          <span class="text-lg text-gray-900 font-bold">{{ title }}</span>
          <span class="mt-0.5 text-xs text-gray-400">{{ subtitle }}</span>
        </div>
        <div
          class="h-8 w-8 flex items-center justify-center rounded-full bg-gray-100 transition-colors active:bg-gray-200"
          @tap="handleClose"
        >
          <wd-icon name="close" size="16px" color="#666" />
        </div>
      </div>

      <!-- 选项列表 -->
      <div class="py-2 space-y-2">
        <div
          v-for="item in options"
          :key="item.value"
          class="group flex items-center justify-between rounded-2xl p-4 transition-all active:bg-blue-50/50"
          :class="[currentValue === item.value ? 'bg-blue-50/40' : 'bg-gray-50/50']"
          @tap="handleSelect(item)"
        >
          <div class="flex items-center gap-3">
            <div
              class="h-10 w-10 flex items-center justify-center rounded-xl transition-colors"
              :class="[currentValue === item.value ? 'bg-blue-600 text-white' : 'bg-white text-gray-400']"
            >
              <wd-icon
                :name="item.icon || 'order-descending'"
                size="20px"
              />
            </div>
            <div class="flex flex-col">
              <span class="text-[15px] transition-colors" :class="[currentValue === item.value ? 'text-blue-700 font-semibold' : 'text-gray-700']">
                {{ item.name }}
              </span>
              <span class="text-xs transition-colors" :class="[currentValue === item.value ? 'text-blue-500/80' : 'text-gray-400']">
                {{ item.subname }}
              </span>
            </div>
          </div>
          <div
            v-if="currentValue === item.value"
            class="h-6 w-6 flex items-center justify-center rounded-full bg-blue-600"
          >
            <wd-icon name="check" size="14px" color="#fff" />
          </div>
        </div>
      </div>

      <!-- 底部完成按钮 -->
      <div class="py-6">
        <div
          class="w-full rounded-2xl bg-gray-900 py-4 text-center text-[16px] text-white font-bold shadow-gray-200 shadow-lg transition-all active:scale-[0.98] active:opacity-90"
          @tap="handleClose"
        >
          完成
        </div>
      </div>
    </div>
  </wd-popup>
</template>
