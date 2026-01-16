<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { getMyMessagesPageApi, readMessageApi, readAllMessagesApi } from '../api/message'
import type { API } from '../types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'unread-change', count: number): void
}>()

const messages = ref<API.Message[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const currentMessage = ref<API.Message | null>(null)

const unreadCount = computed(() => messages.value.filter(m => m.readFlag.code === 0).length)

// 监听未读数量变化并通知父组件
watch(unreadCount, (newCount) => {
  emit('unread-change', newCount)
}, { immediate: true })

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getMyMessagesPageApi({ current: 1, size: 50 })
    messages.value = res.records
  } catch (err) {
    console.error('加载消息失败', err)
  } finally {
    loading.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    loadMessages()
  }
})

onMounted(() => {
  loadMessages()
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleMarkAllRead = async () => {
  try {
    await readAllMessagesApi()
    messages.value.forEach(m => m.readFlag = { code: 1, desc: '已读' })
  } catch (err) {}
}

const handleRead = async (item: API.Message) => {
  currentMessage.value = item
  detailVisible.value = true
  
  if (item.readFlag.code === 1) return
  try {
    await readMessageApi(item.id)
    item.readFlag = { code: 1, desc: '已读' }
  } catch (err) {}
}

const formatTime = (time: string) => {
  return dayjs(time).fromNow()
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

    <a-spin :spinning="loading">
      <a-list item-layout="horizontal" :data-source="messages">
        <template #renderItem="{ item }">
          <a-list-item 
            :class="['cursor-pointer hover:bg-gray-50 transition-colors px-4', { 'opacity-60': item.readFlag.code === 1 }]"
            @click="handleRead(item)"
          >
            <a-list-item-meta :description="item.content">
              <template #title>
                <div class="flex justify-between items-center">
                  <span :class="{ 'font-bold': item.readFlag.code === 0 }">{{ item.title }}</span>
                  <span class="text-xs text-gray-400">{{ formatTime(item.createTime) }}</span>
                </div>
              </template>
              <template #avatar>
                <a-badge :dot="item.readFlag.code === 0">
                  <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-500">
                    <div v-if="item.type === 'success'" class="i-fa6-solid:circle-check" />
                    <div v-else-if="item.type === 'warning'" class="i-fa6-solid:triangle-exclamation" />
                    <div v-else-if="item.type === 'error'" class="i-fa6-solid:circle-xmark" />
                    <div v-else class="i-fa6-solid:bell" />
                  </div>
                </a-badge>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
        <template v-if="messages.length === 0">
          <a-empty description="暂无消息" class="mt-20" />
        </template>
      </a-list>
    </a-spin>
  </a-drawer>

  <a-modal
    v-model:visible="detailVisible"
    :title="currentMessage?.title || '消息详情'"
    :footer="null"
    destroyOnClose
  >
    <div class="py-2">
      <div class="flex items-center gap-2 mb-4 text-xs text-gray-400">
        <a-tag :color="currentMessage?.type === 'info' ? 'blue' : currentMessage?.type === 'success' ? 'green' : currentMessage?.type === 'warning' ? 'orange' : 'red'">
          {{ currentMessage?.type.toUpperCase() }}
        </a-tag>
        <span>{{ currentMessage?.createTime }}</span>
      </div>
      <div class="text-gray-700 leading-relaxed whitespace-pre-wrap">
        {{ currentMessage?.content }}
      </div>
    </div>
  </a-modal>
</template>

<style scoped>
:deep(.ant-list-item) {
  padding: 12px 0;
}
</style>