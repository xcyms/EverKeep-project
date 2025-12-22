<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue'

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
const toast = useToast()
const statusBarHeight = ref(0) // 获取状态栏高度
const safeAreaInsetsBottom = ref(0) // 底部安全区域高度
const categoryId = ref(0)
const leftColumnImages = ref<any[]>([]) // 存储左列图片数据
const rightColumnImages = ref<any[]>([]) // 存储右列图片数据
const loading = ref(false)
const hasMore = ref(true) // 假设初始有更多内容
const page = ref(1)
const pageSize = 10 // 每次获取的图片数量

const category = ref([
  {
    name: '画廊',
    id: 0,
  },
  {
    name: '个性化',
    id: 1,
  },
  {
    name: '推荐',
    id: 2,
  },
])

function changeCategory (event: { index: number, name: string}) {
  categoryId.value = event.index
}

function getMockImages(catId: number) {
  // 模拟 API 请求延迟
  setTimeout(() => {
    let newImages: any[] = []
    if (catId === 0) { // 第一个 Tab: 始终显示默认 10 张图片
      for (let i = 0; i < pageSize; i++) {
        newImages.push({ key: `img-${catId}-${page.value}-${i}`,
          links: { url: `https://picsum.photos/id/${Math.floor(Math.random() * 100)}/200/${Math.floor(Math.random() * 100) + 200}` } }) // 随机高度模拟瀑布流
      }
      hasMore.value = false // 第一个 Tab 初始加载后不再加载更多
    } else { // 其他 Tab
      if (!user.isLoggedIn) {
        newImages = [] // 未登录时不显示图片
        hasMore.value = false
      } else {
        // 已登录时模拟获取更多图片
        for (let i = 0; i < pageSize; i++) {
          newImages.push({ key: `img-${catId}-${page.value}-${i}`,
            links: { url: `https://picsum.photos/id/${Math.floor(Math.random() * 100)}/200/${Math.floor(Math.random() * 100) + 200}` } }) // 随机高度模拟瀑布流
        }
        hasMore.value = page.value < 3 // 模拟有更多页
      }
    }

    // 将图片分配到两列
    newImages.forEach((img, index) => {
      if (index % 2 === 0) {
        leftColumnImages.value.push(img)
      } else {
        rightColumnImages.value.push(img)
      }
    })

    page.value++
    loading.value = false
  }, 2000)
}

const { send: getImages } = useRequest((currentPage: number, currentCategoryId: number) => Apis.lsky.getImages({
  params: {
    page: currentPage,
    order: 'newest',
    permission: currentCategoryId === 0 ? 'public' : 'private',
  },
  headers: {
    'Authorization': `Bearer ${user.token}`,
  },
}), {
  immediate: false,
})

// 获取图片
async function fetchImages(catId: number, reset: boolean = false) {
  if (loading.value && !reset) return // 防止重复加载
  loading.value = true

  if (reset) {
    leftColumnImages.value = []
    rightColumnImages.value = []
    page.value = 1
    hasMore.value = true
  }
  if (!user.isLoggedIn) {
    getMockImages(catId)
  } else {
    const res = await getImages(page.value, categoryId.value)
    if (res.status) {
      res.data.data.forEach((img: {
        key: string;
        name: string;
        origin_name: string;
        pathname: string;
        size: number;
        width: number;
        height: number;
        md5: string;
        sha1: string;
        human_date: string;
        date: number;
        links: {
          url: string;
          html: string;
          bbcode: string;
          markdown: string;
          markdown_with_link: string;
          thumbnail_url: string;
        }
      }, index: number) => {
        if (index % 2 === 0) {
          leftColumnImages.value.push(img)
        } else {
          rightColumnImages.value.push(img)
        }
      })
      page.value++ // 递增页码
      // Assuming res.data.data contains pagination info like current_page and last_page
      hasMore.value = res.data.data.current_page < res.data.data.last_page // 根据 API 响应更新是否有更多数据
    } else {
      toast.error(res.message)
      hasMore.value = false // 如果请求失败，则没有更多内容
    }
    loading.value = false // 无论成功或失败，请求完成后都将 loading 设置为 false
  }
}

function handleLogin() {
  router.push({ name: 'login' })
}

// 监听 categoryId 变化以重置并获取图片
watch(categoryId, (newCatId: number) => {
  fetchImages(newCatId, true)
}, { immediate: true }) // 添加 immediate: true，确保在组件挂载时立即执行一次

// 监听登录状态变化，重新加载数据
watch(() => user.isLoggedIn, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    fetchImages(categoryId.value, true)
  }
})

// 加载更多图片 (用于无限滚动)
async function loadMore() {
  if (hasMore.value && !loading.value && categoryId.value !== 0 && user.isLoggedIn) {
    await fetchImages(categoryId.value)
  }
}

onMounted(() => {
  uni.getSystemInfo({
    success: (res) => {
      statusBarHeight.value = res.statusBarHeight || 0
      safeAreaInsetsBottom.value = res.safeAreaInsets?.bottom || 0 // 获取底部安全区域高度
    },
  })
})

</script>

<template>
  <div class="flex flex-col">
    <view class="flex flex-1 flex-col" :style="{ paddingTop: `${statusBarHeight}px` }">
      <wd-tabs
        v-model="categoryId"
        animated
        inactive-color="#999"
        line-height="0"
        @change="changeCategory"
        :sticky="true"
        :offset-top="statusBarHeight"
        custom-class="tabs"
      >
        <block v-for="item in category" :key="item.id">
          <wd-tab :title="item.name">
            <scroll-view
              @scrolltolower="loadMore"
              v-if="(item.id === 0) || (item.id !== 0 && user.isLoggedIn)"
            >
              <view v-if="loading" class="h-[85vh] flex items-center justify-center"><wd-loading size="50px" /></view>
              <view v-else class="flex gap-2 p-2">
                <view class="flex flex-1 flex-col gap-2">
                  <view v-for="img in leftColumnImages" :key="img.key">
                    <wd-img :src="img.links.url" mode="widthFix" class="w-full" radius="10" :enable-preview="true" :show-menu-by-longpress="true" custom-class="wd-img-block" />
                  </view>
                </view>
                <view class="flex flex-1 flex-col gap-2">
                  <view v-for="img in rightColumnImages" :key="img.key">
                    <wd-img :src="img.links.url" mode="widthFix" class="w-full" radius="10" :enable-preview="true" :show-menu-by-longpress="true" custom-class="wd-img-block" />
                  </view>
                </view>
              </view>
            </scroll-view>

          </wd-tab>
        </block>
      </wd-tabs>
    </view>
    <!-- 未登录底部模糊提示栏 -->
    <view
      v-if="!user.isLoggedIn"
      :style="{ bottom: `${50 + safeAreaInsetsBottom}px` }"
      class="fixed left-0 right-0 h-30 flex flex-col items-center justify-center gap-4 rounded-md bg-white/80 py-4 text-gray-600 backdrop-blur-sm"
    >
      <wd-text text="登录后可查看更多内容" class="mb-4" />
      <wd-button @click="handleLogin" type="primary">立即登录</wd-button>
    </view>
  </div>
</template>

<style lang="scss">
  .tabs {
    .wd-tabs__nav-container {
      padding-left: 75rpx;
      justify-content: flex-start;
      gap: 70rpx;
      font-size: 26rpx;
    }
    .wd-tabs__nav-item {
      flex: none !important;

      &.is-active {
        font-size: 34rpx;
      }
    }
  }
  // 覆盖默认渲染的样式防止图片不显示
  .wd-img-block {
    display: block !important;
  }
</style>
