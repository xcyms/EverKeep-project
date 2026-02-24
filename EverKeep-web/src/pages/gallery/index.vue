<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { getPublicImagesApi } from '../../api/image'
import { getPublicVideoPageApi } from '../../api/video'
import type { API } from '../../types'
import { getImageUrl, DEFAULT_IMAGE, formatSize } from '../../utils/common'

// --- 状态定义 ---
const activeTab = ref<'image' | 'video'>('image')
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const displayedItems = ref<(API.Image | API.Video)[]>([])
const previewVisible = ref(false)
const currentVideoUrl = ref('')

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
    let res
    if (activeTab.value === 'image') {
      res = await getPublicImagesApi({
        current: queryParams.current,
        size: queryParams.size,
        column: queryParams.sort.split('_').slice(0, -1).join('_'),
        asc: queryParams.sort.endsWith('_asc')
      }, {
        name: queryParams.name
      })
    } else {
      res = await getPublicVideoPageApi({
        current: queryParams.current,
        size: queryParams.size,
        column: queryParams.sort.split('_').slice(0, -1).join('_'),
        asc: queryParams.sort.endsWith('_asc')
      }, {
        name: queryParams.name
      })
    }

    const list = res.records || []
    if (isRefresh) {
      displayedItems.value = list
    } else {
      displayedItems.value.push(...list)
    }

    hasMore.value = displayedItems.value.length < (res.total || 0)
  } catch (error) {
    console.error('加载画廊数据失败', error)
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
watch(activeTab, () => {
  queryParams.name = ''
  loadData(true)
})

const handleSearch = () => loadData(true)

const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  queryParams.current++
  loadData()
}

const playVideo = (video: API.Video) => {
  currentVideoUrl.value = getImageUrl(video.url)
  previewVisible.value = true
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
        
        <!-- Tab Switcher -->
        <div class="flex bg-gray-100 p-1 rounded-lg">
          <button 
            @click="activeTab = 'image'"
            class="px-4 py-1.5 rounded-md text-sm font-medium transition-all"
            :class="activeTab === 'image' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
          >
            图片
          </button>
          <button 
            @click="activeTab = 'video'"
            class="px-4 py-1.5 rounded-md text-sm font-medium transition-all"
            :class="activeTab === 'video' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
          >
            视频
          </button>
        </div>

        <a-input-search
          v-model:value="queryParams.name"
          :placeholder="activeTab === 'image' ? '搜索图片名称...' : '搜索视频名称...'"
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

    <!-- 图片/视频列表 -->
    <a-spin :spinning="loading" tip="正在发现美好...">
      <div v-if="displayedItems.length > 0" class="columns-2 md:columns-3 lg:columns-4 xl:columns-5 gap-4 min-h-[400px] space-y-4">
        <div 
          v-for="item in displayedItems" 
          :key="item.id"
          class="break-inside-avoid mb-4 group bg-white rounded-2xl overflow-hidden border border-gray-100 transition-all hover:shadow-xl hover:-translate-y-1 cursor-pointer"
        >
          <!-- 图片展示 -->
          <div v-if="activeTab === 'image'" class="relative">
            <a-image
              :src="getImageUrl((item as API.Image).thumbnailUrl || item.url)"
              :fallback="DEFAULT_IMAGE"
              class="w-full h-auto block"
              :preview="true"
            />
            <!-- 悬浮层 -->
            <div class="absolute inset-x-0 bottom-0 p-3 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
              <div class="text-white text-xs flex items-center gap-1">
                <div class="i-fa6-solid:user text-[10px]" />
                用户 ID: {{ item.userId }}
              </div>
            </div>
          </div>

          <!-- 视频展示 -->
          <div v-else class="relative group" @click="playVideo(item as API.Video)">
            <img 
              v-if="(item as API.Video).coverUrl" 
              :src="getImageUrl((item as API.Video).coverUrl!)" 
              class="w-full h-auto block group-hover:scale-105 transition-transform duration-500" 
            />
            <div v-else class="w-full aspect-video flex items-center justify-center bg-gray-900">
              <div class="i-fa6-solid:video text-gray-700 text-3xl"></div>
            </div>
            
            <div class="absolute inset-0 bg-black/20 group-hover:bg-black/40 flex items-center justify-center transition-colors">
              <div class="i-ant-design:play-circle-outlined text-white text-4xl opacity-80 group-hover:opacity-100 transition-all scale-90 group-hover:scale-110"></div>
            </div>
            
            <!-- 底部信息层 -->
            <div class="absolute inset-x-0 bottom-0 p-3 bg-gradient-to-t from-black/60 to-transparent pointer-events-none">
              <div class="flex justify-between items-end">
                <div class="text-white text-xs flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  <div class="i-fa6-solid:user text-[10px]" />
                  用户 ID: {{ item.userId }}
                </div>
                <div v-if="(item as API.Video).duration" class="px-1.5 py-0.5 bg-black/50 rounded text-[10px] text-white backdrop-blur-sm">
                  {{ Math.floor((item as API.Video).duration! / 60) }}:{{ ((item as API.Video).duration! % 60).toString().padStart(2, '0') }}
                </div>
              </div>
            </div>
          </div>

          <!-- 底部信息 -->
          <div class="p-3">
            <div class="text-sm font-bold text-gray-800 truncate mb-1" :title="item.name">
              {{ item.name }}
            </div>
            <div class="flex items-center justify-between">
              <span class="text-[11px] text-gray-400">{{ formatSize(item.size) }}</span>
              <span class="text-[11px] text-gray-400">{{ item.createTime.split(' ')[0] }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty v-else-if="!loading" :description="activeTab === 'image' ? '画廊空空如也，快去上传并公开你的图片吧' : '暂无公开视频，快去分享你的精彩时刻吧'" class="my-32">
        <template #image>
          <div class="i-ant-design:picture-outlined text-6xl text-gray-200 mx-auto" />
        </template>
      </a-empty>
    </a-spin>

    <!-- 加载更多 -->
    <div v-if="displayedItems.length > 0" class="flex flex-col items-center py-12 gap-4">
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

    <!-- 视频预览 Modal -->
    <a-modal v-model:open="previewVisible" :footer="null" width="800px" destroyOnClose centered :bodyStyle="{ padding: 0 }">
      <div class="bg-black aspect-video flex items-center justify-center">
        <video :src="currentVideoUrl" controls autoplay class="max-w-full max-h-full"></video>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
:deep(.ant-image) {
  display: block;
  width: 100%;
}
:deep(.ant-image-img) {
  width: 100%;
  height: auto;
  display: block;
}
</style>