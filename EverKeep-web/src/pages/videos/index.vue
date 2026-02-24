<script setup lang="ts">
import { ref, reactive, watch, h, onMounted, computed } from 'vue'
import { message, Modal, Select } from 'ant-design-vue'
import { FolderOutlined, PlayCircleOutlined } from '@ant-design/icons-vue'
import { getVideoPageApi, deleteVideosApi, updateVideoStatusApi, batchMoveVideosApi } from '../../api/video'
import { getMyAlbumsApi } from '../../api/album'
import { getImageUrl, formatSize, formatDuration } from '../../utils/common'
import type { API } from '../../types'

// --- 状态变量 ---
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const selectedIds = ref<number[]>([])
const displayedVideos = ref<API.Video[]>([])
const albumList = ref<API.Album[]>([])
const albumNameMap = computed(() => Object.fromEntries(albumList.value.map(a => [a.id, a.name])))
const previewVisible = ref(false)
const currentVideoUrl = ref('')

const queryParams = reactive({
  albumId: null as number | null,
  status: 'all',
  sort: 'createTime_desc',
  current: 1,
  size: 12,
})

// --- 核心逻辑：获取数据 ---
const loadAlbums = async () => {
  try {
    const res = await getMyAlbumsApi()
    albumList.value = res || []
  } catch (err) {}
}

