<script lang="ts" setup>
import type { SortOption } from '@/types/common'
import type { ImageItem, VideoItem } from '@/types/page'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { computed, ref, watch } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import SortSheet from '@/components/common/SortSheet.vue'
import ImageWaterfall from '@/components/ImageWaterfall.vue'
import { useListPagination } from '@/composables/usePagination'
import { formatDuration, getImageUrl } from '@/utils'
import { PAGINATION } from '@/utils/constants'

definePage({
  name: 'home',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '首页',
    navigationStyle: 'custom',
    enablePullDownRefresh: true,
  },
})

const router = useRouter()
const user = useAuthStore()
const { statusBarHeight, menuButtonRight, safeAreaInsetsBottom } = useSystemInfo()
const categoryId = ref(0)
const order = ref<'newest' | 'earliest' | 'utmost' | 'least'>('newest')
const showSortSheet = ref(false)
const { isDark } = useManualTheme()

const orderOptions: SortOption[] = [
  { name: '最新发布', value: 'newest', subname: '按上传时间从新到旧', icon: 'i-solar-clock-circle-bold-duotone' },
  { name: '最早发布', value: 'earliest', subname: '按上传时间从旧到新', icon: 'i-solar-history-bold-duotone' },
  { name: '最大尺寸', value: 'utmost', subname: '按文件大小从大到小', icon: 'i-solar-database-bold-duotone' },
  { name: '最小尺寸', value: 'least', subname: '按文件大小从小到大', icon: 'i-solar-database-linear' },
]

const category = ref([
  { name: '画廊', id: 0 },
  { name: '时光', id: 1 },
  { name: '视频', id: 2 },
  { name: '推荐', id: 3 },
])

// 使用 useListPagination 管理列表数据
const {
  list: listData,
  loading,
  hasMore,
  loadMore,
  refresh,
  reset,
} = useListPagination<ImageItem | VideoItem>({
  fetchFn: async (currentPage) => {
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 800))

    // 未登录时返回模拟数据
    if (!user.isLoggedIn) {
      // 视频分类不需要模拟数据
      if (categoryId.value === 2) {
        return {
          data: [],
          current_page: currentPage,
          last_page: 0,
          total: 0,
          per_page: PAGINATION.DEFAULT_PAGE_SIZE,
        }
      }

      const mockImages: ImageItem[] = []
      // 模拟其他分类都有 3 页数据
      for (let i = 0; i < PAGINATION.DEFAULT_PAGE_SIZE; i++) {
        const width = 200 + Math.floor(Math.random() * 100)
        const height = 200 + Math.floor(Math.random() * 200)
        mockImages.push({
          albumId: categoryId.value,
          createTime: new Date().toISOString(),
          id: `img-${categoryId.value}-${currentPage}-${i}`,
          name: `图片 ${categoryId.value}-${currentPage}-${i}`,
          size: width * height,
          status: {
            code: 0,
            desc: '',
          },
          type: 'image/jpeg',
          url: `https://picsum.photos/id/${Math.floor(Math.random() * 100)}/${width}/${height}`,
          userId: user.user?.id || 'mock-user',
        } as ImageItem)
      }

      return {
        data: mockImages,
        current_page: currentPage,
        last_page: 3,
        total: 3 * PAGINATION.DEFAULT_PAGE_SIZE,
        per_page: PAGINATION.DEFAULT_PAGE_SIZE,
      }
    }

    // 已登录：调用真实接口实现功能
    const sortMap: Record<string, { column: string, asc: boolean }> = {
      newest: { column: 'create_time', asc: false },
      earliest: { column: 'create_time', asc: true },
      utmost: { column: 'size', asc: false },
      least: { column: 'size', asc: true },
    }
    const { column, asc } = sortMap[order.value] || sortMap.newest

    try {
      // 根据不同分类配置接口和参数
      const tabConfigs: Record<number, { api: any, params?: any }> = {
        0: { api: Apis.everkeep.publicPage }, // 画廊
        1: { api: Apis.everkeep.imagePage }, // 时光
        2: { api: Apis.everkeep.videoPage }, // 视频
        3: { api: Apis.everkeep.publicPage, params: { column: 'size', asc: false } }, // 推荐
      }

      const config = tabConfigs[categoryId.value] || tabConfigs[0]
      const apiMethod = config.api
      const finalParams = config.params || { column, asc }

      const res = await apiMethod({
        params: {
          current: currentPage,
          size: PAGINATION.DEFAULT_PAGE_SIZE,
          ...finalParams,
        },
        data: {},
      })

      if (res.code === 200 && res.data) {
        return {
          data: res.data.records || [],
          current_page: res.data.current || currentPage,
          last_page: res.data.pages || 1,
          total: res.data.total || 0,
          per_page: res.data.records.size || PAGINATION.DEFAULT_PAGE_SIZE,
        }
      }
      throw new Error(res.message || '获取数据失败')
    }
    catch (e) {
      console.error('Failed to fetch images:', e)
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
  immediate: true,
})

