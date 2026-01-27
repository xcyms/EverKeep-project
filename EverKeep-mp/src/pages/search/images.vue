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
const message = useMessage()
const { statusBarHeight, menuButtonRight, safeAreaInsetsBottom } = useSystemInfo()

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
    msg: `确定将选中的 ${selectedIds.value.size} 张图片${item.name}吗？`,
  }).then(async () => {
    // 这里后续对接真实的修改权限接口
    try {
      await Apis.everkeep.updateStatus({
        data: {
          ids: Array.from(selectedIds.value),
          status: index,
        },
      })
      toast.success('设置成功')
      selectedIds.value.clear()
      reset()
      refresh()
    } catch (error) {
      console.error('Failed to update image status:', error)
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
    msg: `确定将选中的 ${selectedIds.value.size} 张图片移动到“${item.name}”吗？`,
  }).then(async () => {
    uni.showLoading({ title: '正在移动...', mask: true })
    try {
      const res = await Apis.everkeep.batchMove({
        data: {
          albumId: targetAlbumId,
          ids: Array.from(selectedIds.value),
        },
      })

      if (res.code === 200) {
        toast.success('移动成功')
        selectedIds.value.clear()
        reset()
        refresh()
      } else {
        toast.error(res.message || '移动失败')
      }
    } catch (err) {
      console.error('Move images error:', err)
      toast.error('网络请求失败')
    } finally {
      uni.hideLoading()
    }
  }).catch(() => {})
}

function handleBatchDelete() {
  if (selectedIds.value.size === 0) return
  message.confirm({
    title: '批量删除',
    msg: `确定要删除选中的 ${selectedIds.value.size} 张图片吗？删除后不可恢复。`,
  }).then(async () => {
    try {
      await Apis.everkeep.delete({
        data: Array.from(selectedIds.value),
      })
      toast.success('删除成功')
    } catch (error) {
      console.error('Failed to delete images:', error)
      toast.error('删除失败')
    }
    selectedIds.value.clear()
    reset()
    refresh()
  }).catch(() => {})
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
        <div class="h-10 flex flex-shrink-0 items-center justify-center px-3" @tap="showSortSheet = true">
          <div
            class="text-xl transition-all duration-300" :class="[
              order !== 'newest' ? 'i-solar-tuning-bold-duotone scale-110' : 'i-solar-sort-vertical-line-duotone'
            ]"
            :style="{ color: order !== 'newest' ? (isDark ? '#5189fb' : '#2979ff') : (isDark ? '#eee' : '#666') }"
          />
        </div>
      </div>
    </div>

    <!-- 占位 -->
    <div :style="{ height: `${statusBarHeight + 54}px` }" />

    <!-- 内容区域 -->
    <div class="px-3 pt-3">
      <ImageWaterfall
        v-model:selected-ids="selectedIds"
        :list="images"
        :loading="loading"
        :is-selection-mode="isSelectionMode"
      />

      <!-- 加载状态 -->
      <wd-loadmore custom-class="py-8" :state="loading ? 'loading' : (hasMore ? 'loading' : 'finished')" />
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
      :gap="{ bottom: 20 + safeAreaInsetsBottom, right: 30 }"
    >
      <div class="flex flex-col items-center gap-2">
        <wd-button type="primary" round custom-class="!px-3 !h-10 !min-w-0" @click="handleUpload">
          <div class="flex items-center gap-1.5">
            <div class="i-solar-upload-bold-duotone text-lg" />
            <span class="text-sm">上传图片</span>
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

    <wd-message-box />
  </div>
</template>
