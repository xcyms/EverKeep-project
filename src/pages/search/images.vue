<script setup lang="ts">
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { onMounted, ref } from 'vue'

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

const albumId = ref(null)
const statusBarHeight = ref(0)
const menuButtonRight = ref(0)
const loading = ref(false)
const hasMore = ref(true)
const page = ref(1)
const searchQuery = ref('')
const order = ref<'newest' | 'earliest' | 'utmost' | 'least'>('newest')
const showSortSheet = ref(false)
const uploading = ref(false)

const orderOptions = [
  { name: '最新发布', value: 'newest', subname: '按上传时间从新到旧' },
  { name: '最早发布', value: 'earliest', subname: '按上传时间从旧到新' },
  { name: '最大尺寸', value: 'utmost', subname: '按文件大小从大到小' },
  { name: '最小尺寸', value: 'least', subname: '按文件大小从小到大' },
]

const leftColumnImages = ref<any[]>([])
const rightColumnImages = ref<any[]>([])

// 获取图片接口
const { send: getImages } = useRequest((currentPage: number, id: number, keyword: string, currentOrder: 'newest' | 'earliest' | 'utmost' | 'least') => Apis.lsky.getImages({
  params: {
    page: currentPage,
    order: currentOrder,
    album_id: id,
    keyword,
  },
  headers: {
    'Authorization': `Bearer ${user.token}`,
  },
}), {
  immediate: false,
})

