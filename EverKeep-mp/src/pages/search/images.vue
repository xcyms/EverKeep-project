<script setup lang="ts">
import type { SortOption } from '@/types/common'
import type { ImageItem, VideoItem } from '@/types/page'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { onMounted, ref, watch } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import SortSheet from '@/components/common/SortSheet.vue'
import ImageWaterfall from '@/components/ImageWaterfall.vue'
import { useListPagination } from '@/composables/usePagination'
import { PAGINATION } from '@/utils/constants'

definePage({
  name: 'images',
  style: {
    navigationBarTitleText: '相册图片',
    navigationStyle: 'custom',
  },
})

const router = useRouter()
const user = useAuthStore()
const toast = useToast()
const message = useMessage()
const { statusBarHeight, safeAreaInsetsBottom, menuButtonRight } = useSystemInfo()

const albumId = ref<string | null>(null)
const searchQuery = ref('')
const order = ref<'newest' | 'earliest' | 'utmost' | 'least'>('newest')
const showSortSheet = ref(false)
const showPublicSheet = ref(false)
const showMoveSheet = ref(false)
const albumOptions = ref<{ name: string; value: string | number }[]>([])
const uploading = ref(false)
const { isDark } = useManualTheme()

// 选择模式状态
const isSelectionMode = ref(false)
const selectedIds = ref(new Set<string | number>())

const orderOptions: SortOption[] = [
  { name: '最新发布', value: 'newest', subname: '按上传时间从新到旧', icon: 'i-solar-clock-circle-bold-duotone' },
  { name: '最早发布', value: 'earliest', subname: '按上传时间从旧到新', icon: 'i-solar-history-bold-duotone' },
  { name: '最大尺寸', value: 'utmost', subname: '按文件大小从大到小', icon: 'i-solar-database-bold-duotone' },
  { name: '最小尺寸', value: 'least', subname: '按文件大小从小到大', icon: 'i-solar-database-linear' },
]

const currentTab = ref<'image' | 'video'>('image')

// 使用 useListPagination 管理图片列表
const {
  list: images,
  loading,
  hasMore,
  loadMore,
  refresh,
  reset,
} = useListPagination<ImageItem>({
  fetchFn: async (currentPage) => {
    if (!albumId.value) {
      return {
        data: [],
        current_page: currentPage,
        last_page: 0,
        total: 0,
        per_page: PAGINATION.DEFAULT_PAGE_SIZE,
      }
    }

    const sortMap: Record<string, { column: string; asc: boolean }> = {
      newest: { column: 'create_time', asc: false },
      earliest: { column: 'create_time', asc: true },
      utmost: { column: 'size', asc: false },
      least: { column: 'size', asc: true },
    }
    const { column, asc } = sortMap[order.value] || sortMap.newest

    try {
      const res = await Apis.everkeep.imagePage({
        params: {
          current: currentPage,
          size: PAGINATION.DEFAULT_PAGE_SIZE,
          column,
          asc,
        },
        data: {
          albumId: albumId.value,
          name: searchQuery.value || undefined,
        },
      })

      if (res.code === 200 && res.data) {
        return {
          data: res.data.records || [],
          current_page: res.data.current || currentPage,
          last_page: res.data.pages || 1,
          total: res.data.total || 0,
          per_page: res.data.size || PAGINATION.DEFAULT_PAGE_SIZE,
        }
      }
      throw new Error(res.message || '获取数据失败')
    } catch (e) {
      console.error('Failed to fetch album images:', e)
      return {
        data: [],
        current_page: currentPage,
        last_page: 0,
        total: 0,
        per_page: PAGINATION.DEFAULT_PAGE_SIZE,
      }
    }
  },
  pageSize: PAGINATION.DEFAULT_PAGE_SIZE,
  immediate: false,
})

