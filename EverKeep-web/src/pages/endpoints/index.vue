<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { getEndpointsApi, type Endpoint } from '../../api/system'

const loading = ref(false)
const endpoints = ref<Endpoint[]>([])
const searchText = ref('')

const fetchEndpoints = async () => {
  loading.value = true
  try {
    const data = await getEndpointsApi()
    endpoints.value = data
  } catch (error) {
    console.error('获取接口列表失败:', error)
    message.error('获取接口列表失败')
  } finally {
    loading.value = false
  }
}

const filteredEndpoints = computed(() => {
  const query = searchText.value.toLowerCase().trim()
  if (!query) return endpoints.value

  return endpoints.value.filter(item => 
    item.name.toLowerCase().includes(query) ||
    item.controller.toLowerCase().includes(query) ||
    item.path.some(p => p.toLowerCase().includes(query)) ||
    item.description.toLowerCase().includes(query)
  )
})

const getMethodColor = (method: string) => {
  switch (method.toUpperCase()) {
    case 'GET': return 'blue'
    case 'POST': return 'green'
    case 'PUT': return 'orange'
    case 'DELETE': return 'red'
    default: return 'gray'
  }
}

onMounted(() => {
  fetchEndpoints()
})

const columns = [
  { title: '控制器', dataIndex: 'controller', key: 'controller', width: 180 },
  { title: '方法名', dataIndex: 'name', key: 'name', width: 180 },
  { title: '路径', dataIndex: 'path', key: 'path' },
  { title: '请求方法', dataIndex: 'methods', key: 'methods', width: 120 },
  { title: '描述', dataIndex: 'description', key: 'description' },
]
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex items-center justify-between">
      <h2 class="text-lg font-bold text-gray-800 m-0 flex items-center gap-2">
        <div class="i-fa6-solid:list-check text-blue-500" />
        后端接口列表
      </h2>
      <a-input-search
        v-model:value="searchText"
        placeholder="搜索接口、控制器或路径..."
        class="w-80"
        allow-clear
      />
    </div>

    <a-table 
      :columns="columns" 
      :data-source="filteredEndpoints" 
      :loading="loading"
      :pagination="false"
      row-key="path"
      class="bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'path'">
          <div class="flex flex-col gap-1">
            <code v-for="p in record.path" :key="p" class="text-xs bg-gray-50 text-blue-600 px-2 py-0.5 rounded border border-gray-100">
              {{ p }}
            </code>
          </div>
        </template>
        <template v-if="column.key === 'methods'">
          <div class="flex flex-wrap gap-1">
            <a-tag v-for="m in record.methods" :key="m" :color="getMethodColor(m)">
              {{ m }}
            </a-tag>
          </div>
        </template>
        <template v-if="column.key === 'controller'">
          <span class="font-medium text-gray-700">{{ record.controller }}</span>
        </template>
        <template v-if="column.key === 'description'">
          <span class="text-gray-500 italic">{{ record.description || '暂无描述' }}</span>
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped>
:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa;
}
</style>