// 加载图片数据
async function fetchImages(reset = false) {
  if (loading.value || (!hasMore.value && !reset)) return
  loading.value = true

  if (reset) {
    leftColumnImages.value = []
    rightColumnImages.value = []
    page.value = 1
    hasMore.value = true
  }

  try {
    const res = await getImages(page.value, albumId.value!, searchQuery.value, order.value)
    if (res.status) {
      const newData = res.data.data || []
      newData.forEach((img: any, index: number) => {
        if (index % 2 === 0) {
          leftColumnImages.value.push(img)
        } else {
          rightColumnImages.value.push(img)
        }
      })

      page.value++
      hasMore.value = res.data.current_page < res.data.last_page
    } else {
      toast.error(res.message || '获取图片失败')
    }
  } catch (error) {
    console.error('Error fetching images:', error);
    toast.error('请求失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 处理搜索
function handleSearch() {
  fetchImages(true)
}

// 处理排序选择
function handleSortSelect(item: any) {
  order.value = item.value
  showSortSheet.value = false
}

// 监听排序变化
watch(order, () => {
  fetchImages(true)
})

// 预览图片
function previewImage(url: string) {
  const allImages = [...leftColumnImages.value, ...rightColumnImages.value].map(img => img.links.url)
  uni.previewImage({
    urls: allImages,
    current: url,
  })
}

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

        // 在微信小程序中，直接使用 uni.uploadFile 是最稳健的方式
        // 它会自动处理 multipart/form-data 和 boundary
        uni.uploadFile({
          // 使用 Vite 的环境变量语法
          url: `${import.meta.env.VITE_API_BASE_URL}/upload`,
          filePath: tempFilePath,
          name: 'file', // 后端接收的字段名
          header: {
            'Authorization': `Bearer ${user.token}`,
            'Accept': 'application/json'
          },
          formData: {
            'album_id': String(albumId.value)
          },
          success: (uploadRes) => {
            const data = JSON.parse(uploadRes.data)
            if (data.status) {
              toast.success('上传成功')
              setTimeout(() => {
                fetchImages(true)
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
          }
        })
      } catch (error) {
        console.error('Upload catch error:', error)
        toast.error('上传准备失败')
        uploading.value = false
        uni.hideLoading()
      }
    }
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
  uni.getSystemInfo({
    success: (res) => {
      statusBarHeight.value = res.statusBarHeight || 0
      // #ifdef MP-WEIXIN
      const menuButton = uni.getMenuButtonBoundingClientRect()
      if (menuButton) {
        menuButtonRight.value = res.windowWidth - menuButton.left
      }
      // #endif
    },
  })

  if (albumId.value) {
    fetchImages(true)
  } else {
    toast.error('参数错误')
    setTimeout(() => uni.navigateBack(), 1500)
  }
})

// 触底加载
onReachBottom(() => {
  fetchImages()
})
</script>

<template>
  <div class="bg-[#f8f9fa] pb-10">
    <!-- 沉浸式顶栏 -->
    <div
      class="fixed left-0 right-0 top-0 z-50 bg-white/80 px-3 pb-2 backdrop-blur-xl transition-all"
      :style="{ paddingTop: `${statusBarHeight + 6}px` }"
    >
      <div class="flex items-center" :style="{ paddingRight: `${menuButtonRight}px` }">
        <div class="mr-2 h-10 flex items-center justify-center px-1" @tap="goBack">
          <wd-icon name="arrow-left" size="20px" color="#333" />
        </div>
        <div class="flex-1">
          <wd-search
            v-model="searchQuery"
            placeholder="搜索相册内图片..."
            @search="handleSearch"
            @clear="handleSearch"
            :hide-cancel="true"
            custom-class="!bg-gray-100/80 !rounded-xl !p-0"
          />
        </div>
        <!-- 排序触发按钮 -->
        <div class="ml-1 h-10 flex flex-shrink-0 items-center justify-center px-2" @tap="showSortSheet = true">
          <wd-icon name="order-descending" size="18px" color="#666" />
        </div>
      </div>
    </div>

    <!-- 占位 -->
    <div :style="{ height: `${statusBarHeight + 54}px` }" />

    <!-- 内容区域 -->
    <div class="px-3 pt-3">
      <div v-if="leftColumnImages.length > 0 || rightColumnImages.length > 0" class="flex flex-row items-start gap-3">
        <!-- 左列 -->
        <div class="flex flex-1 flex-col gap-3">
          <div
            v-for="img in leftColumnImages"
            :key="img.key"
            class="overflow-hidden rounded-xl bg-white shadow-[0_4px_16px_rgba(0,0,0,0.04)] transition-all active:scale-[0.98] active:opacity-90"
            @tap="previewImage(img.links.url)"
          >
            <image
              :src="img.links.url"
              mode="widthFix"
              class="fade-in block w-full"
              lazy-load
            />
          </div>
        </div>
        <!-- 右列 -->
        <div class="flex flex-1 flex-col gap-3">
          <div
            v-for="img in rightColumnImages"
            :key="img.key"
            class="overflow-hidden rounded-xl bg-white shadow-[0_4px_16px_rgba(0,0,0,0.04)] transition-all active:scale-[0.98] active:opacity-90"
            @tap="previewImage(img.links.url)"
          >
            <image
              :src="img.links.url"
              mode="widthFix"
              class="fade-in block w-full"
              lazy-load
            />
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading" class="flex flex-col items-center justify-center pt-32">
        <div class="mb-4 h-20 w-20 flex items-center justify-center rounded-full bg-gray-100">
          <wd-icon name="picture" size="32px" color="#cbd5e1" />
        </div>
        <span class="text-sm text-gray-400 font-medium">相册内暂无图片</span>
      </div>

      <!-- 加载状态 -->
      <wd-loadmore
        custom-class="py-8"
        :state="loading ? 'loading' : (hasMore ? 'loading' : 'finished')"
      />
    </div>

    <!-- 悬浮上传按钮 -->
    <div
      class="fixed bottom-10 right-6 z-50 transform-gpu transition-all active:scale-90"
      @tap="handleUpload"
    >
      <div class="h-14 w-14 flex items-center justify-center rounded-full bg-blue-500 text-white shadow-[0_8px_32px_rgba(59,130,246,0.3)]">
        <wd-icon name="add" size="24px" />
      </div>
    </div>

    <!-- 排序操作面板 -->
    <wd-popup
      v-model="showSortSheet"
      position="bottom"
      round
      custom-class="rounded-t-[32rpx] overflow-hidden"
      :z-index="10001"
      safe-area-inset-bottom
    >
      <div class="bg-white/95 px-6 pb-2 backdrop-blur-md">
        <!-- 顶部装饰条 -->
        <div class="flex justify-center py-3">
          <div class="h-1 w-10 rounded-full bg-gray-200" />
        </div>

        <!-- 头部 -->
        <div class="flex items-center justify-between pb-4 pt-2">
          <div class="flex flex-col">
            <span class="text-lg text-gray-900 font-bold">排序方式</span>
            <span class="mt-0.5 text-xs text-gray-400">选择图片内容的展示顺序</span>
          </div>
          <div
            class="h-8 w-8 flex items-center justify-center rounded-full bg-gray-100 transition-colors active:bg-gray-200"
            @tap="showSortSheet = false"
          >
            <wd-icon name="close" size="16px" color="#666" />
          </div>
        </div>

        <!-- 选项列表 -->
        <div class="py-2 space-y-2">
          <div
            v-for="item in orderOptions"
            :key="item.value"
            class="group flex items-center justify-between rounded-2xl p-4 transition-all active:bg-blue-50/50"
            :class="[order === item.value ? 'bg-blue-50/40' : 'bg-gray-50/50']"
            @tap="handleSortSelect(item)"
          >
            <div class="flex items-center gap-3">
              <div
                class="h-10 w-10 flex items-center justify-center rounded-xl transition-colors"
                :class="[order === item.value ? 'bg-blue-600 text-white' : 'bg-white text-gray-400']"
              >
                <wd-icon
                  :name="item.value.includes('est') ? 'time' : 'chart-bar'"
                  size="20px"
                />
              </div>
              <div class="flex flex-col">
                <span class="text-[15px] transition-colors" :class="[order === item.value ? 'text-blue-700 font-semibold' : 'text-gray-700']">
                  {{ item.name }}
                </span>
                <span class="text-xs transition-colors" :class="[order === item.value ? 'text-blue-500/80' : 'text-gray-400']">
                  {{ item.subname }}
                </span>
              </div>
            </div>
            <div
              v-if="order === item.value"
              class="h-6 w-6 flex items-center justify-center rounded-full bg-blue-600"
            >
              <wd-icon name="check" size="14px" color="#fff" />
            </div>
          </div>
        </div>

        <!-- 底部完成按钮 -->
        <div class="py-6">
          <div
            class="w-full rounded-2xl bg-gray-900 py-4 text-center text-[16px] text-white font-bold shadow-gray-200 shadow-lg transition-all active:scale-[0.98] active:opacity-90"
            @tap="showSortSheet = false"
          >
            完成
          </div>
        </div>
      </div>
    </wd-popup>
  </div>
</template>

<style lang="scss" scoped>
.fade-in {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
