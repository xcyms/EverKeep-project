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
  },
})

const router = useRouter()
const user = useAuthStore()
const { statusBarHeight, menuButtonRight, safeAreaInsetsBottom } = useSystemInfo()
const categoryId = ref(0)
const order = ref<'newest' | 'earliest' | 'utmost' | 'least'>('newest')
const showSortSheet = ref(false)

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
    // 未登录时返回模拟数据
    if (!user.isLoggedIn) {
      const mockImages: ImageItem[] = []
      if (categoryId.value === 0) {
        // 画廊分类：显示模拟数据
        for (let i = 0; i < PAGINATION.DEFAULT_PAGE_SIZE; i++) {
          mockImages.push({
            key: `img-${categoryId.value}-${currentPage}-${i}`,
            links: {
              url: `https://picsum.photos/id/${Math.floor(Math.random() * 100)}/200/${Math.floor(Math.random() * 100) + 200}`,
            },
          })
        }
      }
      // 其他分类：未登录不显示
      return {
        data: mockImages,
        current_page: currentPage,
        last_page: categoryId.value === 0 ? 1 : 0,
        total: mockImages.length,
        per_page: PAGINATION.DEFAULT_PAGE_SIZE,
      }
    }

    // 已登录：请求真实数据
    const res = await Apis.lsky.getImages({
      params: {
        page: currentPage,
        order: order.value,
        permission: categoryId.value === 0 ? 'public' : 'private',
      },
      headers: {
        Authorization: `Bearer ${user.token}`,
      },
    })

    if (res.status) {
      return {
        data: res.data.data || [],
        current_page: res.data.current_page,
        last_page: res.data.last_page,
        total: res.data.total || 0,
        per_page: res.data.per_page || PAGINATION.DEFAULT_PAGE_SIZE,
      }
    }

    throw new Error(res.message || '加载图片失败')
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
  <div class="min-h-screen bg-[#f8f9fa] pb-10">
    <!-- 沉浸式顶部 Tab 栏 - 使用 fixed 固定 -->
    <div
      class="fixed left-0 right-0 top-0 z-50 bg-white/80 px-1 backdrop-blur-xl transition-all"
      :style="{ paddingTop: `${statusBarHeight}px` }"
    >
      <div class="flex items-center justify-between" :style="{ paddingRight: `${menuButtonRight}px` }">
        <div class="flex-1 overflow-hidden">
          <wd-tabs
            v-model="categoryId"
            animated
            inactive-color="#999"
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
          <wd-icon name="order-descending" size="18px" color="#666" />
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
        :state="loading ? 'loading' : (hasMore ? 'finished' : 'finished')"
      />
    </div>

    <!-- 未登录底部模糊提示栏 -->
    <view
      v-if="!user.isLoggedIn"
      :style="{ bottom: `${50 + safeAreaInsetsBottom}px` }"
      class="fixed left-0 right-0 z-[100] h-30 flex flex-col items-center justify-center gap-4 rounded-md bg-white/80 py-4 text-gray-600 backdrop-blur-sm"
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
