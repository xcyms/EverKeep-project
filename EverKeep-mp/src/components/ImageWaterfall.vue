<script lang="ts" setup>
import { computed } from 'vue'

interface ImageItem {
  albumId: string | number
  createTime: string
  id: string | number
  name?: string
  size?: number
  status: {
    code: number
    desc: string
  }
  type: string
  url: string
  userId: string | number
}

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
          :key="img.id || index"
          class="overflow-hidden rounded-xl bg-white shadow-[0_4px_16px_rgba(0,0,0,0.04)] transition-opacity active:opacity-80"
          @tap="handleImageTap(img.url)"
        >
          <image
            :src="getImageUrl(img.url)"
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
          :key="img.id || index"
          class="overflow-hidden rounded-xl bg-white shadow-[0_4px_16px_rgba(0,0,0,0.04)] transition-opacity active:opacity-80"
          @tap="handleImageTap(img.url)"
        >
          <image
            :src="getImageUrl(img.url)"
            mode="widthFix"
            class="fade-in block h-auto w-full"
            lazy-load
          />
        </div>
      </div>
    </div>

    <!-- 缺省展示 -->
    <div v-else-if="!loading && list.length === 0" class="fade-in flex flex-col items-center justify-center py-20">
      <div class="mb-4 h-20 w-20 flex items-center justify-center rounded-full bg-gray-50/50">
        <wd-icon name="picture" size="72px" color="#cbd5e1" />
      </div>
      <span class="text-sm text-gray-400 font-medium">{{ emptyText || '暂无数据' }}</span>
    </div>
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
