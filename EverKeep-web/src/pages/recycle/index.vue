<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getRecycleImagesApi, restoreImagesApi, deletePermanentlyApi } from '../../api/image'
import { getImageUrl, DEFAULT_IMAGE } from '../../utils/common'
import type { API } from '../../types'

const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const selectedIds = ref<number[]>([])
const displayedImages = ref<API.Image[]>([])

const queryParams = reactive({
  current: 1,
  size: 12,
})

const loadData = async (isRefresh = false) => {
  if (isRefresh) {
    queryParams.current = 1
    loading.value = true
    displayedImages.value = []
  } else {
    loadingMore.value = true
  }

  try {
    const res = await getRecycleImagesApi({
      current: queryParams.current,
      size: queryParams.size,
    })
    const list = res.records || []
    if (isRefresh) {
      displayedImages.value = list
    } else {
      displayedImages.value.push(...list)
    }

    hasMore.value = displayedImages.value.length < (res.total || 0)
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
  Modal.confirm({
    title: `确定要恢复选中的 ${ids.length} 张图片吗？`,
    content: '恢复后图片将回到原来的位置。',
    async onOk() {
      try {
        await restoreImagesApi(ids)
        message.success('恢复成功')
        selectedIds.value = []
        loadData(true)
      } catch (err) {}
    },
  })
}

const handleDeletePermanently = (ids: number[]) => {
  Modal.confirm({
    title: `确定要永久删除选中的 ${ids.length} 张图片吗？`,
    content: '永久删除后将无法恢复，且会释放存储空间。',
    okType: 'danger',
    async onOk() {
      try {
        await deletePermanentlyApi(ids)
        message.success('已永久删除')
        selectedIds.value = []
        loadData(true)
      } catch (err) {}
    },
  })
}

const formatSize = (bytes: number) => {
  const kb = bytes / 1024
  return kb >= 1024 ? `${(kb / 1024).toFixed(2)} MB` : `${kb.toFixed(2)} KB`
}
</script>

<template>
  <div class="p-6">
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white mb-1">回收站</h1>
        <p class="text-gray-500 text-sm">在这里可以管理已删除的图片</p>
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
    <div v-if="!loading && displayedImages.length === 0" class="flex flex-col items-center justify-center py-20 bg-white dark:bg-gray-800 rounded-2xl border border-dashed border-gray-200 dark:border-gray-700">
      <div class="text-6xl text-gray-200 mb-4 i-fa6-solid:trash-can"></div>
      <p class="text-gray-400">回收站是空的</p>
    </div>

    <!-- 图片列表 -->
    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-6">
      <div 
        v-for="img in displayedImages" 
        :key="img.id"
        class="group relative bg-white dark:bg-gray-800 rounded-xl overflow-hidden shadow-sm hover:shadow-xl transition-all duration-300 border border-gray-100 dark:border-gray-700"
        :class="{ 'ring-2 ring-blue-500': selectedIds.includes(img.id) }"
      >
        <!-- 复选框 -->
        <div 
          @click.stop="toggleSelect(img.id)"
          class="absolute top-2 left-2 z-10 w-6 h-6 rounded-full border-2 flex items-center justify-center cursor-pointer transition-all"
          :class="selectedIds.includes(img.id) ? 'bg-blue-500 border-blue-500 text-white' : 'bg-black/20 border-white/50 text-transparent group-hover:border-white'"
        >
          <div class="i-fa6-solid:check text-[10px]"></div>
        </div>

        <!-- 图片预览 -->
        <div class="aspect-square overflow-hidden bg-gray-100 dark:bg-gray-900">
          <img 
            :src="getImageUrl(img.thumbnailUrl || img.url)" 
            class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
            loading="lazy"
            @error="(e: any) => { e.target.src = DEFAULT_IMAGE }"
          />
        </div>

        <!-- 悬浮操作 -->
        <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-6">
          <button 
            @click="handleRestore([img.id])"
            class="w-12 h-12 bg-white/20 backdrop-blur-md text-white rounded-full hover:bg-white/40 transition-colors flex items-center justify-center"
            title="恢复"
          >
            <div class="i-ant-design:reload-outlined"></div>
          </button>
          <button 
            @click="handleDeletePermanently([img.id])"
            class="w-12 h-12 bg-red-500/80 backdrop-blur-md text-white rounded-full hover:bg-red-500 transition-colors flex items-center justify-center"
            title="永久删除"
          >
            <div class="i-ant-design:delete-outlined"></div>
          </button>
        </div>

        <!-- 信息 -->
        <div class="p-3">
          <div class="text-sm font-medium truncate dark:text-gray-200" :title="img.name">{{ img.name }}</div>
          <div class="flex items-center justify-between mt-1 text-[10px] text-gray-400">
            <span>{{ formatSize(img.size) }}</span>
            <span>{{ img.createTime.split(' ')[0] }}</span>
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