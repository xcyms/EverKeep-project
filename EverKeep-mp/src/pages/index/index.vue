<script lang="ts" setup>
import type { SortOption } from '@/types/common'
import type { ImageItem } from '@/types/page'
import { ref, watch } from 'vue'
import SortSheet from '@/components/common/SortSheet.vue'
import ImageWaterfall from '@/components/ImageWaterfall.vue'
import { useListPagination } from '@/composables/usePagination'
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
  { name: '个性化', id: 1 },
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
    // 仅“画廊”分类有数据，其他分类暂无功能
    if (categoryId.value !== 0) {
      return {
        data: [],
        current_page: currentPage,
        last_page: 0,
        total: 0,
        per_page: PAGINATION.DEFAULT_PAGE_SIZE,
      }
    }

    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 1800))

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
        last_page: 3, // 确保有分页
        total: 3 * PAGINATION.DEFAULT_PAGE_SIZE,
        per_page: PAGINATION.DEFAULT_PAGE_SIZE,
      }
    }

    // 已登录：调用真实接口实现功能
    const sortMap: Record<string, { column: string; asc: boolean }> = {
      newest: { column: 'create_time', asc: false },
      earliest: { column: 'create_time', asc: true },
      utmost: { column: 'size', asc: false },
      least: { column: 'size', asc: true },
    }
    const { column, asc } = sortMap[order.value] || sortMap.newest

    try {
      const res = await Apis.everkeep.publicPage({
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
          per_page: res.data.size || PAGINATION.DEFAULT_PAGE_SIZE,
        }
      }
      throw new Error(res.message || '获取数据失败')
    } catch (e) {
      console.error('Failed to fetch public images:', e)
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
  <div class="min-h-screen bg-[#f8f9fa] pb-10 dark:bg-black">
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
      <ImageWaterfall :list="allImages" :loading="loading" />

      <!-- 加载状态 -->
      <wd-loadmore
        custom-class="py-8"
        :state="loading ? 'loading' : (hasMore ? 'loading' : 'finished')"
      />
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
