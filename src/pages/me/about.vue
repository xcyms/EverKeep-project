<script lang="ts" setup>
import { onMounted, ref } from 'vue'

definePage({
  name: 'about',
  style: {
    navigationBarTitleText: '关于系统',
    navigationStyle: 'custom',
  },
})

const statusBarHeight = ref(0)
const appName = ref('光影秘匣')
const version = ref('v1.0.0')

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
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- 顶部导航 -->
    <div
      class="fixed left-0 right-0 top-0 z-50 bg-white/80 px-4 pb-3 backdrop-blur-xl"
      :style="{ paddingTop: `${statusBarHeight + 10}px` }"
    >
      <div class="flex items-center gap-4">
        <div class="h-10 w-10 flex items-center justify-center rounded-full bg-gray-50 transition-transform active:scale-90" @tap="goBack">
          <wd-icon name="arrow-left" size="20px" color="#333" />
        </div>
        <span class="text-lg text-gray-900 font-bold">关于系统</span>
      </div>
    </div>

    <div class="px-6 pb-10 pt-32" :style="{ paddingTop: `${statusBarHeight + 70}px` }">
      <!-- Logo 区域 -->
      <div class="mb-10 flex flex-col items-center">
        <div class="mb-4 h-24 w-24 overflow-hidden rounded-3xl bg-white p-4 shadow-blue-100/50 shadow-xl">
          <image src="/static/logo.png" class="h-full w-full" mode="aspectFit" />
        </div>
        <span class="text-2xl text-gray-900 font-black tracking-tighter">{{ appName }}</span>
        <span class="mt-1 text-sm text-gray-400 font-medium tracking-widest uppercase">{{ version }}</span>
      </div>

      <!-- 介绍卡片 -->
      <div class="space-y-6">
        <div class="rounded-3xl bg-white p-6 shadow-sm">
          <h3 class="mb-3 flex items-center gap-2 text-[16px] text-gray-900 font-bold">
            <div class="h-4 w-1 rounded-full bg-blue-600" />
            项目介绍
          </h3>
          <p class="text-sm text-gray-500 leading-relaxed">
            {{ appName }} 是一款基于 Uni-app 和 Vue3 开发的高颜值图床管理终端。旨在为 Lsky Pro 用户提供极致的移动端管理体验，支持图片瀑布流预览、分类管理及便捷上传。
          </p>
        </div>

        <div class="rounded-3xl bg-white p-6 shadow-sm">
          <h3 class="mb-3 flex items-center gap-2 text-[16px] text-gray-900 font-bold">
            <div class="h-4 w-1 rounded-full bg-purple-600" />
            技术栈
          </h3>
          <div class="flex flex-wrap gap-2">
            <span class="rounded-lg bg-gray-50 px-3 py-1.5 text-xs text-gray-600 font-medium">Vue 3</span>
            <span class="rounded-lg bg-gray-50 px-3 py-1.5 text-xs text-gray-600 font-medium">TypeScript</span>
            <span class="rounded-lg bg-gray-50 px-3 py-1.5 text-xs text-gray-600 font-medium">Wot Design Uni</span>
            <span class="rounded-lg bg-gray-50 px-3 py-1.5 text-xs text-gray-600 font-medium">Pinia</span>
            <span class="rounded-lg bg-gray-50 px-3 py-1.5 text-xs text-gray-600 font-medium">Tailwind CSS</span>
          </div>
        </div>
      </div>

      <!-- 版权底部 -->
      <div class="mt-20 flex flex-col items-center">
        <span class="text-[10px] text-gray-300 font-bold tracking-[0.2em] uppercase">Developed by Lewis</span>
        <span class="mt-2 text-[10px] text-gray-200">© 2026 {{ appName }}. All rights reserved.</span>
      </div>
    </div>
  </div>
</template>
