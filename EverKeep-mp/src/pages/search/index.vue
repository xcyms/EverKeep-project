<script lang="ts" setup>
import type { SortOption } from '@/types/common'
import type { AlbumItem } from '@/types/page'
import { onPageScroll, onReachBottom, onShow } from '@dcloudio/uni-app'
import { computed, onMounted, ref, watch } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import SortSheet from '@/components/common/SortSheet.vue'
import { useManualTheme } from '@/composables/useManualTheme'
import { usePageCache } from '@/composables/usePageCache'
import { CACHE_KEYS, CACHE_TTL, PAGINATION, PLACEHOLDER_IMAGE } from '@/utils/constants'

definePage({
  name: 'search',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '相册',
    navigationStyle: 'custom',
    enablePullDownRefresh: true,
  },
})

const router = useRouter()
const user = useAuthStore()
const toast = useToast()
const { statusBarHeight, menuButtonRight } = useSystemInfo()
const { isDark } = useManualTheme()
const searchQuery = ref('')
const albumList = ref<AlbumItem[]>([])
const isFirstLoad = ref(true)
const showBackTop = ref(false)
const loading = ref(false)
const hasMore = ref(true)
const page = ref(1)
const order = ref<'newest' | 'earliest' | 'most' | 'least'>('newest')
const showSortSheet = ref(false)
const showCreateAlbum = ref(false)
const newAlbumName = ref('')
const newAlbumDesc = ref('')
const nameError = ref('')

async function createAlbum() {
  if (!newAlbumName.value.trim()) {
    nameError.value = '相册名称不能为空'
    return
  }
  nameError.value = ''

  try {
    const res = await (Apis as any).everkeep.albumCreate({
      data: { name: newAlbumName.value.trim(), description: newAlbumDesc.value.trim() || undefined },
    })
    if (res.code === 200) {
      toast.success('创建成功')
      showCreateAlbum.value = false
      newAlbumName.value = ''
      newAlbumDesc.value = ''
      await handleSearch()
    } else {
      toast.error(res.message || '创建失败')
    }
  } catch (e) {
    console.error(e)
    toast.error('网络错误')
  }
}

watch(showCreateAlbum, (val) => {
  if (val) {
    nameError.value = ''
  }
})

watch(newAlbumName, (val) => {
  if (val.trim()) {
    nameError.value = ''
  }
})

// 将列表拆分为左右两列
const leftColList = computed(() => albumList.value.filter((_, i) => i % 2 === 0))
const rightColList = computed(() => albumList.value.filter((_, i) => i % 2 !== 0))

// 加载状态
const loadingState = computed(() => {
  if (loading.value) return 'loading'
  if (!hasMore.value && albumList.value.length > 0) return 'finished'
  return 'finished'
})

const orderOptions: SortOption[] = [
  { name: '最新创建', value: 'newest', subname: '按相册创建时间从新到旧', icon: 'i-solar-clock-circle-linear' },
  { name: '最早创建', value: 'earliest', subname: '按相册创建时间从旧到新', icon: 'i-solar-history-linear' },
  { name: '图片最多', value: 'most', subname: '按相册内图片数量从多到少', icon: 'i-solar-graph-new-linear' },
  { name: '图片最少', value: 'least', subname: '按相册内图片数量从少到多', icon: 'i-solar-graph-linear' },
]

// 默认模拟数据
const defaultAlbums: AlbumItem[] = [
  {
    id: 'd1',
    userId: user.user?.id || 0,
    name: '风景精选',
    description: '大自然最纯净的呼吸，定格山川河流的美丽瞬间。',
    cover: 'https://picsum.photos/id/10/400/300',
    imageCount: 12,
    createTime: '2023-01-01',
    updateTime: '2023-01-01',
  },
  {
    id: 'd2',
    userId: user.user?.id || 0,
    name: '人像摄影',
    description: '捕捉眼神中的故事，记录每一个真实而动人的面孔。',
    cover: 'https://picsum.photos/id/20/400/300',
    imageCount: 8,
    createTime: '2023-01-02',
    updateTime: '2023-01-02',
  },
  {
    id: 'd3',
    userId: user.user?.id || 0,
    name: '城市建筑',
    description: '穿梭于钢筋水泥之间，探索城市空间的几何美学。',
    cover: 'https://picsum.photos/id/30/400/300',
    imageCount: 15,
    createTime: '2023-01-03',
    updateTime: '2023-01-03',
  },
  {
    id: 'd4',
    userId: user.user?.id || 0,
    name: '自然风光',
    description: '从森林到海洋，带你领略地球上最原始的生命力。',
    cover: 'https://picsum.photos/id/40/400/300',
    imageCount: 20,
    createTime: '2023-01-04',
    updateTime: '2023-01-04',
  },
]

