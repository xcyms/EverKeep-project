<script setup lang="ts">
import type { SortOption } from '@/types/common'
import type { ImageItem } from '@/types/page'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { onMounted, ref, watch } from 'vue'
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
const { statusBarHeight, menuButtonRight } = useSystemInfo()

const albumId = ref<string | null>(null)
const searchQuery = ref('')
const order = ref<'newest' | 'earliest' | 'utmost' | 'least'>('newest')
const showSortSheet = ref(false)
const uploading = ref(false)
const { isDark } = useManualTheme()

const orderOptions: SortOption[] = [
  { name: '最新发布', value: 'newest', subname: '按上传时间从新到旧', icon: 'i-solar-clock-circle-bold-duotone' },
  { name: '最早发布', value: 'earliest', subname: '按上传时间从旧到新', icon: 'i-solar-history-bold-duotone' },
  { name: '最大尺寸', value: 'utmost', subname: '按文件大小从大到小', icon: 'i-solar-database-bold-duotone' },
  { name: '最小尺寸', value: 'least', subname: '按文件大小从小到大', icon: 'i-solar-database-linear' },
]

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

// 处理搜索
function handleSearch() {
  uni.pageScrollTo({ scrollTop: 0, duration: 200 })
  reset()
  refresh()
}

// 处理排序选择
function handleSortSelect(option: SortOption) {
  order.value = option.value as 'newest' | 'earliest' | 'utmost' | 'least'
}

// 监听排序变化
watch(order, () => {
  uni.pageScrollTo({ scrollTop: 0, duration: 200 })
  reset()
  refresh()
})

// 处理上传
function handleUpload() {
  if (uploading.value) return

  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res: any) => {
      const tempFilePath = res.tempFilePaths[0]

      try {
        uploading.value = true
        uni.showLoading({ title: '正在上传...', mask: true })

        uni.uploadFile({
          url: `${import.meta.env.VITE_API_BASE_URL}/file/upload`,
          filePath: tempFilePath,
          name: 'file',
          header: {
            Authorization: `Bearer ${user.token}`,
            Accept: 'application/json',
          },
          formData: {
            albumId: String(albumId.value),
          },
          success: (uploadRes) => {
            const data = JSON.parse(uploadRes.data)
            if (data.code === 200) {
              toast.success('上传成功')
              setTimeout(() => {
                reset()
                refresh()
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
    },
  })
}

onLoad((option) => {
  if (option && option.id) {
    albumId.value = option.id
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
  loadMore()
})
</script>

<template>
  <div class="bg-[#f8f9fa] pb-10 dark:bg-black">
    <!-- 沉浸式顶栏 -->
    <div
      class="fixed left-0 right-0 top-0 z-50 bg-white/80 px-3 pb-2 backdrop-blur-xl transition-all dark:bg-black/60"
      :style="{ paddingTop: `${statusBarHeight + 6}px` }">
      <div class="flex items-center" :style="{ paddingRight: `${menuButtonRight}px` }">
        <div class="mr-2 h-10 flex items-center justify-center px-1" @tap="goBack">
          <wd-icon name="arrow-left" size="20px" :color="isDark ? '#eee' : '#333'" />
        </div>
        <div class="flex-1">
          <wd-search
            v-model="searchQuery" placeholder="搜索相册内图片..." @search="handleSearch" @clear="handleSearch"
            :hide-cancel="true" custom-class="!bg-gray-100/80 dark:!bg-gray-800/60 !rounded-xl !p-0" />
        </div>
        <!-- 排序触发按钮 -->
        <div class="ml-1 h-10 flex flex-shrink-0 items-center justify-center px-2" @tap="showSortSheet = true">
          <wd-icon name="order-descending" size="18px" :color="isDark ? '#eee' : '#666'" />
        </div>
      </div>
    </div>

    <!-- 占位 -->
    <div :style="{ height: `${statusBarHeight + 54}px` }" />

    <!-- 内容区域 -->
    <div class="px-3 pt-3">
      <ImageWaterfall :list="images" :loading="loading" />

      <!-- 加载状态 -->
      <wd-loadmore custom-class="py-8" :state="loading ? 'loading' : (hasMore ? 'loading' : 'finished')" />
    </div>

    <!-- 悬浮上传按钮 -->
    <div class="fixed bottom-10 right-6 z-50 transform-gpu transition-all active:scale-90" @tap="handleUpload">
      <div
        class="h-14 w-14 flex items-center justify-center rounded-full bg-blue-500 text-white shadow-[0_8px_32px_rgba(59,130,246,0.3)]">
        <wd-icon name="add" size="24px" />
      </div>
    </div>

    <!-- 排序操作面板 -->
    <SortSheet
      v-model="showSortSheet" :options="orderOptions" :current-value="order" title="排序方式"
      subtitle="选择图片内容的展示顺序" @select="handleSortSelect" />
  </div>
</template>