const {
  list: videos,
  loading: videoLoading,
  hasMore: videoHasMore,
  loadMore: loadMoreVideos,
  refresh: refreshVideos,
  reset: resetVideos,
} = useListPagination<VideoItem>({
  fetchFn: async (currentPage) => {
    if (!albumId.value) {
      return { data: [], current_page: currentPage, last_page: 0, total: 0, per_page: PAGINATION.DEFAULT_PAGE_SIZE }
    }
    const sortMap: Record<string, { column: string; asc: boolean }> = {
      newest: { column: 'create_time', asc: false },
      earliest: { column: 'create_time', asc: true },
      utmost: { column: 'size', asc: false },
      least: { column: 'size', asc: true },
    }
    const { column, asc } = sortMap[order.value] || sortMap.newest
    try {
      const res = await Apis.everkeep.videoPage({
        params: { current: currentPage, size: PAGINATION.DEFAULT_PAGE_SIZE, column, asc },
        data: { albumId: albumId.value, name: searchQuery.value || undefined }
      })
      if (res.code === 200 && res.data) {
        return {
          data: res.data.records || [],
          current_page: res.data.current || currentPage,
          last_page: res.data.pages || 1,
          total: res.data.total || 0,
          per_page: res.data.size || PAGINATION.DEFAULT_PAGE_SIZE,
        }
      }
      throw new Error(res.message || '获取视频失败')
    } catch (e) {
      console.error('Failed to fetch album videos:', e)
      return { data: [], current_page: currentPage, last_page: 0, total: 0, per_page: PAGINATION.DEFAULT_PAGE_SIZE }
    }
  },
  pageSize: PAGINATION.DEFAULT_PAGE_SIZE,
  immediate: false,
})

const videoLeft = computed(() => videos.value.filter((_, i) => i % 2 === 0))
const videoRight = computed(() => videos.value.filter((_, i) => i % 2 !== 0))

const showSearch = ref(false)

// 处理搜索
function handleSearch() {
  uni.pageScrollTo({ scrollTop: 0, duration: 200 })
  if (currentTab.value === 'image') {
    reset()
    refresh()
  } else {
    resetVideos()
    refreshVideos()
  }
}

function toggleSearch() {
  showSearch.value = !showSearch.value
  if (!showSearch.value && searchQuery.value) {
    searchQuery.value = ''
    handleSearch()
  }
}

function toggleTab() {
  currentTab.value = currentTab.value === 'image' ? 'video' : 'image'
}

// 处理排序选择
function handleSortSelect(option: SortOption) {
  order.value = option.value as 'newest' | 'earliest' | 'utmost' | 'least'
}

// 监听排序变化
watch(order, () => {
  uni.pageScrollTo({ scrollTop: 0, duration: 200 })
  if (currentTab.value === 'image') {
    reset()
    refresh()
  } else {
    resetVideos()
    refreshVideos()
  }
})

watch(currentTab, (tab) => {
  uni.pageScrollTo({ scrollTop: 0, duration: 200 })
  if (tab === 'image') {
    reset()
    refresh()
  } else {
    resetVideos()
    refreshVideos()
  }
})

const showUploadSheet = ref(false)
const uploadOptions = [
  { name: '上传图片', icon: 'i-solar-gallery-bold-duotone', color: '#007aff' },
  { name: '上传视频', icon: 'i-solar-videocamera-record-bold-duotone', color: '#ff9500' },
]

function handleUploadClick() {
  showUploadSheet.value = true
}

function handleUploadSelect({ index }: { item: any; index: number }) {
  showUploadSheet.value = false
  if (index === 0) {
    handleImageUpload()
  } else {
    handleVideoUpload()
  }
}

// 处理图片上传
function handleImageUpload() {
  if (uploading.value) return

  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res: any) => {
      const tempFilePath = res.tempFilePaths[0]
      uploadFile(tempFilePath, 'image')
    },
  })
}

// 处理视频上传
function handleVideoUpload() {
  if (uploading.value) return

  uni.chooseVideo({
    sourceType: ['album', 'camera'],
    compressed: true,
    maxDuration: 60,
    success: async (res: any) => {
      const tempFilePath = res.tempFilePath
      uploadFile(tempFilePath, 'video')
    },
  })
}

