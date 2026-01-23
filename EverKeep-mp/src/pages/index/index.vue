<script lang="ts" setup>
import type { SortOption } from '@/types/common'
import type { ImageItem } from '@/types/page'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { computed, ref, watch } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import SortSheet from '@/components/common/SortSheet.vue'
import ImageWaterfall from '@/components/ImageWaterfall.vue'
import { useListPagination } from '@/composables/usePagination'
import { getImageUrl } from '@/utils'
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
  { name: '最新发布', value: 'newest', subname: '按上传时间从新到旧', icon: 'time' },
  { name: '最早发布', value: 'earliest', subname: '按上传时间从旧到新', icon: 'history' },
  { name: '最大尺寸', value: 'utmost', subname: '按文件大小从大到小', icon: 'order-descending' },
  { name: '最小尺寸', value: 'least', subname: '按文件大小从小到大', icon: 'order-descending' },
]

const category = ref([
  { name: '画廊', id: 0 },
  { name: '时光', id: 1 },
  { name: '推荐', id: 2 },
])

// 使用 useListPagination 管理图片列表
const {
  list: allImages,
  loading,
  hasMore,
  loadMore,
  refresh,
  reset,
} = useListPagination<ImageItem>({
  fetchFn: async (currentPage) => {
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 800))

    // 未登录时返回模拟数据
    if (!user.isLoggedIn) {
      const mockImages: ImageItem[] = []
      // 模拟所有分类都有 3 页数据
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
        })
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
    if (categoryId.value === 2) {
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
      // 画廊(0)使用公共接口，时光(1)使用个人接口
      const apiMethod = categoryId.value === 0 ? Apis.everkeep.publicPage : Apis.everkeep.imagePage
      const res = await apiMethod({
        params: {
          current: currentPage,
          size: PAGINATION.DEFAULT_PAGE_SIZE,
          column,
          asc,
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
    } catch (e) {
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

// 计算属性：将 allImages 按日期分组
const timeGroups = computed(() => {
  if (categoryId.value !== 1) return []

  const groups: Record<string, ImageItem[]> = {}
  const now = new Date()
  const todayMonthDay = `${now.getMonth() + 1}-${now.getDate()}`

  allImages.value.forEach((img) => {
    // 统一处理日期格式，确保只保留 YYYY-MM-DD
    const rawDate = img.createTime || (img as any).create_time || ''
    if (!rawDate) return

    const date = rawDate.includes('T')
      ? rawDate.split('T')[0]
      : rawDate.split(' ')[0]

    if (!groups[date]) groups[date] = []
    groups[date].push(img)
  })

  return Object.keys(groups)
    .sort((a, b) => b.localeCompare(a))
    .map((date) => {
      const dateObj = new Date(date)
      const monthDay = `${dateObj.getMonth() + 1}-${dateObj.getDate()}`
      return {
        date, // 格式为 YYYY-MM-DD
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
  const urls = allImages.value.map((img) => getImageUrl(img.url))
  uni.previewImage({
    urls,
    current: getImageUrl(url),
  })
}

function changeCategory(event: { index: number; name: string }) {
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
  }
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
            @change="changeCategory"
            custom-class="!bg-transparent"
            custom-nav-class="!bg-transparent"
          >
            <block v-for="item in category" :key="item.id">
              <wd-tab :title="item.name" custom-class="!bg-transparent" />
            </block>
          </wd-tabs>
        </div>
        <!-- 排序触发按钮 -->
        <div class="h-10 flex flex-shrink-0 items-center justify-center px-3" @tap="showSortSheet = true">
          <wd-icon name="order-descending" size="18px" :color="isDark ? '#eee' : '#666'" />
        </div>
      </div>
    </div>

    <!-- 顶部占位，防止内容被固定头遮挡 -->
    <div :style="{ height: `${statusBarHeight + 44}px` }"/>

    <!-- 内容区域 - 使用原生页面滚动 -->
    <div class="px-3 pt-3">
      <!-- 画廊模式：瀑布流 -->
      <ImageWaterfall v-if="categoryId === 0" :list="allImages" :loading="loading" />

      <!-- 时光模式：时间轴分组 -->
      <div v-else-if="categoryId === 1" class="space-y-6">
        <template v-if="timeGroups.length > 0">
          <div v-for="group in timeGroups" :key="group.date" class="relative pl-4">
            <!-- 左侧时间轴线条 -->
            <div class="absolute bottom-0 left-0 top-0 w-[1px] bg-gray-200 dark:bg-gray-800" />
            <div
              class="absolute left-[-3px] top-4 h-2 w-2 border border-white rounded-full dark:border-black"
              :class="group.isTodayInHistory ? 'bg-orange-500 scale-125' : 'bg-primary'"
            />

            <!-- 日期标题 -->
            <div
              class="sticky top-[110px] z-10 mb-3 flex items-center gap-2 py-2 backdrop-blur-sm transition-colors"
              :class="group.isTodayInHistory ? 'bg-orange-50/90 dark:bg-orange-900/20 -ml-4 pl-8 pr-2 rounded-r-full' : 'bg-[#f8f9fa]/80 dark:bg-black/80'"
            >
              <span
                class="text-lg font-bold"
                :class="group.isTodayInHistory ? 'text-orange-600 dark:text-orange-400' : 'text-gray-900 dark:text-white'"
              >
                {{ group.date }}
              </span>
              <!-- 那年今日挂件 -->
              <div v-if="group.isTodayInHistory" class="flex items-center gap-1 rounded-full bg-orange-100 px-2 py-0.5 dark:bg-orange-900/40">
                <span class="text-[10px] text-orange-600 font-bold dark:text-orange-300">那年今日</span>
              </div>
            </div>

            <!-- 图片网格 -->
            <div class="grid grid-cols-3 gap-1">
              <div
                v-for="img in group.images"
                :key="img.id"
                class="aspect-square overflow-hidden rounded-sm bg-gray-100 active:opacity-80"
                @tap="handleImageTap(img.url)"
              >
                <image :src="getImageUrl(img.url)" mode="aspectFill" class="h-full w-full" lazy-load />
              </div>
            </div>
          </div>
        </template>

        <!-- 时光模式空状态 -->
        <div v-else-if="!loading" class="py-20">
          <EmptyState
            icon="clock"
            title="时光里还没有照片"
            description="记录美好瞬间，从第一张照片开始"
            action-text="去画廊看看"
            @action="categoryId = 0"
          />
        </div>
      </div>

      <!-- 推荐模式：暂未开放 -->
      <div v-else-if="categoryId === 2" class="py-20">
        <EmptyState
          icon="setting"
          title="推荐功能暂未开放"
          description="我们正在努力开发中，敬请期待"
        />
      </div>

      <!-- 加载状态 -->
      <div v-if="allImages.length > 0">
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
      <wd-button @click="handleLogin" type="primary">立即登录</wd-button>
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
</style>
