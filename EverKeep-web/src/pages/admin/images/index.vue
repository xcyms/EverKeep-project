<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getAdminImagesApi, deleteImagesApi, updateImagesStatusApi } from '../../../api/image'
import type { API } from '../../../types'
import { getImageUrl, formatSize } from '../../../utils/common'

const loading = ref(false)
const imageList = ref<API.Image[]>([])
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const selectedRowKeys = ref<number[]>([])

const queryParams = reactive({
  name: '',
  userId: undefined as number | undefined,
  status: undefined as number | undefined
})

const columns = [
  { title: '预览', dataIndex: 'thumbnailUrl', key: 'thumbnailUrl', width: 100 },
  { title: '名称', dataIndex: 'name', key: 'name', ellipsis: true, width: 150 },
  { title: '所有者 ID', dataIndex: 'userId', key: 'userId', width: 100 },
  { title: '大小', dataIndex: 'size', key: 'size', width: 100 },
  { title: '是否公开', dataIndex: 'status', key: 'status', width: 100 },
  { title: '上传时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
]

const loadData = async (page = current.value, size = pageSize.value) => {
  loading.value = true
  try {
    const res = await getAdminImagesApi(
      { current: Number(page), size: Number(size), column: 'create_time', asc: false },
      { name: queryParams.name, userId: queryParams.userId, status: queryParams.status }
    )
    imageList.value = res.records || []
    total.value = Number(res.total)
    current.value = Number(res.current)
    pageSize.value = Number(res.size)
  } catch (error) {
    console.error('加载图片列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { current.value = 1; loadData() }
const handleReset = () => {
  queryParams.name = ''; queryParams.userId = undefined; queryParams.status = undefined
  current.value = 1; loadData()
}
const handleTableChange = (pagination: any) => { loadData(pagination.current, pagination.pageSize) }
const onSelectChange = (keys: any[]) => { selectedRowKeys.value = keys }

const handleDelete = (id: number) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这张图片吗？',
    onOk: async () => {
      await deleteImagesApi([id])
      message.success('删除成功')
      loadData()
    }
  })
}

const handleBatchDelete = () => {
  Modal.confirm({
    title: '批量删除',
    content: `确定删除选中的 ${selectedRowKeys.value.length} 张图片吗？`,
    onOk: async () => {
      await deleteImagesApi(selectedRowKeys.value)
      message.success('批量删除成功')
      selectedRowKeys.value = []
      loadData()
    }
  })
}

const toggleStatus = async (image: API.Image) => {
  const newStatus = image.status.code === 1 ? 0 : 1
  await updateImagesStatusApi([image.id], newStatus)
  message.success('状态更新成功')
  loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
      <a-form layout="inline" :model="queryParams" @finish="handleSearch">
        <a-form-item label="名称"><a-input v-model:value="queryParams.name" placeholder="搜索名称" allow-clear /></a-form-item>
        <a-form-item label="用户ID"><a-input-number v-model:value="queryParams.userId" placeholder="ID" style="width: 100px" /></a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryParams.status" placeholder="全部" style="width: 100px" allow-clear>
            <a-select-option :value="1">公开</a-select-option>
            <a-select-option :value="0">私有</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
          <a-button class="ml-2" @click="handleReset">重置</a-button>
        </a-form-item>
      </a-form>
    </div>

    <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
      <div class="mb-4 flex justify-between items-center">
        <h2 class="text-lg font-bold m-0 flex items-center gap-2">
          <div class="i-fa6-solid:images text-blue-500" /> 照片管理
        </h2>
        <a-button danger :disabled="selectedRowKeys.length === 0" @click="handleBatchDelete">批量删除</a-button>
      </div>

      <a-table
        :columns="columns as any"
        :data-source="imageList"
        :loading="loading"
        :pagination="{ current, pageSize, total, showSizeChanger: true }"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        :row-key="record => record.id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'thumbnailUrl'">
            <a-image :width="60" :height="60" class="rounded object-cover" :src="getImageUrl(record.url || record.thumbnailUrl)" />
          </template>
          <template v-else-if="column.key === 'size'">{{ formatSize(record.size) }}</template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status.code === 1 ? 'green' : 'orange'">{{ record.status.desc }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="toggleStatus(record as API.Image)">{{ record.status.code === 1 ? '设为私有' : '设为公开' }}</a-button>
            <a-button type="link" danger size="small" @click="handleDelete(record.id)">删除</a-button>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>