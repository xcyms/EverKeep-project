<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { useManualTheme } from '@/composables/useManualTheme'

const { isDark } = useManualTheme()

definePage({
  name: 'about',
  style: {
    navigationBarTitleText: '关于系统',
    navigationStyle: 'custom',
  },
})

const statusBarHeight = ref(0)
const appName = ref('光影秘匣')

onMounted(() => {
  uni.getSystemInfo({
    success: (res) => {
      statusBarHeight.value = res.statusBarHeight || 0
    },
  })
})

function goBack() {
  uni.navigateBack()
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 transition-colors duration-300 dark:bg-black">
    <!-- 顶部导航 -->
    <div
      class="fixed left-0 right-0 top-0 z-50 px-4 pb-3 backdrop-blur-xl transition-colors duration-300"
      :class="isDark ? 'bg-black/80' : 'bg-white/80'"
      :style="{ paddingTop: `${statusBarHeight + 10}px` }"
    >
      <div class="flex items-center gap-4">
        <div
          class="h-10 w-10 flex items-center justify-center rounded-full transition-all active:scale-90"
          :class="isDark ? 'bg-white/10' : 'bg-gray-100'"
          @tap="goBack"
        >
          <div class="i-solar-alt-arrow-left-outline text-xl" :class="isDark ? 'text-white' : 'text-gray-900'" />
        </div>
        <span class="text-lg font-bold" :class="isDark ? 'text-white' : 'text-gray-900'">关于系统</span>
      </div>
    </div>

    <div class="px-6 pb-10" :style="{ paddingTop: `${statusBarHeight + 70}px` }">
      <!-- 介绍卡片 -->
      <div class="space-y-6">
        <div class="overflow-hidden rounded-3xl p-6 shadow-sm transition-colors duration-300" :class="isDark ? 'bg-[#1c1c1e]' : 'bg-white'">
          <h3 class="mb-4 flex items-center gap-2 text-[17px] font-bold" :class="isDark ? 'text-white' : 'text-gray-900'">
            <div class="i-solar-info-circle-bold-duotone text-primary text-xl" />
            项目介绍
          </h3>
          <p class="text-[15px] leading-relaxed" :class="isDark ? 'text-gray-400' : 'text-gray-600'">
            <span class="text-primary font-bold">{{ appName }}</span> 是一款专为用户打造的高颜值图床管理工具。
          </p>
          <p class="mt-2 text-[15px] leading-relaxed" :class="isDark ? 'text-gray-400' : 'text-gray-600'">
            我们致力于通过极简的设计语言与流畅的交互体验，为您提供图片预览、高效检索、分类管理及极速上传等全方位服务，让每一张光影记忆都能得到完美的留存与呈现。
          </p>
        </div>

        <div class="overflow-hidden rounded-3xl p-6 shadow-sm transition-colors duration-300" :class="isDark ? 'bg-[#1c1c1e]' : 'bg-white'">
          <h3 class="mb-4 flex items-center gap-2 text-[17px] font-bold" :class="isDark ? 'text-white' : 'text-gray-900'">
            <div class="i-solar-layers-bold-duotone text-primary text-xl" />
            技术栈
          </h3>
          <div class="flex flex-wrap gap-2">
            <span
              v-for="tech in ['Vue 3', 'TypeScript', 'Wot Design Uni', 'Pinia', 'UnoCSS', 'Vite']"
              :key="tech"
              class="rounded-xl px-3 py-1.5 text-xs font-medium transition-colors"
              :class="isDark ? 'bg-white/5 text-gray-400' : 'bg-gray-50 text-gray-600'"
            >
              {{ tech }}
            </span>
          </div>
        </div>
      </div>

      <!-- 版权底部 -->
      <div class="mt-20 flex flex-col items-center">
        <span class="text-[10px] font-bold tracking-[0.2em] uppercase" :class="isDark ? 'text-gray-600' : 'text-gray-300'">Developed by Lewis</span>
        <span class="mt-2 text-[10px]" :class="isDark ? 'text-gray-700' : 'text-gray-200'">© 2026 {{ appName }}. All rights reserved.</span>
      </div>
    </div>
  </div>
</template>
