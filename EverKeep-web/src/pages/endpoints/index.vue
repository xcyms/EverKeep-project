<script setup lang="ts">
import { ref, onMounted, computed, h, defineComponent } from 'vue'
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
  let list = endpoints.value
  
  if (query) {
    list = endpoints.value.filter(item => 
      item.name.toLowerCase().includes(query) ||
      item.controller.toLowerCase().includes(query) ||
      item.path.some(p => p.toLowerCase().includes(query)) ||
      item.description.toLowerCase().includes(query)
    )
  }

  // 按控制器分组
  const groups: Record<string, Endpoint[]> = {}
  list.forEach(item => {
    if (!groups[item.controller]) {
      groups[item.controller] = []
    }
    groups[item.controller]?.push(item)
  })
  
  return Object.entries(groups).map(([name, children]) => ({
    name,
    children: children.sort((a, b) => a.order - b.order)
  }))
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
  { title: '接口名称', dataIndex: 'name', key: 'name', width: 220 },
  { title: '方法名称', dataIndex: 'methodName', key: 'methodName', width: 120 },
  { title: '路径', dataIndex: 'path', key: 'path' },
  { title: '方法', dataIndex: 'methods', key: 'methods', width: 100 },
  { title: '功能描述', dataIndex: 'description', key: 'description' },
]

// 递归渲染参数表格的组件
const ParamTable = defineComponent({
  name: 'ParamTable',
  props: {
    data: { type: Array as () => any[], required: true }
  },
  setup(props) {
    const columns = [
      { title: '中文描述', dataIndex: 'description', key: 'description', width: 180 },
      { title: '字段名', dataIndex: 'field', key: 'field', width: 150 },
      { title: '类型', dataIndex: 'type', key: 'type', width: 100 },
      { title: '必填', dataIndex: 'required', key: 'required', width: 60 },
      { title: '示例值', dataIndex: 'example', key: 'example' },
    ]
    return () => h('a-table', {
      columns,
      dataSource: props.data,
      pagination: false,
      size: 'small',
      rowKey: 'field',
      childrenColumnName: 'children',
      bordered: true,
      class: 'param-table-custom'
    }, {
      bodyCell: ({ column, text }: any) => {
        if (column.key === 'required') {
          return text ? h('span', { class: 'text-red-500' }, '是') : h('span', { class: 'text-gray-400' }, '否')
        }
        if (column.key === 'field') {
          return h('span', { class: 'font-mono text-blue-600 font-bold' }, text)
        }
        if (column.key === 'description') {
          return h('span', { class: 'text-gray-800 font-medium' }, text || '-')
        }
        if (column.key === 'example' && text) {
          return h('code', { class: 'text-[11px] bg-orange-50 text-orange-600 px-1 rounded' }, text)
        }
        return text
      }
    })
  }
})
</script>

