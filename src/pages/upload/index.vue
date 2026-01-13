<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { getMyConfigsApi } from '../../api/config'
import { uploadImageApi } from '../../api/image'
import { getMyAlbumsApi } from '../../api/album'
import type { API } from '../../types'

const fileList = ref<API.UploadFile[]>([])  
const albumList = ref<API.Album[]>([])
const selectedAlbumId = ref<number | null>(null)
const isUploading = ref(false)

// 计算当前显示的相册名称
const currentAlbumName = computed(() => {
  if (!selectedAlbumId.value) return '默认相册'
  const album = albumList.value.find(a => a.id === selectedAlbumId.value)
  return album ? album.name : '默认相册'
})

// 配置相关
const configs = ref<API.SysConfig[]>([])
const maxFileSize = computed(() => {
  const config = configs.value.find(c => c.configKey === 'max_file_size')
  return config ? parseInt(config.configValue) : 10 * 1024 * 1024 // 默认10MB
})
const allowedExtensions = computed(() => {
  const config = configs.value.find(c => c.configKey === 'allowed_extensions')
  return config ? config.configValue.split(',').map(s => s.trim().toLowerCase()) : ['jpg', 'jpeg', 'png', 'gif', 'webp']
})

// 加载配置和相册
const initData = async () => {
  try {
    const [configRes, albumRes] = await Promise.all([
      getMyConfigsApi(),
      getMyAlbumsApi()
    ])
    configs.value = configRes
    albumList.value = albumRes.data || []
  } catch (error) {
    console.error('加载基础数据失败', error)
  }
}

onMounted(() => {
  initData()
})

onUnmounted(() => {
  fileList.value.forEach(f => {
    if (f.preview) URL.revokeObjectURL(f.preview)
  })
})

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
    const newFiles = Array.from(input.files).map(f => {
      const item: API.UploadFile = {
        id: Math.random().toString(36).slice(2),
        file: f,
        name: f.name,
        size: f.size,
        progress: 0,
        status: 'pending' as const
      }
      if (f.type.startsWith('image/')) {
        item.preview = URL.createObjectURL(f)
      }
      return item
    })
    addFiles(newFiles)
  }
}

// 添加文件到列表
const addFiles = (files: API.UploadFile[]) => {
  const validFiles: API.UploadFile[] = []
  
  for (const file of files) {
    const ext = file.name.split('.').pop()?.toLowerCase() || ''
    
    // 1. 校验后缀
    if (!allowedExtensions.value.includes(ext)) {
      message.error(`文件 [${file.name}] 后缀名不支持，允许的后缀: ${allowedExtensions.value.join(', ')}`)
      if (file.preview) URL.revokeObjectURL(file.preview)
      continue
    }
    
    // 2. 校验大小
    if (file.size > maxFileSize.value) {
      message.error(`文件 [${file.name}] 超过限制大小 (${formatSize(maxFileSize.value)})`)
      if (file.preview) URL.revokeObjectURL(file.preview)
      continue
    }

    validFiles.push(file)
  }

  fileList.value.push(...validFiles)
}

// 执行上传逻辑
const startUpload = async () => {
  const pending = fileList.value.filter(f => f.status === 'pending' || f.status === 'error')
  if (pending.length === 0) return

  isUploading.value = true
  
  for (const item of pending) {
    item.status = 'uploading'
    item.progress = 0
    
    try {
      const formData = new FormData()
      formData.append('file', item.file)
      if (selectedAlbumId.value) {
        formData.append('albumId', selectedAlbumId.value.toString())
      }

      const res = await uploadImageApi(formData)
      
      item.status = 'success'
      item.progress = 100
      item.url = res.data?.url || ''
    } catch (err: any) {
      item.status = 'error'
      item.progress = 0
      console.error(`文件 ${item.name} 上传失败:`, err)
    }
  }
  
  isUploading.value = false
  
  const successCount = pending.filter(f => f.status === 'success').length
  const failCount = pending.filter(f => f.status === 'error').length

  if (successCount === pending.length) {
    message.success(`全部 ${successCount} 个文件上传成功`)
  } else if (failCount === pending.length) {
    message.error('文件上传失败，请检查配置或重试')
  } else {
    message.warning(`上传处理完成：${successCount} 个成功，${failCount} 个失败`)
  }
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
  fileList.value.forEach(f => {
    if (f.preview) URL.revokeObjectURL(f.preview)
  })
  fileList.value = []
}

// 删除单个文件
const removeFile = (id: string) => {
  const index = fileList.value.findIndex(f => f.id === id)
  if (index !== -1) {
    const file = fileList.value[index]
    if (file && file.preview) URL.revokeObjectURL(file.preview)
    fileList.value.splice(index, 1)
  }
}

// 拖拽处理
const isDragOver = ref(false)
const handleDrop = (e: DragEvent) => {
  isDragOver.value = false
  if (e.dataTransfer?.files) {
    const newFiles = Array.from(e.dataTransfer.files).map(f => {
      const item: API.UploadFile = {
        id: Math.random().toString(36).slice(2),
        file: f,
        name: f.name,
        size: f.size,
        progress: 0,
        status: 'pending' as const
      }
      if (f.type.startsWith('image/')) {
        item.preview = URL.createObjectURL(f)
      }
      return item
    })
    addFiles(newFiles)
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto space-y-6 pb-20">
    <!-- 顶部提醒 -->
    <a-alert type="info" show-icon class="rounded-lg border-blue-100">
      <template #message>
        <div class="flex items-center gap-2">
          <span>当前上传至:</span>
          <a-dropdown :trigger="['click']" :disabled="isUploading">
            <a-tag color="blue" class="m-0 font-medium cursor-pointer hover:opacity-80 flex items-center gap-1.5 px-2.5 py-1 border-blue-200 transition-all active:scale-95">
              <div class="i-ant-design:folder-open-outlined text-sm" />
              <span>{{ currentAlbumName }}</span>
              <div class="i-ant-design:down-outlined text-[10px] opacity-60" />
            </a-tag>
            <template #overlay>
              <a-menu @click="({ key }) => selectedAlbumId = (key === 'default' ? null : key) as number | null" class="min-w-[140px] shadow-xl border border-gray-100">
                <a-menu-item key="default">
                  <div class="flex items-center gap-2 py-0.5">
                    <div class="i-ant-design:folder-outlined text-gray-400" />
                    <span>默认相册</span>
                    <div v-if="selectedAlbumId === null" class="i-ant-design:check-outlined ml-auto text-blue-500" />
                  </div>
                </a-menu-item>
                <a-menu-divider v-if="albumList.length > 0" />
                <a-menu-item v-for="album in albumList" :key="album.id">
                  <div class="flex items-center gap-2 py-0.5">
                    <div class="i-ant-design:folder-outlined text-gray-400" />
                    <span>{{ album.name }}</span>
                    <div v-if="selectedAlbumId === album.id" class="i-ant-design:check-outlined ml-auto text-blue-500" />
                  </div>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <span class="text-xs text-gray-400 ml-auto">
            支持 {{ allowedExtensions.join(', ').toUpperCase() }}, 最大 {{ formatSize(maxFileSize) }}
          </span>
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
            <img v-if="item.preview" :src="item.preview" class="w-full h-full object-cover" />
            <div v-else class="i-ant-design:picture-outlined text-gray-300 text-xl" />
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