<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'

interface UploadFile {
  id: string
  file: File
  name: string
  size: number
  progress: number
  status: 'pending' | 'uploading' | 'success' | 'error'
  url?: string
}

const fileList = ref<UploadFile[]>([])
const defaultAlbum = ref('默认相册')
const isUploading = ref(false)

// 检查是否有待上传的文件
const hasPendingFiles = computed(() => fileList.value.some(f => f.status === 'pending' || f.status === 'error'))

// 格式化文件大小
const formatSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 处理文件选择
const handleFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files) {
    addFiles(Array.from(input.files))
  }
}

// 添加文件到列表
const addFiles = (files: File[]) => {
  const newFiles = files.map(file => ({
    id: Math.random().toString(36).slice(2),
    file,
    name: file.name,
    size: file.size,
    progress: 0,
    status: 'pending' as const
  }))
  fileList.value.push(...newFiles)
}

// 执行上传逻辑
const startUpload = async () => {
  const pending = fileList.value.filter(f => f.status === 'pending' || f.status === 'error')
  if (pending.length === 0) return

  isUploading.value = true
  
  for (const item of pending) {
    item.status = 'uploading'
    // 模拟上传过程
    try {
      await simulateUpload(item)
      item.status = 'success'
      item.url = `https://everkeep.storage/images/${item.id}.jpg`
    } catch (err) {
      item.status = 'error'
    }
  }
  
  isUploading.value = false
  message.success('全部文件处理完成')
}

// 模拟上传进度
const simulateUpload = (item: UploadFile) => {
  return new Promise((resolve, reject) => {
    let p = 0
    const timer = setInterval(() => {
      p += Math.random() * 30
      if (p >= 100) {
        item.progress = 100
        clearInterval(timer)
        resolve(true)
      } else {
        item.progress = Math.floor(p)
      }
    }, 300)
  })
}

// 处理主按钮点击
const handleMainAction = () => {
  if (hasPendingFiles.value) {
    startUpload()
  } else {
    document.getElementById('file-input')?.click()
  }
}

// 清空列表
const clearList = () => {
  if (isUploading.value) return
  fileList.value = []
}

// 删除单个文件
const removeFile = (id: string) => {
  fileList.value = fileList.value.filter(f => f.id !== id)
}