// 使用 usePageCache 缓存首页相册数据
const { data: cachedAlbums, refresh: refreshCache } = usePageCache<AlbumItem[]>({
  key: CACHE_KEYS.SEARCH_ALBUMS,
  fetchFn: async () => {
    // 未登录时返回模拟数据
    if (!user.isLoggedIn) {
      await new Promise(resolve => setTimeout(resolve, 1000))
      return defaultAlbums.map(item => ({ ...item, userId: user.user?.id || 0 }))
    }

    try {
      const res = await Apis.everkeep.albumPage({
        params: {
          current: 1,
          size: PAGINATION.DEFAULT_PAGE_SIZE,
          column: 'a.create_time',
          asc: false,
        },
        data: {},
      })

      if (res.code === 200 && res.data) {
        return res.data.records || []
      }
      return []
    } catch (e) {
      console.error('Failed to fetch cached albums:', e)
      return []
    }
  },
  ttl: CACHE_TTL.MEDIUM,
  immediate: false,
})

// 监听缓存数据变化，更新列表
watch(cachedAlbums, (newData) => {
  if (newData && page.value === 1 && searchQuery.value === '') {
    albumList.value = newData
    isFirstLoad.value = false
  }
})

// 获取相册数据
async function fetchAlbums(isLoadMore = false) {
  if (!isLoadMore && albumList.value.length === 0) {
    isFirstLoad.value = true
  }

  loading.value = true

  try {
    // 未登录时返回模拟数据
    if (!user.isLoggedIn) {
      await new Promise(resolve => setTimeout(resolve, 1000))

      const mockData: AlbumItem[] = []
      const totalPages = 3

      if (page.value <= totalPages) {
        for (let i = 0; i < 6; i++) {
          const id = (page.value - 1) * 6 + i + 1
          mockData.push({
            id: `mock-${id}`,
            name: searchQuery.value ? `搜索结果: ${searchQuery.value} ${id}` : `相册 ${id}`,
            description: `这是一个模拟相册的描述内容，用于演示 UI 效果。序号：${id}`,
            cover: `https://picsum.photos/id/${Math.floor(Math.random() * 50) + 50}/400/300`,
            imageCount: Math.floor(Math.random() * 50) + 1,
            createTime: new Date().toISOString(),
            updateTime: new Date().toISOString(),
            userId: user.user?.id || 0,
          })
        }
      }

      if (isLoadMore) {
        albumList.value.push(...mockData)
      } else {
        albumList.value = mockData
      }

      hasMore.value = page.value < totalPages
      return
    }

    // 已登录：调用真实接口
    const sortMap: Record<string, { column: string; asc: boolean }> = {
      newest: { column: 'a.create_time', asc: false },
      earliest: { column: 'a.create_time', asc: true },
      most: { column: 'imageCount', asc: false },
      least: { column: 'imageCount', asc: true },
    }
    const { column, asc } = sortMap[order.value] || sortMap.newest

    const res = await Apis.everkeep.albumPage({
      params: {
        current: page.value,
        size: PAGINATION.DEFAULT_PAGE_SIZE,
        column,
        asc,
      },
      data: {
        name: searchQuery.value || undefined,
      },
    })

    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      if (isLoadMore) {
        albumList.value.push(...records)
      } else {
        albumList.value = records
      }
      hasMore.value = page.value < (res.data.pages || 0)
    } else {
      throw new Error(res.message || '获取相册失败')
    }
  } catch (error) {
    console.error(error)
    toast.error('加载相册失败')
  } finally {
    loading.value = false
    isFirstLoad.value = false
  }
}

// 处理排序选择
function handleSortSelect(option: SortOption) {
  order.value = option.value as 'newest' | 'earliest' | 'most' | 'least'
}

// 监听排序变化
watch(order, () => {
  handleSearch()
})

// 监听滚动
onPageScroll((e) => {
  showBackTop.value = e.scrollTop > 400
})