// 视频列表数据
const allVideos = computed(() => listData.value as VideoItem[])

// 推荐/画廊列表数据
const allGalleryItems = computed(() => listData.value as ImageItem[])

// 计算属性：将 listData 按日期分组
const timeGroups = computed(() => {
  if (categoryId.value !== 1) {
    return []
  }

  const groups: Record<string, ImageItem[]> = {}
  const now = new Date()
  const todayMonthDay = `${now.getMonth() + 1}-${now.getDate()}`

  listData.value.forEach((item) => {
    const img = item as ImageItem
    // 统一处理日期格式，确保只保留 YYYY-MM-DD
    const rawDate = img.createTime || (img as any).create_time || ''
    if (!rawDate) {
      return
    }

    const date = rawDate.includes('T')
      ? rawDate.split('T')[0]
      : rawDate.split(' ')[0]

    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(img)
  })

  return Object.keys(groups)
    .sort((a, b) => b.localeCompare(a))
    .map((date) => {
      const dateObj = new Date(date)
      const now = new Date()
      const diffDays = Math.floor((new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime() - new Date(dateObj.getFullYear(), dateObj.getMonth(), dateObj.getDate()).getTime()) / (1000 * 60 * 60 * 24))

      let displayDate = date
      if (diffDays === 0) {
        displayDate = '今天'
      }
      else if (diffDays === 1) {
        displayDate = '昨天'
      }

      const monthDay = `${dateObj.getMonth() + 1}-${dateObj.getDate()}`
      return {
        date,
        displayDate,
        images: groups[date],
        isTodayInHistory: monthDay === todayMonthDay && dateObj.getFullYear() < now.getFullYear(),
      }
    })
})

onPullDownRefresh(async () => {
  await refresh()
  uni.stopPullDownRefresh()
})

function handleImageTap(url: string) {
  const urls = (listData.value as ImageItem[]).map(img => getImageUrl(img.url))
  uni.previewImage({
    urls,
    current: getImageUrl(url),
  })
}

function handleVideoTap(video: VideoItem) {
  uni.navigateTo({
    url: `/pages/video/preview?url=${encodeURIComponent(getImageUrl(video.url))}&name=${encodeURIComponent(video.name)}&duration=${video.duration || 0}`,
  })
}

function changeCategory(event: { index: number, name: string }) {
  categoryId.value = event.index
}

