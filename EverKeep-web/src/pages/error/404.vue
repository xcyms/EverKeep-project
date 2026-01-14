<script setup lang="ts">
const router = useRouter()
</script>

<template>
  <div class="h-full flex items-center justify-center">
    <a-result status="404" title="404" sub-title="对不起，您访问的页面不存在。">
      <template #extra>
        <a-button type="primary" @click="router.push('/')">返回首页</a-button>
      </template>
    </a-result>
  </div>
</template>

<route lang="yaml">
meta:
  layout: default
  label: 404
</route>