// 回到顶部
function scrollToTop() {
  uni.pageScrollTo({
    scrollTop: 0,
    duration: 300,
  })
}

// 监听登录状态
watch(
  () => user.isLoggedIn,
  (newVal, oldVal) => {
    if (newVal !== oldVal) {
      refreshCache(true)
      handleSearch()
    }
  }
)

// 页面显示时检查数据状态
onShow(() => {
  const isShowingDefault =
    albumList.value.length > 0 && albumList.value.some((item) => typeof item.id === 'string' && item.id.startsWith('d'))

  if (user.isLoggedIn && (albumList.value.length === 0 || isShowingDefault)) {
    fetchAlbums()
  } else if (!user.isLoggedIn && albumList.value.length > 0 && !isShowingDefault) {
    fetchAlbums()
  }
})

// 处理图片加载失败
function handleImageError(album: AlbumItem) {
  album._error = true
  album.cover = PLACEHOLDER_IMAGE
}

// 搜索事件处理
async function handleSearch() {
  uni.pageScrollTo({ scrollTop: 0, duration: 200 })
  albumList.value = []
  page.value = 1
  hasMore.value = true
  await fetchAlbums()
}

// 加载更多相册
async function loadMoreAlbums() {
  if (user.isLoggedIn && hasMore.value && !loading.value) {
    page.value++
    await fetchAlbums(true)
  }
}

function goImages(id: string | number, name: string) {
  if (user.isLoggedIn) {
    router.push({
      name: 'images',
      params: {
        id: String(id),
        name: encodeURIComponent(name),
      },
    })
  }
}

onMounted(() => {
  // 使用缓存加载首页数据
  refreshCache(false)
})

// 触底加载更多
onReachBottom(() => {
  loadMoreAlbums()
})

onPullDownRefresh(async () => {
  await fetchAlbums()
  uni.stopPullDownRefresh()
})

</script>