const loadData = async (isRefresh = false) => {
  if (isRefresh) {
    queryParams.current = 1
    loading.value = true
    displayedVideos.value = []
  } else {
    loadingMore.value = true
  }

  try {
    const res = await getVideoPageApi({
      current: queryParams.current,
      size: queryParams.size,
      column: queryParams.sort.split('_')[0],
      asc: queryParams.sort.split('_')[1] === 'asc'
    }, {
      albumId: queryParams.albumId,
      status: queryParams.status === 'all' ? null : queryParams.status
    })
    const list = res.records || []
    if (isRefresh) {
      displayedVideos.value = list
    } else {
      displayedVideos.value.push(...list)
    }

    hasMore.value = displayedVideos.value.length < (res.total || 0)
  } catch (error) {
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

onMounted(() => {
  loadAlbums()
})

watch(() => [queryParams.albumId, queryParams.status, queryParams.sort], () => {
  loadData(true)
}, { immediate: true })

const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  queryParams.current++
  loadData()
}

// --- 操作方法 ---
const toggleSelect = (id: number) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const handleSelectAll = () => {
  if (selectedIds.value.length === displayedVideos.value.length) {
    selectedIds.value = []
  } else {
    selectedIds.value = displayedVideos.value.map(v => v.id)
  }
}

const handleDelete = (ids: number[]) => {
  Modal.confirm({
    title: `确定要删除选中的 ${ids.length} 个视频吗？`,
    content: '删除后将移至回收站。',
    okType: 'danger',
    async onOk() {
      try {
        await deleteVideosApi(ids)
        message.success('已移至回收站')
        selectedIds.value = []
        loadData(true)
      } catch (err) {}
    },
  })
}

const handleBatchStatusUpdate = (status: number) => {
  const statusName = status === 1 ? '公开' : '私有'
  Modal.confirm({
    title: `确定要将选中的 ${selectedIds.value.length} 个视频设为${statusName}吗？`,
    async onOk() {
      try {
        await updateVideoStatusApi(selectedIds.value, status)
        message.success(`已成功设为${statusName}`)
        selectedIds.value = []
        loadData(true)
      } catch (err) {}
    },
  })
}

const handleBatchMove = () => {
  let selectedAlbumId: number | null = null
  Modal.confirm({
    title: '批量移动到相册',
    icon: h(FolderOutlined, { style: 'color: #1890ff' }),
    width: 420,
    content: h('div', { class: 'pt-4' }, [
      h('div', { class: 'mb-4 p-3 bg-blue-50 rounded-lg text-blue-700 text-sm' }, 
        `已选中 ${selectedIds.value.length} 个视频，请选择目标相册：`
      ),
      h(Select, {
        class: 'w-full',
        placeholder: '请选择相册',
        onChange: (val: any) => { selectedAlbumId = val }
      }, {
        default: () => albumList.value.map(album => 
          h(Select.Option, { value: album.id }, { 
            default: () => h('div', { class: 'flex items-center gap-2' }, [h(FolderOutlined), album.name]) 
          })
        )
      })
    ]),
    async onOk() {
      if (!selectedAlbumId) {
        message.warning('请选择目标相册')
        return Promise.reject()
      }
      try {
        await batchMoveVideosApi(selectedIds.value, selectedAlbumId)
        message.success('批量移动成功')
        selectedIds.value = []
        loadData(true)
      } catch (err) {
        return Promise.reject()
      }
    },
  })
}

const playVideo = (video: API.Video) => {
  currentVideoUrl.value = getImageUrl(video.url)
  previewVisible.value = true
}
</script>

<template>
  <div class="p-6 max-w-7xl mx-auto min-h-screen bg-gray-50/50">
    <!-- 顶部工具栏 -->
    <div class="mb-8 flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div class="flex items-center gap-3">
        <div class="p-2 bg-blue-600 rounded-lg shadow-lg shadow-blue-200">
          <div class="i-fa6-solid:video text-white text-xl"></div>
        </div>
        <div>
          <h1 class="text-2xl font-bold text-gray-800 m-0">我的视频</h1>
          <p class="text-gray-400 text-sm mt-1">管理您上传的所有视频文件</p>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <a-select v-model:value="queryParams.albumId as any" placeholder="所有相册" class="w-40" allow-clear>
          <a-select-option v-for="album in albumList" :key="album.id" :value="album.id">
            <div class="flex items-center gap-2">
              <FolderOutlined /> {{ album.name }}
            </div>
          </a-select-option>
        </a-select>
        <a-select v-model:value="queryParams.status" class="w-32">
          <a-select-option value="all">全部状态</a-select-option>
          <a-select-option value="1">公开</a-select-option>
          <a-select-option value="0">私有</a-select-option>
        </a-select>
        <a-select v-model:value="queryParams.sort" class="w-40">
          <a-select-option value="createTime_desc">最新上传</a-select-option>
          <a-select-option value="createTime_asc">最早上传</a-select-option>
          <a-select-option value="size_desc">文件最大</a-select-option>
          <a-select-option value="size_asc">文件最小</a-select-option>
        </a-select>
      </div>
    </div>

    <!-- 列表展示 -->
    <div v-if="loading && displayedVideos.length === 0" class="py-20 flex flex-col items-center justify-center">
      <a-spin size="large" />
      <span class="mt-4 text-gray-400">正在加载视频库...</span>
    </div>

    <div v-else-if="displayedVideos.length === 0" class="py-32 flex flex-col items-center justify-center bg-white rounded-3xl border border-dashed border-gray-200">
      <div class="w-24 h-24 bg-gray-50 rounded-full flex items-center justify-center mb-4">
        <div class="i-fa6-solid:video-slash text-gray-200 text-4xl"></div>
      </div>
      <p class="text-gray-400">暂无视频，快去上传吧</p>
      <a-button type="primary" class="mt-4 rounded-xl px-8" @click="$router.push('/upload')">去上传</a-button>
    </div>

    <div v-else>
      <!-- 批量操作工具栏 -->
      <div class="mb-4 flex items-center justify-between bg-white p-3 rounded-xl border border-gray-100 shadow-sm">
        <div class="flex items-center gap-4">
          <a-checkbox :checked="selectedIds.length > 0 && selectedIds.length === displayedVideos.length" 
                      :indeterminate="selectedIds.length > 0 && selectedIds.length < displayedVideos.length"
                      @change="handleSelectAll">
            <span class="text-gray-600">全选 ({{ selectedIds.length }}/{{ displayedVideos.length }})</span>
          </a-checkbox>
        </div>
        <div class="flex items-center gap-2 transition-all" :class="{ 'opacity-50 pointer-events-none': selectedIds.length === 0 }">
          <a-button size="small" @click="handleBatchMove">
            <template #icon><FolderOutlined /></template>
            移动到相册
          </a-button>
          <a-dropdown>
            <a-button size="small">
              设置状态 <div class="i-fa6-solid:chevron-down text-[10px] ml-1"></div>
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="handleBatchStatusUpdate(1)">设为公开</a-menu-item>
                <a-menu-item @click="handleBatchStatusUpdate(0)">设为私密</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-button size="small" danger @click="handleDelete(selectedIds)">
            <template #icon><div class="i-fa6-solid:trash-can mr-1"></div></template>
            批量删除
          </a-button>
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        <div v-for="video in displayedVideos" :key="video.id" 
             class="group relative bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-xl transition-all duration-300 border border-gray-100"
             :class="{ 'ring-2 ring-blue-500 shadow-blue-100': selectedIds.includes(video.id) }">
          
          <!-- 视频封面预览 -->
          <div class="relative aspect-video bg-gray-900 overflow-hidden cursor-pointer" @click="playVideo(video)">
            <img v-if="video.coverUrl" :src="getImageUrl(video.coverUrl)" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
            <div v-else class="w-full h-full flex items-center justify-center">
              <div class="i-fa6-solid:video text-gray-700 text-3xl"></div>
            </div>
            
            <div class="absolute inset-0 bg-black/20 group-hover:bg-black/40 flex items-center justify-center transition-colors">
              <PlayCircleOutlined class="text-white text-5xl opacity-0 group-hover:opacity-100 transition-opacity scale-90 group-hover:scale-100 duration-300" />
            </div>

            <div class="absolute bottom-2 right-2 px-1.5 py-0.5 rounded bg-black/60 text-white text-[10px] font-medium backdrop-blur-sm">
              {{ formatDuration(video.duration) }}
            </div>

            <div class="absolute top-3 left-3 z-10 opacity-0 group-hover:opacity-100 transition-opacity" :class="{ 'opacity-100': selectedIds.includes(video.id) }">
              <div @click.stop="toggleSelect(video.id)" 
                   class="w-6 h-6 rounded-lg border-2 flex items-center justify-center transition-all"
                   :class="selectedIds.includes(video.id) ? 'bg-blue-500 border-blue-500 shadow-lg' : 'bg-white/20 border-white backdrop-blur-md'">
                <div v-if="selectedIds.includes(video.id)" class="i-fa6-solid:check text-white text-xs"></div>
              </div>
            </div>

            <div class="absolute top-3 right-3 px-2 py-0.5 rounded-full text-[10px] font-bold backdrop-blur-md shadow-sm"
                 :class="video.status?.code === 1 ? 'bg-green-500/80 text-white' : 'bg-gray-500/80 text-white'">
              {{ video.status?.code === 1 ? '公开' : '私密' }}
            </div>
          </div>

          <!-- 视频信息 -->
          <div class="p-4">
            <h3 class="text-sm font-bold text-gray-800 truncate mb-1" :title="video.name">{{ video.name }}</h3>
            <div class="flex items-center justify-between text-[11px] text-gray-400">
              <span>{{ formatSize(video.size) }} · {{ video.type.toUpperCase() }}</span>
              <span>{{ video.createTime?.split(' ')[0] }}</span>
            </div>
            <div class="mt-1 text-[11px] text-gray-500 truncate flex items-center gap-1">
              <span class="i-fa6-solid:folder-closed"></span>
              <span>{{ albumNameMap[video.albumId] || '未分配相册' }}</span>
            </div>
          </div>

          <div class="absolute bottom-3 right-3 opacity-0 group-hover:opacity-100 transition-opacity">
            <a-dropdown position="bottomRight">
              <div class="w-8 h-8 rounded-full bg-white shadow-md border border-gray-100 flex items-center justify-center hover:bg-gray-50 cursor-pointer">
                <div class="i-fa6-solid:ellipsis-vertical text-gray-400"></div>
              </div>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="playVideo(video)">播放</a-menu-item>
                  <a-menu-item danger @click="handleDelete([video.id])">删除</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
        </div>
      </div>

      <!-- 加载更多 / 底部状态 -->
      <div v-if="displayedVideos.length > 0" class="flex flex-col items-center py-12 gap-4">
        <template v-if="hasMore">
          <a-button :loading="loadingMore" @click="handleLoadMore" class="rounded-xl px-10 h-11 border-blue-200 text-blue-600 hover:border-blue-500 hover:text-blue-500 transition-all">
            {{ loadingMore ? '正在努力加载...' : '加载更多视频' }}
          </a-button>
        </template>
        <div v-else class="text-gray-400 text-xs flex items-center gap-3">
          <div class="w-12 h-[1px] bg-gray-200"></div>
          <span class="font-medium tracking-wider">没有更多视频了</span>
          <div class="w-12 h-[1px] bg-gray-200"></div>
        </div>
      </div>
    </div>

    <!-- 视频预览 Modal -->
    <a-modal v-model:open="previewVisible" :footer="null" width="800px" destroyOnClose centered :bodyStyle="{ padding: 0 }">
      <div class="bg-black aspect-video flex items-center justify-center">
        <video :src="currentVideoUrl" controls autoplay class="max-w-full max-h-full"></video>
      </div>
    </a-modal>
  </div>
</template>