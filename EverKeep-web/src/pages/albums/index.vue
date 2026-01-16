<script setup lang="ts">
import { ref, reactive, watch, h, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { ExclamationCircleOutlined, PictureOutlined, ClockCircleOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { getMyAlbumsPageApi, createAlbumApi, updateAlbumApi, deleteAlbumApi } from '../../api/album'
import { getMyImagesApi, setAlbumCoverApi } from '../../api/image'
import { getImageUrl } from '../../utils/common'
import type { API } from '../../types'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()

// --- 状态变量 ---
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const displayedAlbums = ref<API.Album[]>([])

// --- 新增/修改相册相关 ---
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const uploadUrl = `${import.meta.env.VITE_API_BASE_URL}/file/upload`

const albumForm = reactive({
  id: undefined as number | undefined,
  name: '',
  description: '',
  cover: ''
})

const rules: Record<string, Rule[]> = {
  name: [{ required: true, message: '请输入相册名称', trigger: 'blur' }],
  cover: [{ required: true, message: '请上传相册封面', trigger: 'change' }]
}

const openModal = (album?: API.Album) => {
  if (album) {
    isEdit.value = true
    albumForm.id = album.id
    albumForm.name = album.name
    albumForm.description = album.description
    albumForm.cover = album.cover
  } else {
    isEdit.value = false
    albumForm.id = undefined
    albumForm.name = ''
    albumForm.description = ''
    albumForm.cover = ''
  }
  modalVisible.value = true
}

const handleUploadChange = (info: any) => {
  if (info.file.status === 'uploading') {
    modalLoading.value = true
    return
  }
  if (info.file.status === 'done') {
    modalLoading.value = false
    const res = info.file.response
    // 注意：如果是通过 a-upload 内部处理的，响应已经脱壳或者拦截器已处理
    // 但通常 a-upload 会收到原始响应，这里假设拦截器处理了全局错误，但手动判断下业务 code 也是安全的
    if (res.code === 200) {
      albumForm.cover = res.data.url
      message.success('封面上传成功')
    }
  } else if (info.file.status === 'error') {
    modalLoading.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    modalLoading.value = true
    
    if (isEdit.value) {
      await updateAlbumApi(albumForm as API.Album)
      message.success('相册修改成功')
    } else {
      await createAlbumApi(albumForm as API.Album)
      message.success('相册创建成功')
    }
    
    modalVisible.value = false
    loadData(true)
  } catch (error) {
    console.error('提交失败', error)
  } finally {
    modalLoading.value = false
  }
}

const queryParams = reactive({
  keyword: '',
  sort: 'a.create_time-desc',
  current: 1,
  size: 8,
})

// --- 预览相关 ---
const previewVisible = ref(false)
const currentAlbum = ref<API.Album | null>(null)
const albumImages = ref<API.Image[]>([])
const loadingImages = ref(false)
const imagePagination = reactive({
  current: 1,
  size: 12,
  total: 0
})

// --- 核心逻辑：获取数据 ---
const loadData = async (isRefresh = false) => {
  if (isRefresh) {
    queryParams.current = 1
    loading.value = true
    displayedAlbums.value = []
  } else {
    loadingMore.value = true
  }

  try {
    const [column, order] = queryParams.sort.split('-')
    const res = await getMyAlbumsPageApi({
      current: queryParams.current,
      size: queryParams.size,
      column: column,
      asc: order === 'asc'
    }, {
      name: queryParams.keyword
    })
    
    const list = res.records || []
    
    if (isRefresh) {
      displayedAlbums.value = list
    } else {
      displayedAlbums.value.push(...list)
    }

    hasMore.value = displayedAlbums.value.length < (res.total || 0)
  } catch (error) {
    console.error('加载相册失败', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

onMounted(() => {
  loadData(true)
})

// 监听筛选和排序变化
watch(() => [queryParams.sort], () => loadData(true), { immediate: true })

// 搜索防抖
let timer: any = null
watch(() => queryParams.keyword, () => {
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => loadData(true), 500)
})

const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  queryParams.current++
  loadData()
}

// --- 预览功能 ---
const loadAlbumImages = async (page = 1) => {
  if (!currentAlbum.value) return
  
  loadingImages.value = true
  imagePagination.current = page
  try {
    const res = await getMyImagesApi({
      current: imagePagination.current,
      size: imagePagination.size,
      column: 'createTime',
      asc: false
    }, {
      albumId: currentAlbum.value.id
    })
    albumImages.value = res.records || []
    imagePagination.total = Number(res.total || 0)
  } catch (error) {
    console.error('加载相册图片失败', error)
    message.error('加载图片失败')
  } finally {
    loadingImages.value = false
  }
}

const handlePreview = (album: API.Album) => {
  currentAlbum.value = album
  previewVisible.value = true
  loadAlbumImages(1)
}

const handleImagePageChange = (page: number) => {
  loadAlbumImages(page)
}

// --- 查看详情 ---
const showDetails = (album: API.Album) => {
  Modal.info({
    title: '相册详情',
    width: 450,
    icon: h(PictureOutlined, { style: 'color: #1890ff' }),
    content: h('div', { class: 'space-y-4 pt-4' }, [
      h('div', { class: 'flex justify-between' }, [h('span', { class: 'text-gray-500' }, '相册名称:'), h('span', { class: 'font-medium' }, album.name)]),
      h('div', { class: 'flex justify-between' }, [h('span', { class: 'text-gray-500' }, '图片数量:'), h('span', { class: 'font-medium' }, `${album.imageCount} 张`)]),
      h('div', { class: 'flex justify-between' }, [h('span', { class: 'text-gray-500' }, '创建时间:'), h('span', { class: 'font-medium' }, album.createTime)]),
      h('div', { class: 'space-y-1.5' }, [
        h('span', { class: 'text-gray-500 block' }, '相册描述:'),
        h('div', { class: 'p-3 bg-gray-50 rounded-lg text-sm text-gray-600 italic' }, album.description || '暂无描述')
      ]),
      h('div', { class: 'space-y-1.5' }, [
        h('span', { class: 'text-gray-500 block' }, '相册封面:'),
        h('img', { src: getImageUrl(album.cover), class: 'w-full h-40 object-cover rounded-xl border border-gray-100 shadow-sm' })
      ])
    ]),
    okText: '知道了',
    centered: true
  })
}

// --- 设置封面 ---
const handleSetCover = async (img: API.Image) => {
  Modal.confirm({
    title: '设为封面',
    icon: h(ExclamationCircleOutlined),
    content: '确定要将这张图片设为相册封面吗？',
    async onOk() {
      try {
        message.loading({ content: '设置中...', key: 'setCover' })
        await setAlbumCoverApi(img.id)
        if (currentAlbum.value) {
          currentAlbum.value.cover = img.url
        }
        message.success({ content: '封面设置成功', key: 'setCover' })
        loadData(true) // 刷新列表以显示新封面
      } catch (error) {
        message.error({ content: '设置失败', key: 'setCover' })
      }
    }
  })
}

// --- 删除相册 ---
const handleDeleteAlbum = (album: API.Album) => {
  Modal.confirm({
    title: '确认删除',
    icon: h(ExclamationCircleOutlined, { style: 'color: #ff4d4f' }),
    content: `确定要删除相册 "${album.name}" 吗？此操作将同时删除相册内的所有图片且不可撤销。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        await deleteAlbumApi(album.id)
        message.success('相册已成功删除')
        loadData(true)
      } catch (error) {}
    }
  })
}
</script>

<template>
  <div class="flex flex-col gap-6">
    <!-- 顶部工具栏 -->
    <div class="bg-white p-5 rounded-xl shadow-sm border border-gray-100 flex flex-wrap items-center justify-between gap-4">
      <div class="flex items-center gap-4 flex-1 min-w-[300px]">
        <a-input-search
          v-model:value="queryParams.keyword"
          placeholder="搜索相册名称..."
          allow-clear
          enter-button
          class="max-w-xs"
        />
        <a-select v-model:value="queryParams.sort" class="w-44">
          <template #suffixIcon><div class="i-fa6-solid:sort text-gray-400 mr-1" /></template>
          <a-select-option value="a.create_time-desc">按创建时间 (新→旧)</a-select-option>
          <a-select-option value="a.create_time-asc">按创建时间 (旧→新)</a-select-option>
          <a-select-option value="imageCount-desc">按图片数量 (多→少)</a-select-option>
          <a-select-option value="imageCount-asc">按图片数量 (少→多)</a-select-option>
        </a-select>
      </div>
      <a-button type="primary" class="toolbar-btn flex items-center gap-2" @click="openModal()">
        <template #icon><div class="i-fa6-solid:plus" /></template>
        新建相册
      </a-button>
    </div>

    <!-- 相册网格 -->
    <a-spin :spinning="loading">
      <div v-if="displayedAlbums.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        <div 
          v-for="album in displayedAlbums" 
          :key="album.id"
          class="group bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300"
        >
          <!-- 相册封面 -->
          <div class="relative aspect-[4/3] overflow-hidden bg-gray-100">
            <img 
              :src="getImageUrl(album.cover)" 
              class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
              alt="cover"
            />
            <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-3">
              <a-button type="primary" shape="round" @click="handlePreview(album)">
                <template #icon><PictureOutlined /></template>
                查看内容
              </a-button>
            </div>
            <div class="absolute top-3 right-3">
              <a-dropdown :trigger="['click']">
                <div class="w-8 h-8 bg-white/90 backdrop-blur rounded-full flex items-center justify-center cursor-pointer hover:bg-white shadow-sm" @click.stop>
                  <div class="i-fa6-solid:ellipsis-vertical text-gray-600" />
                </div>
                <template #overlay>
                  <a-menu>
                    <a-menu-item key="edit" @click="openModal(album)">
                      <template #icon><div class="i-fa6-solid:pen-to-square" /></template>
                      编辑名称
                    </a-menu-item>
                    <a-menu-item key="delete" danger @click="handleDeleteAlbum(album)">
                      <template #icon><div class="i-fa6-solid:trash-can" /></template>
                      删除相册
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
            <div class="absolute bottom-3 left-3 px-2 py-1 bg-black/50 backdrop-blur rounded text-[10px] text-white flex items-center gap-1">
              <PictureOutlined /> {{ album.imageCount }} 张图片
            </div>
          </div>

          <!-- 相册信息 -->
          <div class="p-4">
            <h4 class="text-base font-bold text-gray-800 mb-1 truncate">{{ album.name }}</h4>
            <p v-if="album.description" class="text-xs text-gray-400 mb-2 line-clamp-2 min-h-[32px]" :title="album.description">
              {{ album.description }}
            </p>
            <div class="flex items-center justify-between text-xs text-gray-400">
              <span class="flex items-center gap-1"><ClockCircleOutlined /> {{ album.createTime.split(' ')[0] }}</span>
              <span class="hover:text-blue-500 cursor-pointer transition-colors" @click="showDetails(album)">查看详情</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty v-else-if="!loading" description="未找到相关相册" class="my-24" />
    </a-spin>

    <!-- 加载更多 -->
    <div v-if="displayedAlbums.length > 0" class="flex justify-center py-10">
      <a-button 
        v-if="hasMore" 
        :loading="loadingMore" 
        @click="handleLoadMore"
        class="px-10 h-11 rounded-full border-gray-200 text-gray-600 hover:text-blue-500 hover:border-blue-500 transition-all"
      >
        {{ loadingMore ? '正在努力加载...' : '查看更多相册' }}
      </a-button>
      <div v-else class="text-gray-300 text-sm flex items-center gap-3">
        <span class="w-12 h-px bg-gray-100"></span>
        已经到底啦
        <span class="w-12 h-px bg-gray-100"></span>
      </div>
    </div>

    <!-- 新增/编辑相册弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑相册' : '新建相册'"
      :confirm-loading="modalLoading"
      @ok="handleSubmit"
      centered
    >
      <a-form
        ref="formRef"
        :model="albumForm"
        :rules="rules"
        layout="vertical"
        class="mt-4"
      >
        <a-form-item label="相册封面" name="cover">
          <a-upload
            name="file"
            list-type="picture-card"
            class="cover-uploader"
            :show-upload-list="false"
            :action="uploadUrl"
            :data="{ category: 'album_cover' }"
            :headers="{ Authorization: `Bearer ${userStore.token}` }"
            @change="handleUploadChange"
          >
            <img v-if="albumForm.cover" :src="getImageUrl(albumForm.cover)" alt="cover" class="w-full h-full object-cover" />
            <div v-else>
              <loading-outlined v-if="modalLoading" />
              <plus-outlined v-else />
              <div class="ant-upload-text">上传封面</div>
            </div>
          </a-upload>
        </a-form-item>
        <a-form-item label="相册名称" name="name">
          <a-input v-model:value="albumForm.name" placeholder="请输入相册名称" />
        </a-form-item>
        <a-form-item label="相册描述" name="description">
          <a-textarea v-model:value="albumForm.description" placeholder="请输入相册描述（可选）" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 相册内容预览弹窗 -->
    <a-modal
      v-model:open="previewVisible"
      :title="currentAlbum?.name"
      width="1000px"
      :footer="null"
      centered
      destroyOnClose
    >
      <div class="min-h-[400px]">
        <div v-if="loadingImages" class="flex flex-col items-center justify-center py-20 gap-4">
          <a-spin size="large" />
          <span class="text-gray-400">加载图片中...</span>
        </div>
        <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 p-2">
          <div 
            v-for="img in albumImages" 
            :key="img.id" 
            class="group relative aspect-square rounded-lg overflow-hidden border border-gray-100"
          >
            <a-image :src="getImageUrl(img.url)" class="w-full h-full object-cover" />
            <div class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
              <a-button type="primary" size="small" @click="handleSetCover(img)">
                设为封面
              </a-button>
            </div>
          </div>
        </div>
        
        <!-- 图片分页 -->
        <div v-if="imagePagination.total > imagePagination.size" class="flex justify-center mt-6 pb-2">
          <a-pagination
            v-model:current="imagePagination.current"
            :total="imagePagination.total"
            :page-size="imagePagination.size"
            show-less-items
            @change="handleImagePageChange"
          />
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
:deep(.ant-image) {
  width: 100%;
  height: 100%;
}
</style>