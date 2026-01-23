<script lang="ts" setup>
interface Props {
  icon?: string
  iconSize?: string
  title?: string
  description?: string
  actionText?: string
  actionType?: 'primary' | 'default'
}

interface Emits {
  (e: 'action'): void
}

const _props = withDefaults(defineProps<Props>(), {
  icon: 'search',
  iconSize: '32px',
  title: '暂无数据',
  description: '',
  actionText: '',
  actionType: 'primary',
})

const emit = defineEmits<Emits>()
</script>

<template>
  <div class="flex flex-col items-center justify-center gap-2 py-20">
    <div class="mb-4 h-20 w-20 flex items-center justify-center rounded-full bg-gray-100">
      <wd-icon :name="icon" :size="iconSize" color="#cbd5e1" />
    </div>
    <span class="text-sm text-gray-900 font-medium">{{ title }}</span>
    <span v-if="description" class="mt-1 text-xs text-gray-400">{{ description }}</span>

    <wd-button
      v-if="actionText"
      :type="actionType"
      class="mt-6"
      @click="emit('action')"
    >
      {{ actionText }}
    </wd-button>
  </div>
</template>
