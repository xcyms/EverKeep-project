<script lang="ts" setup>
import { useManualTheme } from '@/composables/useManualTheme'

interface Props {
  modelValue: boolean
  type?: 'image' | 'video'
  title?: string
  subtitle?: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'download'): void
}

const _props = withDefaults(defineProps<Props>(), {
  type: 'image',
  title: '下载资源',
  subtitle: '将选中的内容保存到您的系统相册',
})

const emit = defineEmits<Emits>()

const { isDark } = useManualTheme()

function handleClose() {
  emit('update:modelValue', false)
}

function handleDownload() {
  emit('download')
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
          class="group flex items-center justify-between rounded-2xl bg-gray-50/50 p-4 transition-all active:scale-[0.98] dark:bg-gray-800/50"
          @tap="handleDownload"
        >
          <div class="flex items-center gap-3">
            <div
              class="bg-primary h-10 w-10 flex items-center justify-center rounded-xl"
            >
              <div class="i-solar-cloud-download-bold text-xl text-black" />
            </div>
            <div class="flex flex-col">
              <span class="text-[15px] font-semibold" :class="isDark ? 'text-gray-200' : 'text-gray-700'">保存到相册</span>
              <span class="text-xs text-gray-400">将此{{ type === 'image' ? '图片' : '视频' }}原文件保存至手机</span>
            </div>
          </div>
          <div class="i-solar-arrow-right-linear text-gray-300" />
        </div>
      </div>

      <!-- 底部取消按钮 -->
      <div class="py-6">
        <div
          class="w-full rounded-2xl py-4 text-center text-[16px] text-white font-bold shadow-lg transition-all active:scale-[0.98] active:opacity-90"
          :class="isDark ? 'bg-gray-800 shadow-black/20' : 'bg-gray-900 shadow-gray-200'"
          @tap="handleClose"
        >
          取消
        </div>
      </div>
    </div>
  </wd-popup>
</template>
