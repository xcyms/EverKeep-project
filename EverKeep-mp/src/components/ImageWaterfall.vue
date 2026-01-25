<script lang="ts" setup>
import type { ImageItem } from '@/types/page';
import { computed } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'

const props = defineProps<{
  list: ImageItem[]
  loading?: boolean
  emptyText?: string
}>()

const emit = defineEmits(['click'])

// 内部自动分列，简化父组件逻辑
const leftCol = computed(() => props.list.filter((_, i) => i % 2 === 0))
const rightCol = computed(() => props.list.filter((_, i) => i % 2 !== 0))

function handleImageTap(currentUrl: string) {
  const urls = props.list.map(item => getImageUrl(item.url))
  uni.previewImage({
    urls,
    current: currentUrl,
  })
  emit('click', currentUrl)
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
          class="overflow-hidden rounded-xl bg-white shadow-[0_4px_16px_rgba(0,0,0,0.04)] transition-opacity active:opacity-80"
          @tap="handleImageTap(img.url)"
        >
          <image
            :src="getImageUrl(img.thumbnailUrl || img.url)"
            mode="widthFix"
            class="fade-in block h-auto w-full"
            lazy-load
          />
        </div>
      </div>
      <!-- 右列 -->
      <div class="flex flex-1 flex-col gap-3">
        <div
          v-for="(img, index) in rightCol"
          :key="`${img.id}-${index}`"
          class="overflow-hidden rounded-xl bg-white shadow-[0_4px_16px_rgba(0,0,0,0.04)] transition-opacity active:opacity-80"
          @tap="handleImageTap(img.url)"
        >
          <image
            :src="getImageUrl(img.thumbnailUrl || img.url)"
            mode="widthFix"
            class="fade-in block h-auto w-full"
            lazy-load
          />
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