// 通用上传逻辑
async function uploadFile(filePath: string, type: 'image' | 'video') {
  try {
    uploading.value = true
    uni.showLoading({ title: '正在上传...', mask: true })

    uni.uploadFile({
      url: `${import.meta.env.VITE_API_BASE_URL}/file/upload`,
      filePath,
      name: 'file',
      header: {
        Authorization: `Bearer ${user.token}`,
        Accept: 'application/json',
      },
      formData: {
        albumId: String(albumId.value),
        category: type,
      },
      success: (uploadRes) => {
        const data = JSON.parse(uploadRes.data)
        if (data.code === 200) {
          toast.success('上传成功')
          setTimeout(() => {
            if (type === 'image') {
              reset()
              refresh()
            } else {
              resetVideos()
              refreshVideos()
            }
          }, 800)
        } else {
          toast.error(data.message || '上传失败')
        }
      },
      fail: (err) => {
        console.error('uni.uploadFile error:', err)
        toast.error('网络请求失败')
      },
      complete: () => {
        uploading.value = false
        uni.hideLoading()
      },
    })
  } catch (error) {
    console.error('Upload catch error:', error)
    toast.error('上传准备失败')
    uploading.value = false
    uni.hideLoading()
  }
}

const publicOptions = [
  { name: '设为私有', color: '#2979ff' },
  { name: '设为公开', color: '#666' },
]

// 批量操作处理
function handleBatchPublic() {
  if (selectedIds.value.size === 0) return
  showPublicSheet.value = true
}

function handlePublicSelect({ item, index }: { item: { name: string; value?: any }; index: number }) {
  message.confirm({
    title: '权限设置',
    msg: `确定将选中的 ${selectedIds.value.size} 个${currentTab.value === 'image' ? '图片' : '视频'}${item.name}吗？`,
  }).then(async () => {
    try {
      if (currentTab.value === 'image') {
        await Apis.everkeep.updateStatus({
          data: {
            ids: Array.from(selectedIds.value),
            status: index,
          },
        })
        // 本地更新图片状态
        images.value = images.value.map(img => {
          if (selectedIds.value.has(img.id)) {
            return { ...img, status: { ...img.status, code: index } }
          }
          return img
        })
      } else {
        await (Apis as any).everkeep.videoUpdateStatus({
          data: {
            ids: Array.from(selectedIds.value),
            status: index,
          },
        })
        // 本地更新视频状态
        videos.value = videos.value.map(v => {
          if (selectedIds.value.has(v.id)) {
            return { ...v, status: { ...v.status, code: index } }
          }
          return v
        })
      }
      toast.success('设置成功')
      selectedIds.value.clear()
      isSelectionMode.value = false
    } catch (error) {
      console.error('Failed to update status:', error)
      toast.error('设置失败')
    }
  }).catch(() => {})
}

function handleBatchMove() {
  if (selectedIds.value.size === 0) return

  // 加载相册列表
  uni.showLoading({ title: '加载相册...' })
  Apis.everkeep.albumList({
    params: {
      name: searchQuery.value || undefined,
    },
  }).then((res) => {
    if (res.code === 200 && res.data) {
      const albums = res.data || []
      // 过滤掉当前相册
      albumOptions.value = albums
        .filter((a: any) => String(a.id) !== String(albumId.value))
        .map((a: any) => ({
          name: a.name,
          value: a.id,
        }))

      if (albumOptions.value.length === 0) {
        toast.info('没有可选的其他相册')
        return
      }
      showMoveSheet.value = true
    } else {
      toast.error('加载相册失败')
    }
  }).catch((err) => {
    console.error('Fetch albums error:', err)
    toast.error('网络错误')
  }).finally(() => {
    uni.hideLoading()
  })
}

function handleMoveSelect({ item }: { item: { name: string; value?: any } }) {
  const targetAlbumId = item.value
  message.confirm({
    title: '移动确认',
    msg: `确定将选中的 ${selectedIds.value.size} 个${currentTab.value === 'image' ? '图片' : '视频'}移动到“${item.name}”吗？`,
  }).then(async () => {
    uni.showLoading({ title: '正在移动...', mask: true })
    try {
      let res
      if (currentTab.value === 'image') {
        res = await Apis.everkeep.batchMove({
          data: {
            albumId: targetAlbumId,
            ids: Array.from(selectedIds.value),
          },
        })
        if (res.code === 200) {
          images.value = images.value.filter(img => !selectedIds.value.has(img.id))
        }
      } else {
        res = await (Apis as any).everkeep.videoBatchMove({
          data: {
            albumId: targetAlbumId,
            ids: Array.from(selectedIds.value),
          },
        })
        if (res.code === 200) {
          videos.value = videos.value.filter(v => !selectedIds.value.has(v.id))
        }
      }

      if (res.code === 200) {
        toast.success('移动成功')
        selectedIds.value.clear()
        isSelectionMode.value = false
      } else {
        toast.error(res.message || '移动失败')
      }
    } catch (err) {
      console.error('Move error:', err)
      toast.error('网络请求失败')
    } finally {
      uni.hideLoading()
    }
  }).catch(() => {})
}