function handleLogin() {
  router.push({ name: 'login' })
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

// 监听 categoryId 变化以重置并获取图片
watch(categoryId, () => {
  uni.pageScrollTo({ scrollTop: 0, duration: 200 })
  reset()
  refresh()
})

// 监听登录状态变化，重新加载数据
watch(
  () => user.isLoggedIn,
  (newVal, oldVal) => {
    if (newVal !== oldVal) {
      reset()
      refresh()
    }
  },
)

// 触底加载更多
onReachBottom(() => {
  if (!user.isLoggedIn && categoryId.value !== 0) {
    // 未登录且不是画廊分类，不加载更多
    return
  }
  loadMore()
})
</script>

<template>
  <div class="bg-[#f8f9fa] dark:bg-black">
    <!-- 沉浸式顶部 Tab 栏 - 使用 fixed 固定 -->
    <div
      class="fixed left-0 right-0 top-0 z-50 bg-white/80 px-1 backdrop-blur-xl transition-all dark:bg-black/60"
      :style="{ paddingTop: `${statusBarHeight}px` }"
    >
      <div class="flex items-center justify-between" :style="{ paddingRight: `${menuButtonRight}px` }">
        <div class="flex-1 overflow-hidden">
          <wd-tabs
            v-model="categoryId"
            animated
            inactive-color="#999"
            :active-color="isDark ? '#fff' : '#333'"
            line-height="2px"
            custom-class="!bg-transparent"
            custom-nav-class="!bg-transparent"
            @change="changeCategory"
          >
            <block v-for="item in category" :key="item.id">
              <wd-tab :title="item.name" custom-class="!bg-transparent" />
            </block>
          </wd-tabs>
        </div>
        <!-- 排序触发按钮 -->
        <div class="h-10 flex flex-shrink-0 items-center justify-center px-3" @tap="showSortSheet = true">
          <div
            class="text-xl transition-all duration-300" :class="[
              order !== 'newest' ? 'i-solar-tuning-bold-duotone scale-110' : 'i-solar-sort-vertical-line-duotone',
            ]"
            :style="{ color: order !== 'newest' ? (isDark ? '#5189fb' : '#2979ff') : (isDark ? '#eee' : '#666') }"
          />
        </div>
      </div>
    </div>

    <!-- 顶部占位，防止内容被固定头遮挡 -->
    <div :style="{ height: `${statusBarHeight + 44}px` }" />

    <!-- 内容区域 - 使用原生页面滚动 -->
    <div class="px-3 pt-3">
      <!-- 初始加载骨架屏 -->
      <template v-if="loading && listData.length === 0">
        <!-- 画廊骨架屏 -->
        <div v-if="categoryId === 0" class="flex gap-3">
          <div v-for="col in 2" :key="col" class="flex flex-1 flex-col gap-3">
            <div
              v-for="i in 4" :key="i"
              class="skeleton-pulse w-full rounded-2xl bg-gray-100 dark:bg-gray-900"
              :style="{ height: `${[220, 160, 260, 200][i - 1]}px` }"
            />
          </div>
        </div>

        <!-- 时光骨架屏 -->
        <div v-else-if="categoryId === 1" class="pl-10 space-y-12">
          <div v-for="g in 2" :key="g" class="relative">
            <div class="absolute top-8 h-6 w-6 flex items-center justify-center -left-10">
              <div class="h-2 w-2 rounded-full bg-gray-200 dark:bg-gray-800" />
            </div>
            <div class="skeleton-pulse mb-6 h-10 w-40 rounded-xl bg-gray-100 dark:bg-gray-900" />
            <div class="grid grid-cols-3 gap-2 pr-4">
              <div v-for="i in 6" :key="i" class="skeleton-pulse aspect-square rounded-2xl bg-gray-100 dark:bg-gray-900" />
            </div>
          </div>
        </div>

        <!-- 推荐/视频骨架屏 -->
        <div v-else-if="categoryId >= 2" class="space-y-4">
          <div v-for="i in 3" :key="i" class="skeleton-pulse h-64 w-full rounded-2xl bg-gray-100 dark:bg-gray-900" />
        </div>
      </template>

      <!-- 画廊模式：瀑布流 -->
      <ImageWaterfall v-if="categoryId === 0" :list="allGalleryItems" :loading="loading" />

      <!-- 时光模式：时间轴分组 -->
      <div v-else-if="categoryId === 1" class="space-y-6">
        <template v-if="listData.length > 0">
          <div v-for="group in timeGroups" :key="group.date" class="relative pb-8 pl-6">
            <!-- 左侧时间轴线条 -->
            <div class="absolute bottom-0 left-[7px] top-0 w-[2px] bg-gray-100 dark:bg-gray-800" />

            <!-- 时间轴节点 -->
            <div
              class="absolute left-0 top-5 z-20 h-4 w-4 flex items-center justify-center rounded-full bg-white dark:bg-black"
            >
              <div
                class="h-2 w-2 rounded-full transition-all duration-300"
                :class="group.isTodayInHistory ? 'bg-orange-500 shadow-[0_0_8px_rgba(249,115,22,0.6)] scale-125' : 'bg-primary shadow-[0_0_8px_rgba(var(--wot-primary-rgb),0.4)]'"
              />
            </div>

            <!-- 日期标题 -->
            <div
              class="sticky z-10 mb-4 flex items-center gap-3 py-3 backdrop-blur-xl transition-all"
              :style="{ top: `${statusBarHeight + 44}px` }"
              :class="group.isTodayInHistory
                ? 'bg-orange-50/90 dark:bg-orange-950/40 -ml-6 pl-10 pr-4 rounded-r-2xl border-b border-orange-100/50 dark:border-orange-900/30'
                : 'bg-white/70 dark:bg-black/50 -mx-3 px-6 border-b border-gray-100/50 dark:border-gray-800/20 shadow-sm shadow-black/5'"
            >
              <div class="flex flex-col">
                <span
                  class="text-xl font-black tracking-tight"
                  :class="group.isTodayInHistory ? 'text-orange-600 dark:text-orange-400' : 'text-gray-900 dark:text-white'"
                >
                  {{ group.displayDate }}
                </span>
                <span v-if="group.displayDate !== group.date" class="text-[10px] text-gray-400 font-bold tracking-[0.2em] uppercase opacity-80">{{ group.date }}</span>
              </div>

              <!-- 那年今日挂件 -->
              <div v-if="group.isTodayInHistory" class="flex animate-pulse items-center gap-1.5 border border-orange-200 rounded-full from-orange-100 to-orange-50 bg-gradient-to-r px-3 py-1 dark:border-orange-800 dark:from-orange-900/40 dark:to-orange-900/20">
                <wd-icon name="time" size="12px" color="#ea580c" />
                <span class="text-[11px] text-orange-700 font-bold dark:text-orange-300">那年今日</span>
              </div>
            </div>

            <!-- 图片网格 -->
            <div class="grid grid-cols-3 gap-2">
              <div
                v-for="(img, imgIndex) in group.images"
                :key="`${img.id}-${imgIndex}`"
                class="group relative aspect-square overflow-hidden rounded-xl bg-gray-100 ring-1 ring-black/5 transition-all active:scale-95 dark:bg-gray-800 dark:ring-white/5"
                @tap="handleImageTap(img.url)"
              >
                <image :src="getImageUrl(img.thumbnailUrl || img.url)" mode="aspectFill" class="h-full w-full" lazy-load />
              </div>
            </div>
          </div>
        </template>

        <!-- 时光模式空状态 -->
        <div v-else-if="!loading">
          <EmptyState
            icon="clock"
            title="时光里还没有照片"
            description="记录美好瞬间，从第一张照片开始"
          />
        </div>
      </div>

      <!-- 视频模式 -->
      <div v-else-if="categoryId === 2" class="pb-10 space-y-6">
        <template v-if="allVideos.length > 0">
          <div
            v-for="(video, index) in allVideos"
            :key="`${video.id}-${index}`"
            class="group relative overflow-hidden rounded-2xl bg-white shadow-sm transition-all active:scale-[0.99] dark:bg-gray-900"
            @tap="handleVideoTap(video)"
          >
            <div class="relative h-72 w-full overflow-hidden bg-gray-900">
              <image
                :src="getImageUrl(video.coverUrl)"
                mode="aspectFill"
                class="h-full w-full opacity-70 transition-transform duration-500 group-active:scale-105"
                lazy-load
              />
              <div class="absolute inset-0 flex items-center justify-center">
                <div class="i-solar-play-circle-bold text-6xl text-white opacity-80" />
              </div>
              <!-- 时长显示 -->
              <div v-if="video.duration" class="absolute bottom-2 right-2 rounded-md bg-black/50 px-2 py-0.5 text-[10px] text-white backdrop-blur-sm">
                {{ formatDuration(video.duration) }}
              </div>
            </div>
            <div class="absolute bottom-0 left-0 right-0 from-black/80 via-black/30 to-transparent bg-gradient-to-t p-5">
              <div class="flex items-end justify-between">
                <div class="flex-1 overflow-hidden">
                  <div class="mb-2 flex items-center gap-2">
                    <span class="rounded bg-blue-500/90 px-1.5 py-0.5 text-[10px] text-white font-bold">视频</span>
                    <span class="text-[10px] text-white/60 font-medium tracking-wider uppercase">{{ video.type }}</span>
                  </div>
                  <h4 class="truncate text-base text-white font-bold">
                    {{ video.name || '未命名视频' }}
                  </h4>
                </div>
              </div>
            </div>
          </div>
        </template>
        <EmptyState v-else-if="!loading" icon="video" title="暂无视频" description="上传您的第一段精彩视频吧" />
      </div>

      <!-- 推荐模式：大图精选流 -->
      <div v-else-if="categoryId === 3" class="pb-10 space-y-6">
        <template v-if="allGalleryItems.length > 0">
          <!-- 顶部专题入口 -->
          <div class="bg-primary/10 dark:bg-primary/5 relative overflow-hidden rounded-2xl p-5">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-primary dark:text-primary-light text-lg font-bold">
                  今日专题
                </h3>
                <p class="text-primary/60 dark:text-primary-light/50 mt-1 text-xs">
                  探索世界的美好瞬间
                </p>
              </div>
              <wd-icon name="arrow-right" size="20px" custom-class="!text-primary/40" />
            </div>
          </div>

          <!-- 精选图片列表 -->
          <div
            v-for="(img, index) in allGalleryItems"
            :key="`${img.id}-${index}`"
            class="group relative overflow-hidden rounded-2xl bg-white shadow-sm transition-all active:scale-[0.99] dark:bg-gray-900"
            @tap="handleImageTap(img.url)"
          >
            <image
              :src="getImageUrl(img.thumbnailUrl || img.url)"
              mode="aspectFill"
              class="h-72 w-full transition-transform duration-500 group-active:scale-105"
              lazy-load
            />
            <div class="absolute bottom-0 left-0 right-0 from-black/80 via-black/30 to-transparent bg-gradient-to-t p-5">
              <div class="flex items-end justify-between">
                <div class="flex-1 overflow-hidden">
                  <div class="mb-2 flex items-center gap-2">
                    <span class="bg-primary/90 rounded px-1.5 py-0.5 text-[10px] text-white font-bold">精选</span>
                    <span class="text-[10px] text-white/60 font-medium tracking-wider">{{ img.type?.split('/')[1]?.toUpperCase() || 'IMAGE' }}</span>
                  </div>
                  <h4 class="truncate text-base text-white font-bold">
                    {{ img.name || '未命名图片' }}
                  </h4>
                </div>
                <div class="h-10 w-10 flex items-center justify-center rounded-full bg-white/10 backdrop-blur-md active:bg-white/20">
                  <wd-icon name="download" size="18px" color="#fff" />
                </div>
              </div>
            </div>
          </div>
        </template>
        <EmptyState v-else-if="!loading" icon="star" title="暂无推荐" description="更多精彩内容正在准备中" />
      </div>

      <!-- 加载状态 -->
      <div v-if="listData.length > 0">
        <wd-loadmore
          v-if="loading || !hasMore"
          custom-class="py-8"
          :state="loading ? 'loading' : 'finished'"
        />
        <!-- 引导上拉加载更多 -->
        <div
          v-else-if="hasMore"
          class="flex flex-col items-center justify-center py-8 transition-opacity active:opacity-60"
          @tap="loadMore"
        >
          <wd-icon name="arrow-up" size="16px" color="#999" class="animate-bounce" />
          <span class="mt-1 text-sm text-gray-400">轻触加载更多</span>
        </div>
      </div>
    </div>

    <!-- 未登录底部模糊提示栏 -->
    <view
      v-if="!user.isLoggedIn"
      :style="{ bottom: `${50 + safeAreaInsetsBottom}px` }"
      class="fixed left-0 right-0 z-[100] h-30 flex flex-col items-center justify-center gap-4 rounded-md bg-white/80 py-4 text-gray-600 backdrop-blur-sm dark:bg-black/60 dark:text-gray-300"
    >
      <wd-text text="登录后可查看更多内容" class="mb-4" />
      <wd-button type="primary" @click="handleLogin">
        立即登录
      </wd-button>
    </view>

    <!-- 排序操作面板 -->
    <SortSheet
      v-model="showSortSheet"
      :options="orderOptions"
      :current-value="order"
      @select="handleSortSelect"
    />
  </div>
</template>

<style lang="scss" scoped>
:deep(.tabs),
:deep(.wd-tabs__nav) {
  background-color: transparent !important;
}
.skeleton-pulse {
  position: relative;
  overflow: hidden;
}

.skeleton-pulse::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent 0,
    rgba(255, 255, 255, 0.4) 50%,
    transparent 100%
  );
  animation: skeleton-loading 1.5s infinite;
}

@keyframes skeleton-loading {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.dark .skeleton-pulse::after {
  background: linear-gradient(
    90deg,
    transparent 0,
    rgba(255, 255, 255, 0.05) 50%,
    transparent 100%
  );
}
</style>