<template>
  <div class="flex flex-col gap-4">
    <!-- 顶部搜索栏 -->
    <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex items-center justify-between sticky top-0 z-10">
      <h2 class="text-lg font-bold text-gray-800 m-0 flex items-center gap-2">
        <div class="i-fa6-solid:list-check text-blue-500" />
        后端接口文档
      </h2>
      <div class="flex items-center gap-3">
        <a-tag color="blue">共 {{ endpoints.length }} 个接口</a-tag>
        <a-input-search
          v-model:value="searchText"
          placeholder="搜索接口、控制器或路径..."
          class="w-80"
          allow-clear
        />
      </div>
    </div>

    <!-- 分组展示区域 -->
    <div v-for="group in filteredEndpoints" :key="group.name" class="space-y-3">
      <div class="flex items-center gap-2 px-2">
        <div class="w-1 h-4 bg-blue-500 rounded-full" />
        <h3 class="text-base font-bold text-gray-700 m-0">{{ group.name }}</h3>
        <span class="text-xs text-gray-400 font-normal">({{ group.children.length }} 个接口)</span>
      </div>

      <a-table 
        :columns="columns" 
        :data-source="group.children" 
        :loading="loading"
        :pagination="false"
        row-key="path"
        class="bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100"
      >
        <!-- 保持之前的展开行逻辑 -->
        <template #expandedRowRender="{ record }">
          <div class="p-4 bg-gray-50/50 rounded-lg space-y-6">
            <!-- 权限信息 -->
            <div v-if="record.security && (record.security.roles?.length || record.security.permissions?.length || record.security.requiresLogin)">
              <h4 class="text-sm font-bold text-gray-700 mb-3 flex items-center gap-2">
                <div class="i-fa6-solid:shield-halved text-orange-500" />
                权限要求
              </h4>
              <div class="flex flex-wrap gap-2">
                <a-tag v-if="record.security.requiresLogin" color="orange">需要登录</a-tag>
                <a-tag v-for="role in record.security.roles" :key="role" color="purple">角色: {{ role }}</a-tag>
                <a-tag v-for="perm in record.security.permissions" :key="perm" color="cyan">权限: {{ perm }}</a-tag>
              </div>
            </div>

            <!-- 入参展示 -->
            <div v-if="record.params && record.params.length > 0">
              <h4 class="text-sm font-bold text-gray-700 mb-3 flex items-center gap-2">
                <div class="i-fa6-solid:arrow-right-to-bracket text-blue-500" />
                请求参数
              </h4>
              <div v-if="record.methodName" class="text-sm text-gray-500 mb-3">
                <span class="font-mono font-bold text-gray-800">{{ record.methodName }}</span>
              </div>
              <div class="space-y-4">
                <div v-for="param in record.params" :key="param.name" class="bg-white p-3 rounded border border-gray-200 shadow-sm">
                  <div class="flex items-center gap-3 mb-2">
                    <span class="px-2 py-0.5 bg-blue-50 text-blue-600 rounded text-xs font-bold">{{ param.source }}</span>
                    <span class="font-mono font-bold text-gray-800">{{ param.name }}</span>
                    <span class="text-xs text-gray-400">{{ param.type }}</span>
                  </div>
                  <!-- Schema 详情 -->
                  <div v-if="param.schema && param.schema.length > 0" class="mt-2">
                    <ParamTable :data="param.schema" />
                  </div>
                </div>
              </div>
            </div>

            <!-- 出参展示 -->
            <div>
              <h4 class="text-sm font-bold text-gray-700 mb-3 flex items-center gap-2">
                <div class="i-fa6-solid:arrow-right-from-bracket text-green-500" />
                响应内容
                <span class="text-xs font-normal text-gray-400 ml-2">({{ record.returnTypeName }})</span>
              </h4>
              <div v-if="record.response && record.response.length > 0" class="bg-white p-3 rounded border border-gray-200 shadow-sm">
                <ParamTable :data="record.response" />
              </div>
              <div v-else class="bg-white p-4 rounded border border-gray-200 border-dashed text-center text-gray-400 text-xs">
                该接口返回简单类型数据，无结构化字段
              </div>
            </div>
          </div>
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'path'">
            <div class="flex flex-col gap-1">
              <code v-for="p in record.path" :key="p" class="text-xs bg-gray-50 text-blue-600 px-2 py-0.5 rounded border border-gray-100 w-fit">
                {{ p }}
              </code>
            </div>
          </template>
          <template v-if="column.key === 'methods'">
            <div class="flex flex-wrap gap-1">
              <a-tag v-for="m in record.methods" :key="m" :color="getMethodColor(m)" size="small">
                {{ m }}
              </a-tag>
            </div>
          </template>
          <template v-if="column.key === 'name'">
            <div class="flex flex-col">
              <span class="font-bold text-gray-700">{{ record.name }}</span>
              <span class="text-[11px] text-gray-400 font-mono">{{ record.methodName }}</span>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 空状态 -->
    <div v-if="filteredEndpoints.length === 0 && !loading" class="py-20 bg-white rounded-xl border border-dashed border-gray-200 flex flex-col items-center justify-center text-gray-400">
      <div class="i-fa6-solid:folder-open text-4xl mb-2 opacity-20" />
      <p>未找到相关接口</p>
    </div>
  </div>
</template>

<style scoped>
:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa;
}
</style>