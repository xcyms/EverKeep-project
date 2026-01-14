<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getSystemConfigsApi, updateConfigApi } from '../../api/config'
import type { API } from '../../types'

const loading = ref(false)
const configs = ref<API.SysConfig[]>([])

// 加载系统配置
const loadConfigs = async () => {
  loading.value = true
  try {
    const res = await getSystemConfigsApi()
    configs.value = res
  } catch (error) {
    console.error('加载配置失败:', error)
  } finally {
    loading.value = false
  }
}

// 保存配置修改
const handleSave = async (config: API.SysConfig) => {
  try {
    await updateConfigApi(config)
    message.success(`配置 [${config.configName}] 已更新`)
    loadConfigs() // 重新加载以确保同步
  } catch (error) {
    // 错误已由拦截器处理
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<template>
  <div class="max-w-5xl mx-auto py-6">
    <div class="flex items-center justify-between mb-8">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">系统设置</h2>
        <p class="text-gray-500 mt-1 text-sm">管理系统全局运行参数，修改后对所有未自定义配置的用户生效。</p>
      </div>
      <a-button @click="loadConfigs" :loading="loading" class="flex items-center gap-1.5">
        <template #icon>
          <span class="i-fa6-solid:rotate text-[13px]" />
        </template>
        刷新配置
      </a-button>
    </div>

    <div v-if="loading && configs.length === 0" class="py-20 text-center">
      <a-spin tip="正在获取系统配置..." />
    </div>

    <div v-else class="grid grid-cols-1 gap-6">
      <div v-for="item in configs" :key="item.id" class="bg-white rounded-xl border border-gray-100 p-6 shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-start justify-between gap-6">
          <div class="flex-1 space-y-4">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-lg bg-blue-50 text-blue-500 flex items-center justify-center flex-shrink-0">
                <span class="i-fa6-solid:gears text-lg" />
              </div>
              <div class="flex flex-col">
                <h4 class="font-bold text-gray-800 m-0 leading-tight">{{ item.configName }}</h4>
                <div class="mt-1">
                  <code class="text-[10px] text-gray-400 bg-gray-50 px-1.5 py-0.5 rounded border border-gray-100">{{ item.configKey }}</code>
                </div>
              </div>
            </div>

            <div class="flex items-center gap-4">
              <div class="flex-1">
                <a-input 
                  v-model:value="item.configValue" 
                  size="large"
                  placeholder="请输入配置值"
                  class="font-mono text-sm"
                />
              </div>
              <a-button type="primary" size="large" @click="handleSave(item)">
                保存修改
              </a-button>
            </div>

            <p class="text-xs text-gray-400 m-0 flex items-center gap-1.5">
              <span class="i-fa6-solid:circle-info text-[12px] opacity-70" />
              {{ item.remark || '暂无详细说明' }}
            </p>
          </div>
        </div>
      </div>
      
      <a-empty v-if="!loading && configs.length === 0" description="未找到系统配置项" />
    </div>
  </div>
</template>

<style scoped>
:deep(.ant-input) {
  border-radius: 8px;
}
</style>