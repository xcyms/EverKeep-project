<script lang="ts" setup>
import type { ImageItem } from '@/types/page';
import { computed } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'

const props = defineProps<{
  list: ImageItem[]
  loading?: boolean
  emptyText?: string
  isSelectionMode?: boolean
  selectedIds?: Set<string | number>
}>()

const emit = defineEmits(['click', 'selection-change', 'update:selectedIds'])

// 内部自动分列，简化父组件逻辑
const leftCol = computed(() => props.list.filter((_, i) => i % 2 === 0))
const rightCol = computed(() => props.list.filter((_, i) => i % 2 !== 0))

function handleImageTap(img: ImageItem) {
  if (props.isSelectionMode) {
    toggleSelection(img.id)
    return
  }
  const urls = props.list.map(item => getImageUrl(item.url))
  uni.previewImage({
    urls,
    current: getImageUrl(img.url),
  })
  emit('click', img.url)
}

function toggleSelection(id: string | number) {
  const newSelectedIds = new Set(props.selectedIds)
  if (newSelectedIds.has(id)) {
    newSelectedIds.delete(id)
  } else {
    newSelectedIds.add(id)
  }
  emit('update:selectedIds', newSelectedIds)
  emit('selection-change', Array.from(newSelectedIds))
}
</script>

<template>
  <div class="image-waterfall">
    <div v-if="list.length > 0" class="flex flex-row items-start gap-3">
      <!-- ... (保持原有瀑布流逻辑) ... -->
      <!-- 左列 -->
      <div class="flex flex-1 flex-col gap-3">
        <div
          v-for="(img, index) in leftCol"
          :key="`${img.id}-${index}`"
          class="relative overflow-hidden rounded-xl bg-white shadow-[0_4px_16px_rgba(0,0,0,0.04)] transition-all active:opacity-80"
          :class="{ 'scale-[0.98] ring-2 ring-primary': isSelectionMode && selectedIds?.has(img.id) }"
          @tap="handleImageTap(img)"
        >
          <image
            :src="getImageUrl(img.thumbnailUrl || img.url)"
            mode="widthFix"
            class="fade-in block h-auto w-full transition-opacity"
            :class="{ 'opacity-60': isSelectionMode && selectedIds?.has(img.id) }"
            lazy-load
          />
          <!-- 状态标签 -->
          <div
            v-if="isSelectionMode"
            class="absolute left-2 top-2 z-10 flex items-center gap-1 rounded-full bg-black/40 px-2 py-0.5 text-[10px] text-white backdrop-blur-md"
          >
            <div :class="img.status?.code === 1 ? 'i-solar-earth-bold text-blue-400' : 'i-solar-lock-keyhole-bold text-gray-300'" />
            <span>{{ img.status?.code === 1 ? '公开' : '私有' }}</span>
          </div>
          <!-- 选中状态指示器 -->
          <div
            v-if="isSelectionMode"
            class="absolute right-2 top-2 h-6 w-6 flex items-center justify-center border-2 rounded-full transition-all"
            :class="selectedIds?.has(img.id) ? 'bg-primary border-primary shadow-lg scale-110' : 'bg-black/20 border-white/60 backdrop-blur-md'"
          >
            <div v-if="selectedIds?.has(img.id)" class="i-solar-check-read-bold text-xs text-red" />
          </div>
        </div>
      </div>
      <!-- 右列 -->
      <div class="flex flex-1 flex-col gap-3">
        <div
          v-for="(img, index) in rightCol"
          :key="`${img.id}-${index}`"
          class="relative overflow-hidden rounded-xl bg-white shadow-[0_4px_16px_rgba(0,0,0,0.04)] transition-all active:opacity-80"
          :class="{ 'scale-[0.98] ring-2 ring-primary': isSelectionMode && selectedIds?.has(img.id) }"
          @tap="handleImageTap(img)"
        >
          <image
            :src="getImageUrl(img.thumbnailUrl || img.url)"
            mode="widthFix"
            class="fade-in block h-auto w-full transition-opacity"
            :class="{ 'opacity-60': isSelectionMode && selectedIds?.has(img.id) }"
            lazy-load
          />
          <!-- 状态标签 -->
          <div
            v-if="isSelectionMode"
            class="absolute left-2 top-2 z-10 flex items-center gap-1 rounded-full bg-black/40 px-2 py-0.5 text-[10px] text-white backdrop-blur-md"
          >
            <div :class="img.status?.code === 1 ? 'i-solar-earth-bold text-blue-400' : 'i-solar-lock-keyhole-bold text-gray-300'" />
            <span>{{ img.status?.code === 1 ? '公开' : '私有' }}</span>
          </div>
          <!-- 选中状态指示器 -->
          <div
            v-if="isSelectionMode"
            class="absolute right-2 top-2 h-6 w-6 flex items-center justify-center border-2 rounded-full transition-all"
            :class="selectedIds?.has(img.id) ? 'bg-primary border-primary shadow-lg scale-110' : 'bg-black/20 border-white/60 backdrop-blur-md'"
          >
            <div v-if="selectedIds?.has(img.id)" class="i-solar-check-read-bold text-xs text-red" />
          </div>
        </div>
      </div>
    </div>

    <!-- 缺省展示 -->
    <!-- Empty State -->
    <EmptyState
      v-else-if="!loading && list.length === 0"
      icon="image"
      :title="emptyText || '暂无数据'"
      description="这里空空如也，快去上传或探索吧"
      class="py-12"
    />
  </div>
</template>

<style lang="scss" scoped>
.fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.39, 0.575, 0.565, 1) both;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