function handleBatchDelete() {
  if (selectedIds.value.size === 0) return
  const isImage = currentTab.value === 'image'
  message.confirm({
    title: '批量删除',
    msg: `确定要删除选中的 ${selectedIds.value.size} 个${isImage ? '图片' : '视频'}吗？删除后不可恢复。`,
  }).then(async () => {
    try {
      if (isImage) {
        await Apis.everkeep.delete({
          data: Array.from(selectedIds.value),
        })
        // 从本地列表移除已删除的图片
        images.value = images.value.filter(img => !selectedIds.value.has(img.id))
      } else {
        await (Apis as any).everkeep.videoDelete({
          data: Array.from(selectedIds.value),
        })
        // 从本地列表移除已删除的视频
        videos.value = videos.value.filter(v => !selectedIds.value.has(v.id))
      }
      toast.success('删除成功')
    } catch (error) {
      console.error('Failed to delete items:', error)
      toast.error('删除失败')
    }
    selectedIds.value.clear()
    isSelectionMode.value = false
  }).catch(() => {})
}

function openVideo(video: VideoItem) {
  if (isSelectionMode.value) {
    toggleSelection(video.id)
    return
  }
  const url = encodeURIComponent(getImageUrl(video.url))
  const name = encodeURIComponent(video.name || '')
  const duration = String(video.duration || 0)
  uni.navigateTo({ url: `/pages/video/preview?url=${url}&name=${name}&duration=${duration}` })
}

function toggleSelection(id: string | number) {
  if (selectedIds.value.has(id)) {
    selectedIds.value.delete(id)
  } else {
    selectedIds.value.add(id)
  }
}

function handleSetCover() {
  if (selectedIds.value.size !== 1) return
  const id = Array.from(selectedIds.value)[0]
  message.confirm({
    title: '设置封面',
    msg: `确定将该${currentTab.value === 'image' ? '图片' : '视频封面'}设为相册封面吗？`
  })
    .then(async () => {
      try {
        let res
        if (currentTab.value === 'image') {
          res = await (Apis as any).everkeep.setAlbumCover({ params: { imageId: id } })
        } else {
          res = await (Apis as any).everkeep.videoSetCover({ params: { videoId: id } })
        }

        if (res.code === 200) {
          toast.success('已设置为相册封面')
          selectedIds.value.clear()
          isSelectionMode.value = false
        } else {
          toast.error(res.message || '设置失败')
        }
      } catch (e) {
        console.error(e)
        toast.error('网络错误')
      }
    })
    .catch(() => {})
}

const albumName = ref('')
const showRename = ref(false)
const renameType = ref<'album' | 'video'>('album')
const renameValue = ref('')
const renameId = ref<string | number>('')

// 监听标题点击
function handleTitleClick() {
  renameType.value = 'album'
  renameValue.value = albumName.value
  renameId.value = albumId.value!
  showRename.value = true
}

async function handleRename() {
  if (!renameValue.value.trim()) {
    toast.error('名称不能为空')
    return
  }

  try {
    if (renameType.value === 'album') {
      await (Apis as any).everkeep.albumUpdate({
        data: { id: renameId.value, name: renameValue.value }
      })
      albumName.value = renameValue.value
      uni.setNavigationBarTitle({ title: renameValue.value })
    } else {
      await (Apis as any).everkeep.videoRename({
        data: { id: renameId.value, name: renameValue.value }
      })
      // 更新本地视频列表
      const target = videos.value.find(v => v.id === renameId.value)
      if (target) target.name = renameValue.value
    }
    toast.success('重命名成功')
    showRename.value = false
  } catch (e) {
    console.error(e)
    toast.error('操作失败')
  }
}

function handleVideoLongPress(video: VideoItem) {
  if (isSelectionMode.value) return
  renameType.value = 'video'
  renameValue.value = video.name
  renameId.value = video.id
  showRename.value = true
}

onLoad((option) => {
  if (option && option.id) {
    albumId.value = option.id
  }
  if (option && option.name) {
    albumName.value = decodeURIComponent(option.name)
    uni.setNavigationBarTitle({ title: albumName.value })
  }
})

