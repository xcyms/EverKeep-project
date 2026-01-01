<script lang="ts" setup>
import type { SortOption } from '@/types/common'
import type { AlbumItem } from '@/types/page'
import { onPageScroll, onReachBottom, onShow } from '@dcloudio/uni-app'
import { computed, onMounted, ref, watch } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import SortSheet from '@/components/common/SortSheet.vue'
import { useManualTheme } from '@/composables/useManualTheme'
import { usePageCache } from '@/composables/usePageCache'
import { CACHE_KEYS, CACHE_TTL, PLACEHOLDER_IMAGE } from '@/utils/constants'

definePage({
  name: 'search',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '搜索',
    navigationStyle: 'custom',
    enablePullDownRefresh: false,
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
  { name: '最新创建', value: 'newest', subname: '按相册创建时间从新到旧', icon: 'time' },
  { name: '最早创建', value: 'earliest', subname: '按相册创建时间从旧到新', icon: 'history' },
  { name: '图片最多', value: 'most', subname: '按相册内图片数量从多到少', icon: 'chart-bar' },
  { name: '图片最少', value: 'least', subname: '按相册内图片数量从少到多', icon: 'chart-bar' },
]

// 默认模拟数据
const defaultAlbums: AlbumItem[] = [
  {
    id: 'd1',
    name: '风景精选',
    image_num: 12,
    intro: '大自然最纯净的呼吸，定格山川河流的美丽瞬间。',
    cover: 'https://picsum.photos/id/10/400/300',
  },
  {
    id: 'd2',
    name: '人像摄影',
    image_num: 8,
    intro: '捕捉眼神中的故事，记录每一个真实而动人的面孔。',
    cover: 'https://picsum.photos/id/20/400/300',
  },
  {
    id: 'd3',
    name: '城市建筑',
    image_num: 15,
    intro: '穿梭于钢筋水泥之间，探索城市空间的几何美学。',
    cover: 'https://picsum.photos/id/30/400/300',
  },
  {
    id: 'd4',
    name: '自然风光',
    image_num: 20,
    intro: '从森林到海洋，带你领略地球上最原始的生命力。',
    cover: 'https://picsum.photos/id/40/400/300',
  },
]

// 使用 usePageCache 缓存首页相册数据
const { data: cachedAlbums, refresh: refreshCache } = usePageCache<AlbumItem[]>({
  key: CACHE_KEYS.SEARCH_ALBUMS,
  fetchFn: async () => {
    if (!user.isLoggedIn) {
      return defaultAlbums
    }
    const res = await Apis.lsky.albums({
      headers: {
        Authorization: `Bearer ${user.token}`,
      },
      params: {
        keyword: '',
        page: 1,
        order: order.value,
      },
    })
    if (res.status) {
      return (res.data.data || []).map((item: any) => ({
        ...item,
        _error: false,
        cover: item.cover || PLACEHOLDER_IMAGE,
      }))
    }
    return []
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

// 加载相册数据
const { send: searchAlbums } = useRequest(
  (keyword: string, currentPage: number, orderValue: 'newest' | 'earliest' | 'most' | 'least') =>
    Apis.lsky.albums({
      headers: {
        Authorization: `Bearer ${user.token}`,
      },
      params: {
        keyword,
        page: currentPage,
        order: orderValue,
      },
    }),
  {
    immediate: false,
  }
)

// 获取相册数据
async function fetchAlbums(isLoadMore = false) {
  if (!user.isLoggedIn) {
    if (!isLoadMore) {
      albumList.value = defaultAlbums.map((item) => ({ ...item, _error: false }))
      hasMore.value = false
      isFirstLoad.value = false
    }
    return
  }

  if (!isLoadMore && albumList.value.length === 0) {
    isFirstLoad.value = true
  }

  loading.value = true

  try {
    const res = await searchAlbums(searchQuery.value, page.value, order.value)

    if (res.status) {
      const newData = (res.data.data || []).map((item: any) => ({
        ...item,
        _error: false,
        cover: item.cover || PLACEHOLDER_IMAGE,
      }))

      if (isLoadMore) {
        albumList.value.push(...newData)
      } else {
        albumList.value = newData
      }
      hasMore.value = res.data.last_page > res.data.current_page
    } else {
      toast.error(res.message || '加载相册失败')
    }
  } catch (error) {
    console.log(error)
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

function goImages(id: string | number) {
  if (user.isLoggedIn) {
    router.push({
      name: 'images',
      params: {
        id: String(id),
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



</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa] pb-10 transition-colors duration-300 dark:bg-black">
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
        <div class="ml-1 h-10 flex flex-shrink-0 items-center justify-center px-2" @tap="showSortSheet = true">
          <wd-icon name="order-descending" size="18px" :color="isDark ? '#999' : '#666'" />
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
            <div class="animate-pulse bg-gray-200 dark:bg-gray-800" :style="{ height: i % 2 === 0 ? '180px' : '220px' }" />
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
        <span class="text-[11px] text-gray-400 font-medium tracking-wider dark:text-gray-500">{{ albumList.length }} ALBUMS</span>
      </div>

      <!-- 瀑布流布局容器 - 使用 Flex 改写以提升稳定性 -->
      <div v-if="albumList.length > 0" class="flex gap-4 px-1">
        <!-- 左列 -->
        <div class="flex flex-1 flex-col">
          <div
            v-for="album in leftColList" :key="album.id"
            class="group relative mb-4 flex flex-col overflow-hidden rounded-2xl bg-white shadow-[0_4px_16px_rgba(0,0,0,0.04)] dark:bg-gray-900 active:opacity-80 dark:shadow-none"
            @tap="goImages(album.id)">
            <div class="relative w-full overflow-hidden bg-gray-100 dark:bg-gray-800">
              <image
                :src="album.cover" mode="widthFix" class="block h-auto w-full" lazy-load
                @error="handleImageError(album)" />
              <div
                class="absolute right-2.5 top-2.5 z-10 rounded-full bg-black/30 px-2 py-0.5 text-[10px] text-white font-bold backdrop-blur-md">
                {{ album.image_num }}
              </div>
            </div>
            <div class="p-3">
              <div class="text-sm text-gray-800 font-bold leading-snug dark:text-gray-200">{{ album.name }}</div>
              <div v-if="album.intro" class="mt-1.5 text-[11px] text-gray-400 leading-relaxed dark:text-gray-500">
                {{ album.intro }}
              </div>
            </div>
          </div>
        </div>

        <!-- 右列 -->
        <div class="flex flex-1 flex-col">
          <div
            v-for="album in rightColList" :key="album.id"
            class="group relative mb-4 flex flex-col overflow-hidden rounded-2xl bg-white shadow-[0_4px_16px_rgba(0,0,0,0.04)] dark:bg-gray-900 active:opacity-80 dark:shadow-none"
            @tap="goImages(album.id)">
            <div class="relative w-full overflow-hidden bg-gray-100 dark:bg-gray-800">
              <image
                :src="album.cover" mode="widthFix" class="block h-auto w-full" lazy-load
                @error="handleImageError(album)" />
              <div
                class="absolute right-2.5 top-2.5 z-10 rounded-full bg-black/30 px-2 py-0.5 text-[10px] text-white font-bold backdrop-blur-md">
                {{ album.image_num }}
              </div>
            </div>
            <div class="p-3">
              <div class="text-sm text-gray-800 font-bold leading-snug dark:text-gray-200">{{ album.name }}</div>
              <div v-if="album.intro" class="mt-1.5 text-[11px] text-gray-400 leading-relaxed dark:text-gray-500">
                {{ album.intro }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <EmptyState v-else-if="!loading" icon="search" title="未发现相关相册" description="尝试使用其他关键词搜索" />

      <!-- 底部状态与适配 -->
      <div class="mt-auto">
        <wd-loadmore custom-class="py-8" :state="loadingState" @reload="fetchAlbums" />
      </div>
    </div>

    <!-- 悬浮功能按钮 -->
    <div
      class="fixed bottom-24 right-6 z-50 flex flex-col gap-4 transition-all duration-300"
      :class="[showBackTop ? 'translate-y-0 opacity-100' : 'translate-y-20 opacity-0']">
      <div
        class="h-12 w-12 flex items-center justify-center rounded-full bg-white/90 shadow-lg backdrop-blur-md transition-transform active:scale-90 dark:bg-gray-800/90"
        @click="scrollToTop">
        <wd-icon name="arrow-up" size="20px" :color="isDark ? '#ddd' : '#333'" />
      </div>
    </div>

    <!-- 排序操作面板 -->
    <SortSheet
      v-model="showSortSheet"
      :options="orderOptions"
      :current-value="order"
      title="排序方式"
      subtitle="选择相册内容的展示顺序"
      @select="handleSortSelect"
    />
  </div>
</template>
