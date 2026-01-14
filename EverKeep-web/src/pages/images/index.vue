<script setup lang="ts">
import { ref, reactive, watch, h, onMounted } from 'vue'
import { message, Modal, Select } from 'ant-design-vue'
import { ExclamationCircleOutlined, FolderOutlined } from '@ant-design/icons-vue'
import { getMyImagesApi, deleteImagesApi, updateImagesStatusApi, moveImageToAlbumApi, setAlbumCoverApi } from '../../api/image'
import { getMyAlbumsApi } from '../../api/album'
import { getImageUrl } from '../../utils/common'
import type { API } from '../../types'

// --- 状态变量 ---
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const selectedIds = ref<number[]>([])
const displayedImages = ref<API.Image[]>([])
const albumList = ref<API.Album[]>([])

const queryParams = reactive({
  albumId: null as number | null,
  status: 'all',
  sort: 'createTime_desc',
  current: 1,
  size: 12,
})

// --- 核心逻辑：获取数据 ---
const loadAlbums = async () => {
  try {
    const res = await getMyAlbumsApi()
    albumList.value = res || []
  } catch (err) {}
}

const loadData = async (isRefresh = false) => {
  if (isRefresh) {
    queryParams.current = 1
    loading.value = true
    displayedImages.value = []
  } else {
    loadingMore.value = true
  }

  try {
    const res = await getMyImagesApi({
      current: queryParams.current,
      size: queryParams.size,
      column: queryParams.sort.split('_')[0],
      asc: queryParams.sort.split('_')[1] === 'asc'
    }, {
      albumId: queryParams.albumId,
      status: queryParams.status === 'all' ? null : queryParams.status
    })
    const list = res.records || []
    if (isRefresh) {
      displayedImages.value = list
    } else {
      displayedImages.value.push(...list)
    }

    hasMore.value = displayedImages.value.length < (res.total || 0)
  } catch (error) {
    console.error('加载图片失败', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

onMounted(() => {
  loadAlbums()
})

// 监听筛选条件变化
watch(() => [queryParams.albumId, queryParams.status, queryParams.sort], () => {
  loadData(true)
}, { immediate: true })

const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  queryParams.current++
  loadData()
}

// --- 操作方法 ---
const toggleSelect = (id: number) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const handleDelete = (ids: number[]) => {
  Modal.confirm({
    title: `确定要删除选中的 ${ids.length} 张图片吗？`,
    content: '删除后将无法恢复，请谨慎操作。',
    okType: 'danger',
    async onOk() {
      try {
        await deleteImagesApi(ids)
        message.success('删除成功')
        selectedIds.value = []
        loadData(true)
      } catch (err) {}
    },
  })
}

const handleBatchStatusUpdate = (status: number) => {
  const statusName = status === 1 ? '公开' : '私有'
  Modal.confirm({
    title: `确定要将选中的 ${selectedIds.value.length} 张图片设为${statusName}吗？`,
    async onOk() {
      try {
        await updateImagesStatusApi(selectedIds.value, status)
        message.success(`已成功设为${statusName}`)
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

// --- 右键菜单操作 ---
const handleMenuClick = (action: string, img: API.Image) => {
  switch (action) {
    case 'details':
      showDetails(img)
      break
    case 'move':
      handleMoveToAlbum(img)
      break
    case 'setCover':
      handleSetCover(img)
      break
    case 'delete':
      handleDelete([img.id])
      break
  }
}

const showDetails = (img: API.Image) => {
  Modal.info({
    title: '图片详情',
    width: 400,
    content: h('div', { class: 'space-y-3 pt-4' }, [
      h('div', { class: 'flex justify-between' }, [h('span', '文件名:'), h('span', { class: 'font-medium' }, img.name)]),
      h('div', { class: 'flex justify-between' }, [h('span', '所属相册:'), h('span', albumList.value.find(a => a.id === img.albumId)?.name || '无')]),
      h('div', { class: 'flex justify-between' }, [h('span', '文件大小:'), h('span', formatSize(img.size))]),
      h('div', { class: 'flex justify-between' }, [h('span', '上传时间:'), h('span', img.createTime)]),
      h('div', { class: 'flex justify-between' }, [h('span', '状态:'), h('span', img.status.code === 1 ? '公开' : '私有')]),
      h('div', { class: 'mt-4 border-t pt-4' }, [
        h('img', { src: getImageUrl(img.url), class: 'w-full rounded-lg shadow-sm' })
      ])
    ]),
    okText: '关闭',
  })
}

const handleMoveToAlbum = (img: API.Image) => {
  let selectedAlbum = img.albumId
  Modal.confirm({
    title: '移动图片到相册',
    icon: h(ExclamationCircleOutlined, { style: 'color: #1890ff' }),
    width: 420,
    content: h('div', { class: 'pt-4' }, [
      h('div', { class: 'mb-4 p-3 bg-gray-50 rounded-lg border border-gray-100 flex items-center gap-3' }, [
        h('img', { src: getImageUrl(img.url), class: 'w-12 h-12 object-cover rounded' }),
        h('div', { class: 'flex-1 min-w-0' }, [
          h('p', { class: 'text-xs text-gray-400 mb-0.5' }, '当前图片'),
          h('p', { class: 'text-sm font-medium truncate mb-0' }, img.name)
        ])
      ]),
      h('div', { class: 'space-y-2' }, [
        h('p', { class: 'text-sm font-medium text-gray-700 mb-1' }, '选择目标相册'),
        h(Select, {
          defaultValue: img.albumId,
          class: 'w-full',
          placeholder: '请选择相册',
          onChange: (val: any) => { selectedAlbum = val }
        }, {
          default: () => albumList.value.map(album => 
            h(Select.Option, { value: album.id }, { 
              default: () => h('div', { class: 'flex items-center gap-2' }, [h(FolderOutlined), album.name]) 
            })
          )
        })
      ])
    ]),
    async onOk() {
      if (selectedAlbum === img.albumId) {
        message.info('图片已在当前相册中')
        return
      }
      try {
        await moveImageToAlbumApi(img.id, selectedAlbum)
        const albumName = albumList.value.find(a => a.id === selectedAlbum)?.name || selectedAlbum
        message.success(`已成功移动到 [${albumName}] 相册`)
        loadData(true)
      } catch (err) {}
    },
    okText: '确认移动',
    cancelText: '取消'
  })
}

const handleSetCover = async (img: API.Image) => {
  message.loading({ content: '设置中...', key: 'setCover' })
  try {
    await setAlbumCoverApi(img.id)
    message.success({ content: '已设置为相册封面', key: 'setCover', duration: 2 })
  } catch (err) {}
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <!-- 顶部筛选栏 -->
    <div class="bg-white p-4 rounded-lg shadow-sm flex flex-wrap items-center justify-between gap-4">
      <div class="flex flex-wrap items-center gap-4">
        <a-space>
          <span class="text-gray-500 text-sm">相册:</span>
          <a-select 
            v-model:value="queryParams.albumId as any"
            class="w-48"
            show-search
            option-filter-prop="label"
            placeholder="选择相册"
          >
            <a-select-option :value="null" label="全部相册">全部相册</a-select-option>
            <a-select-option 
              v-for="album in albumList" 
              :key="album.id" 
              :value="album.id"
              :label="album.name"
            >
              {{ album.name }}
            </a-select-option>
          </a-select>
        </a-space>

        <a-space>
          <span class="text-gray-500 text-sm">状态:</span>
          <a-select v-model:value="queryParams.status" class="w-28">
            <a-select-option value="all">全部</a-select-option>
            <a-select-option value="1">公开</a-select-option>
            <a-select-option value="0">私有</a-select-option>
          </a-select>
        </a-space>

        <a-space>
          <span class="text-gray-500 text-sm">排序:</span>
          <a-select v-model:value="queryParams.sort"
            class="w-40">
            <a-select-option value="createTime_desc">时间降序 (最新)</a-select-option>
            <a-select-option value="createTime_asc">时间升序</a-select-option>
            <a-select-option value="size_desc">大小降序</a-select-option>
            <a-select-option value="size_asc">大小升序</a-select-option>
          </a-select>
        </a-space>
      </div>

      <a-space v-if="selectedIds.length > 0">
        <a-dropdown>
          <a-button type="primary" ghost>
            权限设置 ({{ selectedIds.length }})
            <template #icon><div class="i-fa6-solid:shield-halved mr-1" /></template>
          </a-button>
          <template #overlay>
            <a-menu @click="({ key }) => handleBatchStatusUpdate(Number(key))">
              <a-menu-item key="1">
                <template #icon><div class="i-fa6-solid:eye text-green-500" /></template>
                设为公开
              </a-menu-item>
              <a-menu-item key="0">
                <template #icon><div class="i-fa6-solid:eye-slash text-orange-500" /></template>
                设为私有
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-button 
          danger 
          type="primary" 
          @click="handleDelete(selectedIds)"
        >
          批量删除
        </a-button>
      </a-space>
    </div>

    <!-- 图片列表区 -->
    <a-spin :spinning="loading" tip="加载中...">
      <div v-if="displayedImages.length > 0" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-4 min-h-[200px]">
        <a-dropdown 
          v-for="img in displayedImages" 
          :key="img.id"
          :trigger="['contextmenu']"
        >
          <div 
            class="group relative bg-white rounded-lg overflow-hidden border transition-all hover:shadow-md cursor-pointer"
            :class="selectedIds.includes(img.id) ? 'border-blue-500 ring-2 ring-blue-100' : 'border-gray-100'"
          >
            <!-- 复选框 -->
            <div 
              class="absolute top-2 left-2 z-10 w-5 h-5 rounded border flex items-center justify-center cursor-pointer transition-colors"
              :class="selectedIds.includes(img.id) ? 'bg-blue-500 border-blue-500 text-white' : 'bg-white/80 border-gray-300'"
              @click.stop="toggleSelect(img.id)"
            >
              <div v-if="selectedIds.includes(img.id)" class="i-fa6-solid:check text-[10px]" />
            </div>

            <!-- 图片预览 -->
            <div class="aspect-square overflow-hidden bg-gray-50">
              <a-image
                :src="getImageUrl(img.url)"
                class="w-full h-full object-cover transition-transform group-hover:scale-105"
                :preview="true"
              />
            </div>

            <!-- 图片信息 -->
            <div class="p-2 space-y-1">
              <div class="text-sm font-medium truncate text-gray-800" :title="img.name">
                {{ img.name }}
              </div>
              <div class="flex items-center justify-between text-[10px] text-gray-400">
                <span>{{ formatSize(img.size) }}</span>
                <a-tag :color="img.status.code === 1 ? 'green' : 'orange'" size="small" class="m-0 scale-90 origin-right">
                  {{ img.status.code === 1 ? '公开' : '私有' }}
                </a-tag>
              </div>
              <div class="text-[10px] text-gray-400 truncate">
                {{ img.createTime }}
              </div>
            </div>

            <!-- 悬浮操作层 -->
            <div class="absolute inset-0 bg-black/0 group-hover:bg-black/5 pointer-events-none transition-colors" />
          </div>

          <template #overlay>
            <a-menu @click="({ key }) => handleMenuClick(key as string, img)">
              <a-menu-item key="details">
                <template #icon><div class="i-fa6-solid:circle-info" /></template>
                图片详情
              </a-menu-item>
              <a-menu-item key="move">
                <template #icon><div class="i-fa6-solid:folder-open" /></template>
                移动到相册
              </a-menu-item>
              <a-menu-item key="setCover">
                <template #icon><div class="i-fa6-solid:image" /></template>
                设置为相册封面
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item key="delete" danger>
                <template #icon><div class="i-fa6-solid:trash-can" /></template>
                删除图片
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>

      <!-- 空状态 -->
      <a-empty v-else-if="!loading" description="暂无图片" class="my-20" />
    </a-spin>

    <!-- 加载更多 / 底部状态 -->
    <div v-if="displayedImages.length > 0" class="flex flex-col items-center py-8 gap-4">
      <template v-if="hasMore">
        <a-button :loading="loadingMore" @click="handleLoadMore" class="px-8 h-10 rounded-full">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </a-button>
      </template>
      <div v-else class="text-gray-400 text-xs flex items-center gap-2">
        <div class="w-8 h-[1px] bg-gray-200"></div>
        没有更多图片了
        <div class="w-8 h-[1px] bg-gray-200"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.ant-image) {
  width: 100%;
  height: 100%;
}
</style>

<style>
/* 调整图片预览遮罩层的背景颜色，使其更暗 */
.ant-image-preview-mask {
  background-color: rgba(0, 0, 0, 0.85) !important;
}

/* 也可以调整预览容器的背景，确保完全覆盖 */
.ant-image-preview-wrap {
  background-color: rgba(0, 0, 0, 0.1);
}
</style>