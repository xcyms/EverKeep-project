<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getAdminVideoPageApi, deleteVideosPermanentlyApi } from '../../../api/video'
import { getImageUrl, formatSize, formatDuration } from '../../../utils/common'
import type { API } from '../../../types'

const loading = ref(false)
const dataSource = ref<API.Video[]>([])
const total = ref(0)
const previewVisible = ref(false)
const currentVideoUrl = ref('')

const queryParams = reactive({
  current: 1,
  size: 10,
  name: '',
  status: undefined as number | undefined
})

const columns = [
  { title: '预览', dataIndex: 'coverUrl', key: 'coverUrl', width: 120 },
  { title: '名称', dataIndex: 'name', key: 'name', ellipsis: true },
  { title: '时长', dataIndex: 'duration', key: 'duration', width: 100 },
  { title: '大小', dataIndex: 'size', key: 'size', width: 100 },
  { title: '是否公开', dataIndex: 'status', key: 'status', width: 100 },
  { title: '上传时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminVideoPageApi({
      current: queryParams.current,
      size: queryParams.size,
    }, {
      name: queryParams.name,
      status: queryParams.status
    })
    dataSource.value = res.records
    total.value = Number(res.total)
  } catch (err) {
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

const handleSearch = () => {
  queryParams.current = 1
  loadData()
}

const handleReset = () => {
  queryParams.name = ''
  queryParams.status = undefined
  handleSearch()
}

const handleDelete = (video: API.Video) => {
  Modal.confirm({
    title: '确定要永久删除该视频吗？',
    content: '此操作不可撤销，视频文件将从服务器彻底移除。',
    okType: 'danger',
    async onOk() {
      try {
        await deleteVideosPermanentlyApi([video.id])
        message.success('已永久删除')
        loadData()
      } catch (err) {}
    }
  })
}

const playVideo = (video: API.Video) => {
  currentVideoUrl.value = getImageUrl(video.url)
  previewVisible.value = true
}
</script>

<template>
  <div class="p-6">
    <div class="mb-4 flex justify-between items-center">
      <h1 class="text-xl font-bold m-0">视频管理</h1>
      <div class="flex gap-2">
        <a-input-search v-model:value="queryParams.name" placeholder="搜索视频名称" class="w-64" @search="handleSearch" />
        <a-select v-model:value="queryParams.status" placeholder="状态" class="w-32" allow-clear @change="handleSearch">
          <a-select-option :value="1">公开</a-select-option>
          <a-select-option :value="0">私有</a-select-option>
        </a-select>
        <a-button @click="handleReset">重置</a-button>
      </div>
    </div>

    <a-table :columns="columns as any" :data-source="dataSource" :loading="loading" :pagination="{
      current: queryParams.current,
      pageSize: queryParams.size,
      total: total,
      showSizeChanger: true,
      onChange: (page, size) => { queryParams.current = page; queryParams.size = size; loadData() }
    }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'coverUrl'">
          <div class="w-20 h-12 bg-gray-900 rounded overflow-hidden cursor-pointer flex items-center justify-center relative group" @click="playVideo(record as API.Video)">
            <img v-if="record.coverUrl" :src="getImageUrl(record.coverUrl)" class="w-full h-full object-cover" />
            <div v-else class="i-fa6-solid:video text-gray-600"></div>
            <div class="absolute bottom-1 right-1 px-1 py-0.5 rounded bg-black/60 text-white text-[10px] scale-90 origin-bottom-right">
              {{ formatDuration(record.duration) }}
            </div>
            <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity">
              <div class="i-fa6-solid:play text-white text-xs"></div>
            </div>
          </div>
        </template>
        <template v-else-if="column.key === 'duration'">
          {{ formatDuration(record.duration) }}
        </template>
        <template v-else-if="column.key === 'size'">
          {{ formatSize(record.size) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status.code === 1 ? 'green' : 'gray'">{{ record.status.desc }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="flex gap-2">
            <a-button type="link" size="small" @click="playVideo(record as API.Video)">播放</a-button>
            <a-button type="link" danger size="small" @click="handleDelete(record as API.Video)">删除</a-button>
          </div>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="previewVisible" :footer="null" width="800px" destroyOnClose centered :bodyStyle="{ padding: 0 }">
      <div class="bg-black aspect-video flex items-center justify-center">
        <video :src="currentVideoUrl" controls autoplay class="max-w-full max-h-full"></video>
      </div>
    </a-modal>
  </div>
</template>