// 拖拽处理
const isDragOver = ref(false)
const handleDrop = (e: DragEvent) => {
  isDragOver.value = false
  if (e.dataTransfer?.files) {
    addFiles(Array.from(e.dataTransfer.files))
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto space-y-6 pb-20">
    <!-- 顶部提醒 -->
    <a-alert type="info" show-icon class="rounded-lg border-blue-100">
      <template #message>
        <div class="flex items-center gap-2">
          <span>当前默认上传至相册:</span>
          <a-tag color="blue" class="m-0 font-medium cursor-pointer hover:opacity-80">
            {{ defaultAlbum }}
            <template #icon><div class="i-ant-design:edit-outlined" /></template>
          </a-tag>
          <span class="text-xs text-gray-400 ml-auto">支持 JPG, PNG, GIF, 最大 20MB</span>
        </div>
      </template>
    </a-alert>

    <!-- 大上传区域 -->
    <div 
      class="relative group cursor-pointer transition-all duration-300"
      @click="handleMainAction"
      @dragover.prevent="isDragOver = true"
      @dragleave.prevent="isDragOver = false"
      @drop.prevent="handleDrop"
    >
      <input 
        id="file-input" 
        type="file" 
        multiple 
        class="hidden" 
        accept="image/*"
        @change="handleFileChange"
      />
      
      <div 
        class="border-2 border-dashed rounded-2xl p-12 flex flex-col items-center justify-center gap-4 transition-all"
        :class="[
          isDragOver ? 'border-blue-500 bg-blue-50/50 scale-[1.01]' : 'border-gray-200 bg-white hover:border-blue-400 hover:bg-gray-50/50',
          hasPendingFiles ? 'py-16' : 'py-24'
        ]"
      >
        <div 
          class="w-20 h-20 rounded-full flex items-center justify-center transition-all duration-500"
          :class="hasPendingFiles ? 'bg-blue-500 text-white animate-bounce' : 'bg-blue-50 text-blue-500'"
        >
          <div :class="hasPendingFiles ? 'i-ant-design:cloud-upload-outlined' : 'i-ant-design:plus-outlined'" class="text-4xl" />
        </div>
        
        <div class="text-center">
          <h3 class="text-xl font-semibold text-gray-800 mb-1">
            {{ hasPendingFiles ? '准备就绪，点击开始上传' : '点击或拖拽图片至此处' }}
          </h3>
          <p class="text-gray-400 text-sm">
            {{ hasPendingFiles ? `已选择 ${fileList.filter(f => f.status === 'pending').length} 张新图片` : '支持多选批量上传，极速云端存储' }}
          </p>
        </div>

        <div v-if="hasPendingFiles" class="mt-2">
          <a-button type="primary" size="large" shape="round" class="px-8 h-12 shadow-lg shadow-blue-200">
            立即上传
          </a-button>
        </div>
      </div>
    </div>

    <!-- 待上传列表 -->
    <div v-if="fileList.length > 0" class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-50 flex items-center justify-between bg-gray-50/30">
        <h4 class="font-medium text-gray-700 flex items-center gap-2">
          上传列表 
          <span class="text-xs font-normal text-gray-400">({{ fileList.length }} 个文件)</span>
        </h4>
        <a-button type="link" danger size="small" @click="clearList" :disabled="isUploading">
          <template #icon><div class="i-ant-design:clear-outlined" /></template>
          清空列表
        </a-button>
      </div>

      <div class="divide-y divide-gray-50">
        <div v-for="item in fileList" :key="item.id" class="px-6 py-4 flex items-center gap-4 hover:bg-gray-50/50 transition-colors">
          <!-- 缩略图预览 (本地) -->
          <div class="w-12 h-12 rounded bg-gray-100 flex-shrink-0 flex items-center justify-center overflow-hidden border border-gray-100">
            <div class="i-ant-design:picture-outlined text-gray-300 text-xl" />
          </div>

          <!-- 文件信息与进度 -->
          <div class="flex-1 min-w-0 space-y-1">
            <div class="flex items-center justify-between gap-4">
              <span class="text-sm font-medium text-gray-700 truncate" :title="item.name">{{ item.name }}</span>
              <span class="text-xs text-gray-400 flex-shrink-0">{{ formatSize(item.size) }}</span>
            </div>
            
            <div class="flex items-center gap-3">
              <div class="flex-1">
                <a-progress 
                  :percent="item.progress" 
                  size="small" 
                  :status="item.status === 'error' ? 'exception' : (item.status === 'success' ? 'success' : 'active')"
                  :show-info="false"
                />
              </div>
              <span class="text-[10px] w-8 text-right text-gray-400">{{ item.progress }}%</span>
            </div>

            <!-- 成功后的 URL -->
            <div v-if="item.status === 'success'" class="flex items-center gap-2 bg-green-50 px-2 py-0.5 rounded border border-green-100 mt-1">
              <span class="text-[10px] text-green-600 truncate flex-1 font-mono">{{ item.url }}</span>
              <div class="i-ant-design:copy-outlined text-green-500 cursor-pointer hover:text-green-700 text-xs" @click="message.success('链接已复制')" />
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex-shrink-0">
            <div v-if="item.status === 'uploading'" class="i-ant-design:loading-outlined animate-spin text-blue-500" />
            <div v-else-if="item.status === 'success'" class="i-ant-design:check-circle-filled text-green-500" />
            <div v-else-if="item.status === 'error'" class="i-ant-design:exclamation-circle-filled text-red-500 cursor-pointer" @click="startUpload" />
            <div v-else class="i-ant-design:close-outlined text-gray-300 hover:text-red-400 cursor-pointer transition-colors" @click="removeFile(item.id)" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.ant-alert-info) {
  background-color: #f0f7ff;
}
</style>