function goBack() {
  router.back()
}

onMounted(() => {
  if (albumId.value) {
    refresh()
  } else {
    toast.error('参数错误')
    setTimeout(() => uni.navigateBack(), 1500)
  }
})

// 触底加载
onReachBottom(() => {
  if (currentTab.value === 'image') {
    loadMore()
  } else {
    loadMoreVideos()
  }
})
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa] pb-10 dark:bg-black">
    <!-- 顶部组合栏 (仅导航) -->
    <div
      class="fixed left-0 right-0 top-0 z-50 bg-[#f8f9fa]/85 px-4 backdrop-blur-xl transition-all dark:bg-black/85"
      :style="{ paddingTop: `${statusBarHeight}px` }"
    >
      <div class="h-[44px] flex items-center" :style="{ paddingRight: `${menuButtonRight}px` }">
        <!-- 左侧返回 -->
        <div
          class="h-10 w-10 flex flex-shrink-0 items-center justify-center active:opacity-60"
          @tap="goBack"
        >
          <wd-icon name="arrow-left" size="24px" :color="isDark ? '#fff' : '#1a1a1a'" />
        </div>

        <!-- 中间标题 -->
        <div class="flex flex-1 items-center justify-center overflow-hidden">
          <div
            class="max-w-[90%] flex items-center justify-center rounded-full bg-white/50 px-4 py-1 backdrop-blur-sm active:bg-gray-100 dark:bg-gray-800/50 dark:active:bg-gray-700"
            @tap="handleTitleClick"
          >
            <span class="truncate text-[17px] text-gray-900 font-bold dark:text-white">
              {{ albumName || '相册详情' }}
            </span>
            <div class="i-solar-pen-new-square-linear ml-1.5 text-sm text-gray-500" />
          </div>
        </div>

        <!-- 右侧排序 -->
        <div
          class="h-10 w-10 flex flex-shrink-0 items-center justify-center active:opacity-60"
          @tap="showSortSheet = true"
        >
          <div
            class="text-xl transition-all duration-300" :class="[
              order !== 'newest' ? 'i-solar-tuning-bold-duotone scale-110' : 'i-solar-sort-vertical-line-duotone',
            ]"
            :style="{ color: order !== 'newest' ? (isDark ? '#5189fb' : '#2979ff') : (isDark ? '#eee' : '#666') }"
          />
        </div>
      </div>

      <!-- 搜索栏 (动态显示) -->
      <div
        v-if="showSearch"
        class="pb-2 pt-1 transition-all"
      >
        <wd-search
          v-model="searchQuery"
          :placeholder="currentTab === 'image' ? '搜索图片...' : '搜索视频...'"
          @search="handleSearch"
          @clear="handleSearch"
          :hide-cancel="true"
          focus
          custom-class="!bg-white dark:!bg-gray-900 !rounded-2xl !py-2.5 shadow-sm border border-gray-100 dark:border-gray-800/50"
        />
      </div>
    </div>

    <!-- 顶部占位 -->
    <div :style="{ height: showSearch ? `${statusBarHeight + 100}px` : `${statusBarHeight + 44}px` }" />

    <!-- 内容区域 -->
    <div class="px-3 pt-3">
      <template v-if="currentTab === 'image'">
        <ImageWaterfall
          v-model:selected-ids="selectedIds"
          :list="images"
          :loading="loading"
          :is-selection-mode="isSelectionMode"
        />
        <wd-loadmore custom-class="py-8" :state="loading ? 'loading' : (hasMore ? 'loading' : 'finished')" />
      </template>

      <template v-else>
        <!-- 视频瀑布流 -->
        <div v-if="videos.length > 0" class="w-full flex flex-row items-start gap-3 overflow-hidden">
          <!-- 左列 -->
          <div class="min-w-0 flex flex-1 flex-col gap-3">
            <div
              v-for="video in videoLeft" :key="video.id"
              class="relative w-full overflow-hidden rounded-xl bg-white shadow-sm transition-all dark:bg-gray-900 active:opacity-80"
              :class="[selectedIds.has(video.id) ? 'ring-2 ring-blue-500 scale-[0.98]' : '']"
              @tap="openVideo(video)"
              @longpress="handleVideoLongPress(video)">
              <div class="relative w-full bg-gray-100 dark:bg-gray-800">
                <image :src="getImageUrl(video.coverUrl || video.url)" mode="widthFix" class="block h-auto w-full" lazy-load />
                <div v-if="!isSelectionMode" class="absolute inset-0 flex items-center justify-center bg-black/10">
                  <div class="i-solar-play-circle-bold text-3xl text-white opacity-80 drop-shadow-md"/>
                </div>
                <!-- 选中状态遮罩 -->
                <div v-if="isSelectionMode" class="absolute inset-0 flex items-start justify-end bg-black/20 p-2 transition-all">
                  <div
                    class="h-6 w-6 flex items-center justify-center border-2 rounded-full transition-all"
                    :class="selectedIds.has(video.id) ? 'bg-blue-500 border-blue-500 shadow-lg' : 'bg-white/30 border-white'"
                  >
                    <div v-if="selectedIds.has(video.id)" class="i-solar-check-read-bold text-xs text-white" />
                  </div>
                </div>
                <div class="absolute bottom-2 right-2 flex items-center gap-1.5">
                  <!-- 权限状态 -->
                  <div
                    class="rounded px-1.5 py-0.5 text-[10px] text-white font-medium backdrop-blur-sm"
                    :class="video.status?.code === 1 ? 'bg-gray-500/60' : 'bg-blue-500/60'"
                  >
                    {{ video.status?.code === 1 ? '公开' : '私有' }}
                  </div>
                  <!-- 时长 -->
                  <div class="rounded bg-black/60 px-1.5 py-0.5 text-[10px] text-white font-medium backdrop-blur-sm">
                    {{ formatDuration(video.duration) }}
                  </div>
                </div>
              </div>
              <view class="p-3">
                <view class="line-clamp-2 text-sm text-gray-800 font-medium leading-snug dark:text-gray-200">{{ video.name }}</view>
                <div class="mt-1.5 flex items-center justify-between text-[10px] text-gray-400">
                  <span>{{ formatSize(video.size) }}</span>
                  <span>{{ video.createTime?.split(' ')[0] }}</span>
                </div>
              </view>
            </div>
          </div>
          <!-- 右列 -->
          <div class="min-w-0 flex flex-1 flex-col gap-3">
            <div
              v-for="video in videoRight" :key="video.id"
              class="relative w-full overflow-hidden rounded-xl bg-white shadow-sm transition-all dark:bg-gray-900 active:opacity-80"
              :class="[selectedIds.has(video.id) ? 'ring-2 ring-blue-500 scale-[0.98]' : '']"
              @tap="openVideo(video)"
              @longpress="handleVideoLongPress(video)">
              <div class="relative w-full bg-gray-100 dark:bg-gray-800">
                <image :src="getImageUrl(video.coverUrl || video.url)" mode="widthFix" class="block h-auto w-full" lazy-load />
                <div v-if="!isSelectionMode" class="absolute inset-0 flex items-center justify-center bg-black/10">
                  <div class="i-solar-play-circle-bold text-3xl text-white opacity-80 drop-shadow-md"/>
                </div>
                <!-- 选中状态遮罩 -->
                <div v-if="isSelectionMode" class="absolute inset-0 flex items-start justify-end bg-black/20 p-2 transition-all">
                  <div
                    class="h-6 w-6 flex items-center justify-center border-2 rounded-full transition-all"
                    :class="selectedIds.has(video.id) ? 'bg-blue-500 border-blue-500 shadow-lg' : 'bg-white/30 border-white'"
                  >
                    <div v-if="selectedIds.has(video.id)" class="i-solar-check-read-bold text-xs text-white" />
                  </div>
                </div>
                <div class="absolute bottom-2 right-2 flex items-center gap-1.5">
                  <!-- 权限状态 -->
                  <div
                    class="rounded px-1.5 py-0.5 text-[10px] text-white font-medium backdrop-blur-sm"
                    :class="video.status?.code === 1 ? 'bg-gray-500/60' : 'bg-blue-500/60'"
                  >
                    {{ video.status?.code === 1 ? '公开' : '私有' }}
                  </div>
                  <!-- 时长 -->
                  <div class="rounded bg-black/60 px-1.5 py-0.5 text-[10px] text-white font-medium backdrop-blur-sm">
                    {{ formatDuration(video.duration) }}
                  </div>
                </div>
              </div>
              <view class="p-3">
                <view class="line-clamp-2 text-sm text-gray-800 font-medium leading-snug dark:text-gray-200">{{ video.name }}</view>
                <div class="mt-1.5 flex items-center justify-between text-[10px] text-gray-400">
                  <span>{{ formatSize(video.size) }}</span>
                  <span>{{ video.createTime?.split(' ')[0] }}</span>
                </div>
              </view>
            </div>
          </div>
        </div>
        <div v-else-if="!videoLoading" class="py-10">
          <EmptyState
            icon="video"
            title="暂无视频"
            description="该相册下还没有视频内容"
          />
        </div>
        <wd-loadmore custom-class="py-8" :state="videoLoading ? 'loading' : (videoHasMore ? 'loading' : 'finished')" />
      </template>
    </div>

    <!-- 底部批量操作栏 -->
    <div
      v-if="isSelectionMode"
      class="fixed left-1/2 z-[10] h-16 w-[90%] flex items-center justify-between rounded-2xl bg-gray-800/95 px-6 shadow-2xl backdrop-blur-xl -translate-x-1/2 dark:bg-gray-900/95"
      :style="{ bottom: `${safeAreaInsetsBottom + 24}px` }"
    >
      <div class="flex items-center gap-2">
        <span class="text-sm text-white/70">已选 <span class="text-white font-bold">{{ selectedIds.size }}</span></span>
      </div>

      <div class="flex items-center gap-3">
        <div
          class="h-10 w-10 flex items-center justify-center rounded-xl bg-white/15 text-white transition-all active:scale-90 active:bg-white/25"
          @tap="handleBatchMove"
        >
          <div class="i-solar-folder-path-connect-bold-duotone text-xl" />
        </div>
        <div
          class="h-10 w-10 flex items-center justify-center rounded-xl bg-white/15 text-white transition-all active:scale-90 active:bg-white/25"
          @tap="handleBatchPublic"
        >
          <div class="i-solar-earth-bold-duotone text-xl" />
        </div>
        <div
          v-show="selectedIds.size === 1"
          class="h-10 w-10 flex items-center justify-center rounded-xl bg-white/15 text-white transition-all active:scale-90 active:bg-white/25"
          @tap="handleSetCover"
        >
          <div class="i-solar-gallery-add-bold-duotone text-xl" />
        </div>
        <div
          class="h-10 w-10 flex items-center justify-center rounded-xl bg-red-500/20 text-red-500 transition-all active:scale-90"
          @tap="handleBatchDelete"
        >
          <div class="i-solar-trash-bin-trash-bold-duotone text-xl" />
        </div>
        <div class="h-6 w-[1px] bg-white/10" />
        <div
          class="h-10 w-10 flex items-center justify-center rounded-xl bg-white text-gray-900 transition-all active:scale-90"
          @tap="isSelectionMode = false; selectedIds.clear()"
        >
          <div class="i-solar-close-circle-bold-duotone text-xl" />
        </div>
      </div>
    </div>

    <!-- 悬浮操作按钮 -->
    <wd-fab
      v-if="!isSelectionMode"
      type="primary"
      position="right-bottom"
      direction="top"
      :z-index="99"
      :gap="{ bottom: 20 + safeAreaInsetsBottom, right: 30 }"
    >
      <div class="flex flex-col items-center gap-2">
        <wd-button type="primary" round custom-class="!px-3 !h-10 !min-w-0" @click="handleUploadClick">
          <div class="flex items-center gap-1.5">
            <div class="i-solar-cloud-upload-bold-duotone text-lg" />
            <span class="text-sm">上传内容</span>
          </div>
        </wd-button>
        <wd-button type="info" round custom-class="!px-3 !h-10 !min-w-0" @click="toggleTab">
          <div class="flex items-center gap-1.5">
            <div
              class="text-lg"
              :class="currentTab === 'image' ? 'i-solar-play-circle-bold-duotone' : 'i-solar-gallery-bold-duotone'"
            />
            <span class="text-sm">{{ currentTab === 'image' ? '切换视频' : '切换图片' }}</span>
          </div>
        </wd-button>
        <wd-button type="warning" round custom-class="!px-3 !h-10 !min-w-0" @click="toggleSearch">
          <div class="flex items-center gap-1.5">
            <div class="i-solar-magnifer-bold-duotone text-lg" />
            <span class="text-sm">{{ showSearch ? '关闭搜索' : '搜索内容' }}</span>
          </div>
        </wd-button>
        <wd-button type="success" round custom-class="!px-3 !h-10 !min-w-0" @click="isSelectionMode = true">
          <div class="flex items-center gap-1.5">
            <div class="i-solar-checklist-minimalistic-bold-duotone text-lg" />
            <span class="text-sm">批量管理</span>
          </div>
        </wd-button>
      </div>
    </wd-fab>

    <!-- 排序操作面板 -->
    <SortSheet
      v-model="showSortSheet" :options="orderOptions" :current-value="order" title="排序方式"
      subtitle="选择图片内容的展示顺序" @select="handleSortSelect" />
    <!-- 权限设置面板 -->
    <wd-action-sheet
      v-model="showPublicSheet"
      :actions="publicOptions"
      title="设置图片权限"
      @select="handlePublicSelect"
    />

    <!-- 移动相册面板 -->
    <wd-action-sheet
      v-model="showMoveSheet"
      :actions="albumOptions"
      title="移动到相册"
      @select="handleMoveSelect"
    />

    <!-- 上传选择面板 -->
    <wd-action-sheet
      v-model="showUploadSheet"
      title="选择上传类型"
      :z-index="10010"
      @select="handleUploadSelect"
    >
      <div class="grid grid-cols-2 gap-4 p-6">
        <div
          v-for="(item, index) in uploadOptions"
          :key="index"
          class="flex flex-col items-center justify-center gap-3 rounded-2xl py-6 transition-all active:scale-95 active:bg-gray-100 dark:active:bg-gray-800"
          @tap="handleUploadSelect({ item, index })"
        >
          <div
            class="h-14 w-14 flex items-center justify-center rounded-2xl text-2xl shadow-sm"
            :class="[
              index === 0 ? 'bg-blue-50 text-blue-600 dark:bg-blue-900/30' : 'bg-orange-50 text-orange-600 dark:bg-orange-900/30'
            ]"
          >
            <div :class="item.icon" />
          </div>
          <span class="text-sm text-gray-700 font-bold dark:text-gray-200">{{ item.name }}</span>
        </div>
      </div>
    </wd-action-sheet>

    <wd-message-box />

    <!-- 重命名弹窗 -->
    <wd-popup
      v-model="showRename"
      position="bottom"
      round
      custom-class="rounded-t-[32rpx] overflow-hidden"
      :z-index="10001"
      safe-area-inset-bottom
    >
      <div
        class="px-6 pb-6 pt-3 backdrop-blur-md transition-colors duration-300"
        :class="isDark ? 'bg-gray-900/95 text-white' : 'bg-white/95 text-gray-900'"
      >
        <!-- 顶部装饰条 -->
        <div class="flex justify-center pb-4">
          <div
            class="h-1 w-10 rounded-full"
            :class="isDark ? 'bg-gray-700' : 'bg-gray-200'"
          />
        </div>

        <div class="mb-6">
          <div class="mb-1 text-lg font-bold">{{ renameType === 'album' ? '重命名相册' : '重命名视频' }}</div>
          <div class="text-xs text-gray-400">请输入新的名称</div>
        </div>

        <div class="mb-6 w-full">
          <input
            v-model="renameValue"
            class="box-border h-12 w-full rounded-2xl bg-gray-100 px-4 text-base dark:bg-gray-800"
            :class="isDark ? 'text-white' : 'text-gray-900'"
            placeholder="请输入名称"
            :focus="showRename"
          >
        </div>

        <div class="flex gap-3">
          <div
            class="flex-1 rounded-2xl py-3.5 text-center text-[15px] font-bold transition-all active:scale-[0.98]"
            :class="isDark ? 'bg-gray-800 text-gray-300' : 'bg-gray-100 text-gray-600'"
            @tap="showRename = false"
          >
            取消
          </div>
          <div
            class="flex-1 rounded-2xl py-3.5 text-center text-[15px] text-white font-bold shadow-lg transition-all active:scale-[0.98]"
            :class="isDark ? 'bg-blue-600 shadow-blue-900/20' : 'bg-blue-600 shadow-blue-200'"
            @tap="handleRename"
          >
            保存
          </div>
        </div>
      </div>
    </wd-popup>
  </div>
</template>
