<script setup lang="ts">
const router = useRouter()
</script>

<template>
  <div class="h-full flex items-center justify-center">
    <a-result status="403" title="401" sub-title="对不起，您没有权限访问此页面。">
      <template #extra>
        <a-button type="primary" @click="router.push('/')">返回首页</a-button>
      </template>
    </a-result>
  </div>
</template>

<route lang="yaml">
meta:
  layout: default
  label: 无权限
</route>