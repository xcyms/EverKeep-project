<script setup lang="ts">
import { ref, computed, watch } from 'vue'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'unread-change', count: number): void
}>()

const messages = ref([
  { id: 1, title: '系统通知', content: '您的账号于 2026-01-11 10:00:00 登录成功。', time: '10分钟前', read: false },
  { id: 2, title: '任务提醒', content: '您有一个待处理的上传任务。', time: '1小时前', read: false },
  { id: 3, title: '新消息', content: '欢迎使用 EverKeep！', time: '1天前', read: false },
  { id: 4, title: '版本更新', content: '系统已更新至 v1.2.0。', time: '2天前', read: true },
])

const unreadCount = computed(() => messages.value.filter(m => !m.read).length)

// 监听未读数量变化并通知父组件
watch(unreadCount, (newCount) => {
  emit('unread-change', newCount)
}, { immediate: true })

const handleClose = () => {
  emit('update:visible', false)
}

const handleMarkAllRead = () => {
  messages.value.forEach(m => m.read = true)
}
</script>

<template>
  <a-drawer
    :visible="visible"
    title="消息中心"
    placement="right"
    :width="350"
    @close="handleClose"
  >
    <template #extra>
      <a-button type="link" @click="handleMarkAllRead" :disabled="unreadCount === 0">
        全部已读
      </a-button>
    </template>

    <a-list item-layout="horizontal" :data-source="messages">
      <template #renderItem="{ item }">
        <a-list-item :class="{ 'opacity-60': item.read }">
          <a-list-item-meta :description="item.content">
            <template #title>
              <div class="flex justify-between items-center">
                <span :class="{ 'font-bold': !item.read }">{{ item.title }}</span>
                <span class="text-xs text-gray-400">{{ item.time }}</span>
              </div>
            </template>
            <template #avatar>
              <a-badge :dot="!item.read">
                <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-500">
                  <div v-if="item.id === 1" class="i-fa6-solid:circle-info" />
                  <div v-else-if="item.id === 2" class="i-fa6-solid:list-check" />
                  <div v-else-if="item.id === 3" class="i-fa6-solid:message" />
                  <div v-else class="i-fa6-solid:bell" />
                </div>
              </a-badge>
            </template>
          </a-list-item-meta>
        </a-list-item>
      </template>
      <template v-if="messages.length === 0">
        <a-empty description="暂无消息" />
      </template>
    </a-list>
  </a-drawer>
</template>

<style scoped>
:deep(.ant-list-item) {
  padding: 12px 0;
}
</style>