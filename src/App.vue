<script setup lang="ts">
const env = import.meta.env.MODE
const isDev = env === 'development'

// eslint-disable-next-line no-console
console.log(
  `%c ${import.meta.env.VITE_APP_TITLE} %c Running in ${env} mode %c`,
  `background: ${isDev ? '#42b883' : '#35495e'}; padding: 1px; border-radius: 3px 0 0 3px; color: #fff`,
  `background: ${isDev ? '#35495e' : '#42b883'}; padding: 1px; border-radius: 0 3px 3px 0; color: #fff`,
  'background: transparent',
)
// eslint-disable-next-line no-console
console.log(`%cAPI URL:%c ${import.meta.env.VITE_API_BASE_URL}`, 'color: #999', 'color: #42b883; font-weight: bold')
</script>

<template>
  <!-- 路由出口 -->
  <router-view />
</template>

<style scoped>
/* 全局样式 */
html.dark {
  background: #121212;
  color: white;
}
</style>
