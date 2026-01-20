<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { getPublicImagesApi } from '../../api/image'
import type { API } from '../../types'
import { getImageUrl } from '../../utils/common'

// --- 状态定义 ---
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const displayedImages = ref<API.Image[]>([])

const queryParams = reactive({
  current: 1,
  size: 24,
  sort: 'create_time_desc',
  name: ''
})

// --- 数据加载 ---
const loadData = async (isRefresh = false) => {
  if (isRefresh) {
    queryParams.current = 1
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const res = await getPublicImagesApi({
      current: queryParams.current,
      size: queryParams.size,
      column: queryParams.sort.split('_').slice(0, -1).join('_'),
      asc: queryParams.sort.endsWith('_asc')
    }, {
      name: queryParams.name
    })

    const list = res.records || []
    if (isRefresh) {
      displayedImages.value = list
    } else {
      displayedImages.value.push(...list)
    }

    hasMore.value = displayedImages.value.length < (res.total || 0)
  } catch (error) {
    console.error('加载画廊图片失败', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

onMounted(() => {
  loadData(true)
})

// 监听排序和搜索变化
watch(() => queryParams.sort, () => loadData(true))

const handleSearch = () => loadData(true)

const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  queryParams.current++
  loadData()
}

// 格式化文件大小
const formatSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <!-- 顶部筛选栏 -->
    <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-wrap items-center justify-between gap-4">
      <div class="flex items-center gap-4">
        <h2 class="text-lg font-bold text-gray-800 m-0 flex items-center gap-2">
          <div class="i-fa6-solid:images text-blue-500" />
          公共画廊
        </h2>
        <a-input-search
          v-model:value="queryParams.name"
          placeholder="搜索图片名称..."
          class="w-64"
          @search="handleSearch"
          allow-clear
        />
      </div>
      
      <div class="flex items-center gap-3">
        <span class="text-gray-500 text-sm">排序:</span>
        <a-select v-model:value="queryParams.sort" class="w-40">
          <a-select-option value="create_time_desc">最新发布</a-select-option>
          <a-select-option value="create_time_asc">最早发布</a-select-option>
          <a-select-option value="size_desc">文件最大</a-select-option>
          <a-select-option value="size_asc">文件最小</a-select-option>
        </a-select>
      </div>
    </div>

    <!-- 图片瀑布流/网格区 -->
    <a-spin :spinning="loading" tip="正在发现美好...">
      <div v-if="displayedImages.length > 0" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-6 min-h-[400px]">
        <div 
          v-for="img in displayedImages" 
          :key="img.id"
          class="group bg-white rounded-2xl overflow-hidden border border-gray-100 transition-all hover:shadow-xl hover:-translate-y-1 cursor-pointer"
        >
          <!-- 图片展示 -->
          <div class="aspect-[4/5] overflow-hidden bg-gray-50 relative">
            <a-image
              :src="getImageUrl(img.url)"
              class="w-full h-full object-cover"
              :preview="true"
            />
            <!-- 悬浮层：显示作者或其他信息 -->
            <div class="absolute inset-x-0 bottom-0 p-3 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity">
              <div class="text-white text-xs flex items-center gap-1">
                <div class="i-fa6-solid:user text-[10px]" />
                用户 ID: {{ img.userId }}
              </div>
            </div>
          </div>

          <!-- 底部信息 -->
          <div class="p-3">
            <div class="text-sm font-bold text-gray-800 truncate mb-1" :title="img.name">
              {{ img.name }}
            </div>
            <div class="flex items-center justify-between">
              <span class="text-[11px] text-gray-400">{{ formatSize(img.size) }}</span>
              <span class="text-[11px] text-gray-400">{{ img.createTime.split(' ')[0] }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty v-else-if="!loading" description="画廊空空如也，快去上传并公开你的图片吧" class="my-32">
        <template #image>
          <div class="i-ant-design:picture-outlined text-6xl text-gray-200 mx-auto" />
        </template>
      </a-empty>
    </a-spin>

    <!-- 加载更多 -->
    <div v-if="displayedImages.length > 0" class="flex flex-col items-center py-12 gap-4">
      <template v-if="hasMore">
        <a-button :loading="loadingMore" @click="handleLoadMore" size="large" shape="round" class="px-10 h-12 shadow-md hover:shadow-lg transition-shadow">
          {{ loadingMore ? '发现中...' : '加载更多精彩' }}
        </a-button>
      </template>
      <div v-else class="text-gray-300 text-sm flex items-center gap-3">
        <div class="w-12 h-[1px] bg-gray-100"></div>
        已经到底啦
        <div class="w-12 h-[1px] bg-gray-100"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.ant-image) {
  width: 100%;
  height: 100%;
}
:deep(.ant-image-img) {
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.group:hover :deep(.ant-image-img) {
  transform: scale(1.1);
}
</style>