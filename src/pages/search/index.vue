<script lang="ts" setup>
import { onMounted, ref } from 'vue'

definePage({
  name: 'search',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '搜索',
    navigationStyle: 'custom',
  },
})

const statusBarHeight = ref(0) // 获取状态栏高度

onMounted(() => {
  uni.getSystemInfo({
    success: (res) => {
      statusBarHeight.value = res.statusBarHeight || 0
    },
  })
})
</script>

<template>
  <div class="flex flex-col items-center p-6" :style="{ paddingTop: `${statusBarHeight}px` }">
    <h1 class="my-4">开发中</h1>
  </div>
</template>
