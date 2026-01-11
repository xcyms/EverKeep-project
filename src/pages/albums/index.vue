<script setup lang="ts">
import { ref, reactive, watch, h, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { ExclamationCircleOutlined, PictureOutlined, ClockCircleOutlined } from '@ant-design/icons-vue'
import { getMyAlbumsApi } from '../../api/album'

// --- 接口定义 ---
interface Album {
  id: number
  name: string
  cover: string
  imageCount: number
  createTime: string
  description: string
}

interface Image {
  id: number
  url: string
  name: string
  albumId: number
}

// --- 模拟数据 ---
const mockAlbums = Array.from({ length: 25 }).map((_, i) => ({
  id: i + 1,
  name: i === 0 ? '生活记录' : (i === 1 ? '工作周报' : (i === 2 ? '旅行足迹' : `我的相册 ${i + 1}`)),
  cover: `https://picsum.photos/seed/${i + 100}/400/300`,
  imageCount: Math.floor(Math.random() * 100) + 5,
  createTime: new Date(Date.now() - i * 86400000 * 5).toLocaleString(),
  description: `这是关于 ${i + 1} 的一些描述信息...`
}))

const mockImagesByAlbum = (albumId: number): Image[] => 
  Array.from({ length: 12 }).map((_, i) => ({
    id: albumId * 100 + i,
    url: `https://picsum.photos/seed/${albumId * 100 + i}/400/300`,
    name: `图片_${albumId}_${i + 1}.jpg`,
    albumId
  }))

// --- 状态变量 ---
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const displayedAlbums = ref<Album[]>([])

const queryParams = reactive({
  keyword: '',
  sort: 'time_desc',
  current: 1,
  pageSize: 8,
})

// --- 预览相关 ---
const previewVisible = ref(false)
const currentAlbum = ref<Album | null>(null)
const albumImages = ref<Image[]>([])
const loadingImages = ref(false)

// --- 核心逻辑：获取数据 ---
const loadData = async (isRefresh = false) => {
  if (isRefresh) {
    queryParams.current = 1
    loading.value = true
    displayedAlbums.value = []
  } else {
    loadingMore.value = true
  }

  try {
    const list = await getMyAlbumsApi() as Album[]
    
    // 前端过滤和排序逻辑（如果后端没实现的话）
    let filteredList = [...list]
    if (queryParams.keyword) {
      filteredList = filteredList.filter(a => a.name.toLowerCase().includes(queryParams.keyword.toLowerCase()))
    }

    // 排序逻辑
    filteredList.sort((a, b) => {
      if (queryParams.sort === 'time_asc') return new Date(a.createTime).getTime() - new Date(b.createTime).getTime()
      if (queryParams.sort === 'time_desc') return new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      return 0
    })

    // 分页处理
    const start = (queryParams.current - 1) * queryParams.pageSize
    const end = start + queryParams.pageSize
    const pageData = filteredList.slice(start, end)

    if (isRefresh) {
      displayedAlbums.value = pageData
    } else {
      displayedAlbums.value.push(...pageData)
    }

    hasMore.value = end < filteredList.length
  } catch (error) {
    console.error('加载相册失败', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

onMounted(() => {
  loadData(true)
})

// 监听筛选和排序变化
watch(() => [queryParams.sort], () => loadData(true), { immediate: true })

// 搜索防抖
let timer: any = null
watch(() => queryParams.keyword, () => {
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => loadData(true), 500)
})

const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  queryParams.current++
  loadData()
}

// --- 预览功能 ---
const handlePreview = async (album: Album) => {
  currentAlbum.value = album
  previewVisible.value = true
  loadingImages.value = true
  // 模拟加载相册内图片
  await new Promise(resolve => setTimeout(resolve, 500))
  albumImages.value = mockImagesByAlbum(album.id)
  loadingImages.value = false
}

// --- 设置封面 ---
const handleSetCover = (img: Image) => {
  Modal.confirm({
    title: '设为封面',
    icon: h(ExclamationCircleOutlined),
    content: '确定要将这张图片设为相册封面吗？',
    onOk() {
      if (currentAlbum.value) {
        currentAlbum.value.cover = img.url
        message.success('封面设置成功')
      }
    }
  })
}

// --- 删除相册 ---
const handleDeleteAlbum = (album: Album) => {
  Modal.confirm({
    title: '确认删除',
    icon: h(ExclamationCircleOutlined, { style: 'color: #ff4d4f' }),
    content: `确定要删除相册 "${album.name}" 吗？此操作不可撤销。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk() {
      message.success('相册已删除')
      displayedAlbums.value = displayedAlbums.value.filter(a => a.id !== album.id)
    }
  })
}
</script>

<template>
  <div class="flex flex-col gap-6">
    <!-- 顶部工具栏 -->
    <div class="bg-white p-5 rounded-xl shadow-sm border border-gray-100 flex flex-wrap items-center justify-between gap-4">
      <div class="flex items-center gap-4 flex-1 min-w-[300px]">
        <a-input-search
          v-model:value="queryParams.keyword"
          placeholder="搜索相册名称..."
          allow-clear
          enter-button
          class="max-w-xs"
        />
        <a-select v-model:value="queryParams.sort" class="w-44">
          <template #suffixIcon><div class="i-fa6-solid:sort text-gray-400 mr-1" /></template>
          <a-select-option value="time_desc">按创建时间 (新→旧)</a-select-option>
          <a-select-option value="time_asc">按创建时间 (旧→新)</a-select-option>
          <a-select-option value="count_desc">按图片数量 (多→少)</a-select-option>
          <a-select-option value="count_asc">按图片数量 (少→多)</a-select-option>
        </a-select>
      </div>
      <a-button type="primary" class="toolbar-btn flex items-center gap-2">
        <template #icon><div class="i-fa6-solid:plus" /></template>
        新建相册
      </a-button>
    </div>

    <!-- 相册网格 -->
    <a-spin :spinning="loading">
      <div v-if="displayedAlbums.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        <div 
          v-for="album in displayedAlbums" 
          :key="album.id"
          class="group bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300"
        >
          <!-- 相册封面 -->
          <div class="relative aspect-[4/3] overflow-hidden bg-gray-100">
            <img 
              :src="album.cover" 
              class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
              alt="cover"
            />
            <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-3">
              <a-button type="primary" shape="round" @click="handlePreview(album)">
                <template #icon><PictureOutlined /></template>
                查看内容
              </a-button>
            </div>
            <div class="absolute top-3 right-3">
              <a-dropdown :trigger="['click']">
                <div class="w-8 h-8 bg-white/90 backdrop-blur rounded-full flex items-center justify-center cursor-pointer hover:bg-white shadow-sm" @click.stop>
                  <div class="i-fa6-solid:ellipsis-vertical text-gray-600" />
                </div>
                <template #overlay>
                  <a-menu>
                    <a-menu-item key="edit">
                      <template #icon><div class="i-fa6-solid:pen-to-square" /></template>
                      编辑名称
                    </a-menu-item>
                    <a-menu-item key="delete" danger @click="handleDeleteAlbum(album)">
                      <template #icon><div class="i-fa6-solid:trash-can" /></template>
                      删除相册
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
            <div class="absolute bottom-3 left-3 px-2 py-1 bg-black/50 backdrop-blur rounded text-[10px] text-white flex items-center gap-1">
              <PictureOutlined /> {{ album.imageCount }} 张图片
            </div>
          </div>

          <!-- 相册信息 -->
          <div class="p-4">
            <h4 class="text-base font-bold text-gray-800 mb-1 truncate">{{ album.name }}</h4>
            <div class="flex items-center justify-between text-xs text-gray-400">
              <span class="flex items-center gap-1"><ClockCircleOutlined /> {{ album.createTime.split(' ')[0] }}</span>
              <span class="hover:text-blue-500 cursor-pointer transition-colors">查看详情</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty v-else-if="!loading" description="未找到相关相册" class="my-24" />
    </a-spin>

    <!-- 加载更多 -->
    <div v-if="displayedAlbums.length > 0" class="flex justify-center py-10">
      <a-button 
        v-if="hasMore" 
        :loading="loadingMore" 
        @click="handleLoadMore"
        class="px-10 h-11 rounded-full border-gray-200 text-gray-600 hover:text-blue-500 hover:border-blue-500 transition-all"
      >
        {{ loadingMore ? '正在努力加载...' : '查看更多相册' }}
      </a-button>
      <div v-else class="text-gray-300 text-sm flex items-center gap-3">
        <span class="w-12 h-px bg-gray-100"></span>
        已经到底啦
        <span class="w-12 h-px bg-gray-100"></span>
      </div>
    </div>

    <!-- 相册内容预览弹窗 -->
    <a-modal
      v-model:visible="previewVisible"
      :title="currentAlbum?.name"
      width="1000px"
      :footer="null"
      centered
      destroyOnClose
    >
      <div class="min-h-[400px]">
        <div v-if="loadingImages" class="flex flex-col items-center justify-center py-20 gap-4">
          <a-spin size="large" />
          <span class="text-gray-400">加载图片中...</span>
        </div>
        <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 p-2">
          <div 
            v-for="img in albumImages" 
            :key="img.id" 
            class="group relative aspect-square rounded-lg overflow-hidden border border-gray-100"
          >
            <a-image :src="img.url" class="w-full h-full object-cover" />
            <div class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
              <a-button type="primary" size="small" @click="handleSetCover(img)">
                设为封面
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
:deep(.ant-image) {
  width: 100%;
  height: 100%;
}
</style>