<template>
  <div class="bg-[#f8f9fa] pb-10 transition-colors duration-300 dark:bg-black">
    <!-- 沉浸式搜索头部 - 使用 fixed 确保不随页面滚动 -->
    <div
      class="fixed left-0 right-0 top-0 z-50 bg-white/80 px-3 pb-2 backdrop-blur-xl transition-all dark:bg-black/60"
      :style="{ paddingTop: `${statusBarHeight + 6}px` }">
      <div class="flex items-center" :style="{ paddingRight: `${menuButtonRight}px` }">
        <div class="flex-1">
          <wd-search
            v-model="searchQuery" placeholder="搜索相册、摄影师..." @search="handleSearch" @clear="handleSearch"
            :hide-cancel="true" custom-class="!bg-gray-100/80 dark:!bg-gray-800/80 !rounded-xl !p-0" />
        </div>
        <!-- 排序触发按钮 -->
        <div class="h-10 flex flex-shrink-0 items-center justify-center px-3" @tap="showSortSheet = true">
          <div
            class="text-xl transition-all duration-300" :class="[
            order !== 'newest' ? 'i-solar-tuning-bold-duotone scale-110' : 'i-solar-sort-vertical-line-duotone'
          ]"
            :style="{ color: order !== 'newest' ? (isDark ? '#5189fb' : '#2979ff') : (isDark ? '#eee' : '#666') }" />
        </div>
      </div>
    </div>

    <!-- 顶部占位，防止内容被固定头遮挡 -->
    <div :style="{ height: `${statusBarHeight + 54}px` }" />

    <!-- 内容区域 - 使用原生页面滚动 -->
    <div class="flex flex-col px-4 pt-2">
      <!-- 骨架屏状态 -->
      <div v-if="isFirstLoad && albumList.length === 0" class="flex gap-4 px-1">
        <div v-for="col in 2" :key="col" class="flex flex-1 flex-col">
          <div v-for="i in 3" :key="i" class="mb-4 overflow-hidden rounded-2xl bg-white p-0 shadow-sm dark:bg-gray-900">
            <div
              class="animate-pulse bg-gray-200 dark:bg-gray-800"
              :style="{ height: i % 2 === 0 ? '180px' : '220px' }" />
            <div class="p-3 space-y-2">
              <div class="h-4 w-3/4 animate-pulse rounded bg-gray-200 dark:bg-gray-800" />
              <div class="h-3 w-1/2 animate-pulse rounded bg-gray-100 dark:bg-gray-800" />
            </div>
          </div>
        </div>
      </div>

      <!-- 结果统计 -->
      <div v-if="albumList.length > 0" class="mb-4 flex items-center justify-between px-1">
        <span class="text-base text-gray-900 font-bold italic dark:text-gray-100">Discovery</span>
        <span class="text-[11px] text-gray-400 font-medium tracking-wider dark:text-gray-500">{{ albumList.length }}
          ALBUMS</span>
      </div>

      <!-- 瀑布流布局容器 - 使用 Flex 改写以提升稳定性 -->
      <div v-if="albumList.length > 0" class="flex gap-4 px-1">
        <!-- 左列 -->
        <div class="flex flex-1 flex-col">
          <div
            v-for="album in leftColList" :key="album.id"
            class="group relative mb-4 flex flex-col overflow-hidden rounded-2xl bg-white shadow-[0_4px_16px_rgba(0,0,0,0.04)] dark:bg-gray-900 active:opacity-80 dark:shadow-none"
            @tap="goImages(album.id, album.name)">
            <div class="relative w-full overflow-hidden bg-gray-100 dark:bg-gray-800">
              <image
                :src="getImageUrl(album.cover)" mode="widthFix" class="block h-auto w-full" lazy-load
                @error="handleImageError(album)" />
              <div
                class="absolute right-2.5 top-2.5 z-10 rounded-full bg-black/30 px-2 py-0.5 text-[10px] text-white font-bold backdrop-blur-md">
                {{ album.imageCount }}
              </div>
            </div>
            <div class="p-3">
              <div class="text-sm text-gray-800 font-bold leading-snug dark:text-gray-200">{{ album.name }}</div>
              <div v-if="album.description" class="mt-1.5 text-[11px] text-gray-400 leading-relaxed dark:text-gray-500">
                {{ album.description }}
              </div>
            </div>
          </div>
        </div>

        <!-- 右列 -->
        <div class="flex flex-1 flex-col">
          <div
            v-for="album in rightColList" :key="album.id"
            class="group relative mb-4 flex flex-col overflow-hidden rounded-2xl bg-white shadow-[0_4px_16px_rgba(0,0,0,0.04)] dark:bg-gray-900 active:opacity-80 dark:shadow-none"
            @tap="goImages(album.id, album.name)">
            <div class="relative w-full overflow-hidden bg-gray-100 dark:bg-gray-800">
              <image
                :src="getImageUrl(album.cover)" mode="widthFix" class="block h-auto w-full" lazy-load
                @error="handleImageError(album)" />
              <div
                class="absolute right-2.5 top-2.5 z-10 rounded-full bg-black/30 px-2 py-0.5 text-[10px] text-white font-bold backdrop-blur-md">
                {{ album.imageCount }}
              </div>
            </div>
            <div class="p-3">
              <div class="text-sm text-gray-800 font-bold leading-snug dark:text-gray-200">{{ album.name }}</div>
              <div v-if="album.description" class="mt-1.5 text-[11px] text-gray-400 leading-relaxed dark:text-gray-500">
                {{ album.description }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <EmptyState v-else-if="!loading" icon="search" title="未发现相关相册" description="尝试使用其他关键词搜索" />

      <!-- 底部状态与适配 -->
      <div v-if="albumList.length > 0" class="mt-auto">
        <wd-loadmore custom-class="py-8" :state="loadingState" @reload="fetchAlbums" />
      </div>
    </div>

    <!-- 悬浮功能按钮 -->
    <div class="fixed bottom-24 right-6 z-50 flex flex-col gap-4 transition-all duration-300">
      <div
        class="h-12 w-12 flex items-center justify-center rounded-full bg-white/90 shadow-lg backdrop-blur-md transition-transform active:scale-90 dark:bg-gray-800/90"
        @click="showCreateAlbum = true">
        <div class="i-solar-add-circle-linear text-2xl" :style="{ color: isDark ? '#ddd' : '#333' }" />
      </div>
      <div
        class="h-12 w-12 flex items-center justify-center rounded-full bg-white/90 shadow-lg backdrop-blur-md transition-transform active:scale-90 dark:bg-gray-800/90"
        :class="[showBackTop ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-2 pointer-events-none']"
        @click="scrollToTop">
        <div class="i-solar-alt-arrow-up-linear text-2xl" :style="{ color: isDark ? '#ddd' : '#333' }" />
      </div>
    </div>

    <!-- 排序操作面板 -->
    <SortSheet
      v-model="showSortSheet" :options="orderOptions" :current-value="order" title="排序方式"
      subtitle="选择相册内容的展示顺序" @select="handleSortSelect" />

    <!-- 新建相册弹窗 -->
    <wd-popup
      v-model="showCreateAlbum" position="bottom" round custom-class="rounded-t-[32rpx] overflow-hidden"
      :z-index="10001" safe-area-inset-bottom>
      <div
        class="px-6 pb-6 pt-2 backdrop-blur-md transition-colors duration-300"
        :class="isDark ? 'bg-gray-900/95 text-white' : 'bg-white/95 text-gray-900'">
        <!-- 顶部装饰条 -->
        <div class="flex justify-center py-3">
          <div class="h-1 w-10 rounded-full" :class="isDark ? 'bg-gray-700' : 'bg-gray-200'" />
        </div>

        <!-- 头部 -->
        <div class="flex items-center justify-between pb-6">
          <div class="flex flex-col">
            <span class="text-lg font-bold">新建相册</span>
            <span class="mt-0.5 text-xs text-gray-400">创建您的专属相册空间</span>
          </div>
          <div
            class="h-8 w-8 flex items-center justify-center rounded-full transition-colors"
            :class="isDark ? 'bg-gray-800 active:bg-gray-700' : 'bg-gray-100 active:bg-gray-200'"
            @tap="showCreateAlbum = false">
            <div class="i-solar-close-circle-bold text-xl text-gray-400" />
          </div>
        </div>

        <!-- 表单内容 -->
        <div class="space-y-5">
          <!-- 相册名称 -->
          <div class="space-y-2">
            <div class="flex items-center justify-between">
              <span class="ml-1 text-sm font-bold" :class="isDark ? 'text-gray-300' : 'text-gray-700'">相册名称</span>
              <span v-if="nameError" class="animate-fade-in text-xs text-red-500 font-medium">{{ nameError }}</span>
            </div>
            <div class="relative">
              <input
                v-model="newAlbumName" placeholder="请输入相册名称" placeholder-class="text-gray-400"
                class="box-border h-12 w-full border rounded-2xl px-4 pr-10 text-base outline-none transition-colors"
                :class="[
                  isDark ? 'bg-gray-800 text-white' : 'bg-gray-50 text-gray-900',
                  nameError ? 'border-red-500 bg-red-50/10' : 'border-transparent'
                ]">
              <div
                v-if="newAlbumName"
                class="absolute right-3 top-1/2 h-5 w-5 flex items-center justify-center rounded-full bg-gray-200 text-gray-500 transition-colors -translate-y-1/2 active:bg-gray-300"
                @tap.stop="newAlbumName = ''">
                <div class="i-solar-close-circle-bold text-xs" />
              </div>
            </div>
          </div>

          <!-- 相册描述 -->
          <div class="space-y-2">
            <span class="ml-1 text-sm font-bold" :class="isDark ? 'text-gray-300' : 'text-gray-700'">相册描述</span>
            <div class="relative">
              <textarea
                v-model="newAlbumDesc" placeholder="写一段描述（可选）..." placeholder-class="text-gray-400"
                class="box-border h-28 w-full rounded-2xl border-none px-4 py-3 text-base transition-colors"
                :class="isDark ? 'bg-gray-800 text-white' : 'bg-gray-50 text-gray-900'" :maxlength="200"
                disable-default-padding />
              <div class="absolute bottom-2 right-3 text-xs text-gray-400 font-medium">
                {{ newAlbumDesc.length }}/200
              </div>
            </div>
          </div>
        </div>

        <!-- 底部按钮 -->
        <div class="pb-2 pt-8">
          <div
            class="w-full flex items-center justify-center gap-2 rounded-2xl py-3.5 text-[16px] text-white font-bold shadow-lg transition-all active:scale-[0.98] active:opacity-90"
            :class="isDark ? 'bg-blue-600 shadow-blue-900/20' : 'bg-blue-600 shadow-blue-200'" @tap="createAlbum">
            <div class="i-solar-add-circle-bold text-xl" />
            <span>立即创建</span>
          </div>
        </div>
      </div>
    </wd-popup>
  </div>
</template>
