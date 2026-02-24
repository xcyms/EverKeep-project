<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getRecycleImagesApi, restoreImagesApi, deletePermanentlyApi } from '../../api/image'
import { getRecycleVideoPageApi, restoreVideosApi, deleteVideosPermanentlyApi } from '../../api/video'
import { getImageUrl, DEFAULT_IMAGE, formatSize } from '../../utils/common'
import type { API } from '../../types'

const activeTab = ref<'image' | 'video'>('image')
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const selectedIds = ref<number[]>([])
const displayedItems = ref<(API.Image | API.Video)[]>([])

const queryParams = reactive({
  current: 1,
  size: 12,
})

const loadData = async (isRefresh = false) => {
  if (isRefresh) {
    queryParams.current = 1
    loading.value = true
    displayedItems.value = []
    selectedIds.value = [] // 切换 Tab 或刷新时清空选中
  } else {
    loadingMore.value = true
  }

  try {
    let res
    if (activeTab.value === 'image') {
      res = await getRecycleImagesApi({
        current: queryParams.current,
        size: queryParams.size,
      })
    } else {
      res = await getRecycleVideoPageApi({
        current: queryParams.current,
        size: queryParams.size,
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
    console.error('加载回收站失败', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

onMounted(() => {
  loadData(true)
})

watch(activeTab, () => {
  loadData(true)
})

const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  queryParams.current++
  loadData()
}

const toggleSelect = (id: number) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const handleRestore = (ids: number[]) => {
  const typeName = activeTab.value === 'image' ? '图片' : '视频'
  Modal.confirm({
    title: `确定要恢复选中的 ${ids.length} 个${typeName}吗？`,
    content: `恢复后${typeName}将回到原来的位置。`,
    async onOk() {
      try {
        if (activeTab.value === 'image') {
          await restoreImagesApi(ids)
        } else {
          await restoreVideosApi(ids)
        }
        message.success('恢复成功')
        selectedIds.value = []
        loadData(true)
      } catch (err) {}
    },
  })
}

const handleDeletePermanently = (ids: number[]) => {
  const typeName = activeTab.value === 'image' ? '图片' : '视频'
  Modal.confirm({
    title: `确定要永久删除选中的 ${ids.length} 个${typeName}吗？`,
    content: '永久删除后将无法恢复，且会释放存储空间。',
    okType: 'danger',
    async onOk() {
      try {
        if (activeTab.value === 'image') {
          await deletePermanentlyApi(ids)
        } else {
          await deleteVideosPermanentlyApi(ids)
        }
        message.success('已永久删除')
        selectedIds.value = []
        loadData(true)
      } catch (err) {}
    },
  })
}
</script>

<template>
  <div class="p-6">
    <div class="mb-6 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <div>
          <h1 class="text-2xl font-bold text-gray-800 dark:text-white mb-1">回收站</h1>
          <p class="text-gray-500 text-sm">在这里可以管理已删除的文件</p>
        </div>
        
        <!-- Tab Switcher -->
        <div class="flex bg-gray-100 dark:bg-gray-800 p-1 rounded-lg self-center ml-4">
          <button 
            @click="activeTab = 'image'"
            class="px-4 py-1.5 rounded-md text-sm font-medium transition-all"
            :class="activeTab === 'image' ? 'bg-white dark:bg-gray-700 text-blue-600 shadow-sm' : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200'"
          >
            图片
          </button>
          <button 
            @click="activeTab = 'video'"
            class="px-4 py-1.5 rounded-md text-sm font-medium transition-all"
            :class="activeTab === 'video' ? 'bg-white dark:bg-gray-700 text-blue-600 shadow-sm' : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200'"
          >
            视频
          </button>
        </div>
      </div>
      
      <div class="flex gap-3" v-if="selectedIds.length > 0">
        <button 
          @click="handleRestore(selectedIds)"
          class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors flex items-center gap-2"
        >
          <div class="i-fa6-solid:rotate-left"></div>
          恢复所选 ({{ selectedIds.length }})
        </button>
        <button 
          @click="handleDeletePermanently(selectedIds)"
          class="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors flex items-center gap-2"
        >
          <div class="i-fa6-solid:trash-can"></div>
          永久删除 ({{ selectedIds.length }})
        </button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && displayedItems.length === 0" class="flex flex-col items-center justify-center py-20 bg-white dark:bg-gray-800 rounded-2xl border border-dashed border-gray-200 dark:border-gray-700">
      <div class="text-6xl text-gray-200 mb-4 i-fa6-solid:trash-can"></div>
      <p class="text-gray-400">回收站是空的</p>
    </div>

    <!-- 列表 -->
    <div class="columns-2 md:columns-3 lg:columns-4 xl:columns-5 gap-4 space-y-4">
      <div 
        v-for="item in displayedItems" 
        :key="item.id"
        class="break-inside-avoid mb-4 group relative bg-white dark:bg-gray-800 rounded-xl overflow-hidden shadow-sm hover:shadow-xl transition-all duration-300 border border-gray-100 dark:border-gray-700"
        :class="{ 'ring-2 ring-blue-500': selectedIds.includes(item.id) }"
      >
        <!-- 复选框 -->
        <div 
          @click.stop="toggleSelect(item.id)"
          class="absolute top-2 left-2 z-10 w-6 h-6 rounded-full border-2 flex items-center justify-center cursor-pointer transition-all"
          :class="selectedIds.includes(item.id) ? 'bg-blue-500 border-blue-500 text-white' : 'bg-black/20 border-white/50 text-transparent group-hover:border-white'"
        >
          <div class="i-fa6-solid:check text-[10px]"></div>
        </div>

        <!-- 预览 -->
        <div class="relative">
          <template v-if="activeTab === 'image'">
            <img 
              :src="getImageUrl((item as API.Image).thumbnailUrl || item.url)" 
              class="w-full h-auto block transition-transform duration-500 group-hover:scale-105"
              loading="lazy"
              @error="(e: any) => { e.target.src = DEFAULT_IMAGE }"
            />
          </template>
          <template v-else>
            <img 
              v-if="(item as API.Video).coverUrl" 
              :src="getImageUrl((item as API.Video).coverUrl!)" 
              class="w-full h-auto block transition-transform duration-500 group-hover:scale-105"
            />
            <div v-else class="w-full aspect-square flex items-center justify-center bg-gray-800">
              <div class="i-fa6-solid:video text-gray-600 text-3xl"></div>
            </div>
            <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
              <div class="i-ant-design:play-circle-outlined text-white/80 text-3xl"></div>
            </div>
          </template>
        </div>

        <!-- 悬浮操作 -->
        <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-6">
          <button 
            @click="handleRestore([item.id])"
            class="w-12 h-12 bg-white/20 backdrop-blur-md text-white rounded-full hover:bg-white/40 transition-colors flex items-center justify-center"
            title="恢复"
          >
            <div class="i-ant-design:reload-outlined"></div>
          </button>
          <button 
            @click="handleDeletePermanently([item.id])"
            class="w-12 h-12 bg-red-500/80 backdrop-blur-md text-white rounded-full hover:bg-red-500 transition-colors flex items-center justify-center"
            title="永久删除"
          >
            <div class="i-ant-design:delete-outlined"></div>
          </button>
        </div>

        <!-- 信息 -->
        <div class="p-3">
          <div class="text-sm font-medium truncate dark:text-gray-200" :title="item.name">{{ item.name }}</div>
          <div class="flex items-center justify-between mt-1 text-[10px] text-gray-400">
            <span>{{ formatSize(item.size) }}</span>
            <span>{{ item.createTime.split(' ')[0] }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore" class="mt-10 text-center">
      <button 
        @click="handleLoadMore"
        :disabled="loadingMore"
        class="px-8 py-2 bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 rounded-full hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors"
      >
        {{ loadingMore ? '加载中...' : '加载更多' }}
      </button>
    </div>
  </div>
</template>