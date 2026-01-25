<script lang="ts" setup>
import type { MessageItem } from '@/types/page'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import { onMounted, ref } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useManualTheme } from '@/composables/useManualTheme'
import { PAGINATION } from '@/utils/constants'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

definePage({
  name: 'message',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '消息',
    navigationStyle: 'custom',
    enablePullDownRefresh: true,
  },
})

const toast = useToast()
const userStore = useAuthStore()
const messageStore = useMessageStore()
const { statusBarHeight } = useSystemInfo()
const { isDark } = useManualTheme()
const activeTab = ref(0)
const loading = ref(false)

// 预定义的图标和颜色组合
const iconStyles = [
  { icon: 'notification', bg: 'bg-blue-50', color: 'text-blue-600' },
  { icon: 'spool', bg: 'bg-purple-50', color: 'text-purple-600' },
  { icon: 'info-circle', bg: 'bg-orange-50', color: 'text-orange-500' },
  { icon: 'chat', bg: 'bg-green-50', color: 'text-green-600' },
  { icon: 'queue', bg: 'bg-red-50', color: 'text-red-600' },
  { icon: 'star', bg: 'bg-yellow-50', color: 'text-yellow-600' },
]

// 获取消息图标样式
function getMessageStyle(item: MessageItem) {
  // 使用 id 进行哈希计算，确保随机分布且同一 ID 样式固定
  const idStr = String(item.id)
  let hash = 0
  for (let i = 0; i < idStr.length; i++) {
    hash = (hash << 5) - hash + idStr.charCodeAt(i)
    hash |= 0 // Convert to 32bit integer
  }
  const index = Math.abs(hash) % iconStyles.length
  return iconStyles[index]
}

// 从 store 获取消息数据
const messages = computed(() => {
  const list = messageStore.messages
  if (activeTab.value === 1) {
    // 系统消息：userId 为空（null 或 undefined）
    return list.filter(m => m.userId === null || m.userId === undefined || m.userId === '')
  } else if (activeTab.value === 2) {
    // 活动消息：userId 不等于 null
    return list.filter(m => m.userId !== null && m.userId !== undefined && m.userId !== '')
  }
  return list
})

async function fetchMessages() {
  if (!userStore.isLoggedIn) {
    messageStore.setMessages([])
    uni.stopPullDownRefresh()
    return
  }

  loading.value = true
  try {
    const res = await Apis.everkeep.getMessage({
      params: {
        current: 1,
        size: PAGINATION.MAX_PAGE_SIZE, // 消息通常不需要太复杂的分页，直接获取较多数量
      },
    })
    if (res.code === 200 && res.data) {
      messageStore.setMessages(res.data.records || [])
    }
  } catch (e) {
    console.error('Failed to fetch messages:', e)
  } finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

onPullDownRefresh(() => {
  fetchMessages()
})

function formatTime (time: string) {
  return dayjs(time).fromNow()
}

const hasUnread = computed(() => messageStore.hasUnread)

// 标记消息为已读
function handleMessageClick(message: MessageItem) {
  if (message.readFlag.code === 0) {
    messageStore.markAsRead(message.id)
  }
}

// 标记全部已读
function handleMarkAllRead() {
  messageStore.markAllAsRead()
  toast.success('已标记全部已读')
}

// 监听登录状态变化
watch(() => userStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn) {
    fetchMessages()
  } else {
    messageStore.clearMessages()
  }
})

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchMessages()
  }
})
</script>

<template>
  <div class="bg-[#f8f9fa] pb-10 transition-colors duration-300 dark:bg-black">
    <!-- 沉浸式毛玻璃顶栏 -->
    <div
      class="sticky top-0 z-50 w-full border-b border-white/20 bg-white/70 backdrop-blur-xl transition-all dark:border-white/5 dark:bg-black/60"
      :style="{ paddingTop: `${statusBarHeight}px` }"
    >
      <div class="h-12 flex items-center px-5">
        <span class="text-lg text-gray-900 font-bold tracking-tight dark:text-gray-100">消息中心</span>
        <div v-if="hasUnread" class="ml-2 h-2 w-2 rounded-full bg-red-500" />
      </div>

      <!-- 分类切换 -->
      <div class="px-2 pb-1">
        <wd-tabs
          v-model="activeTab"
          :border="false"
          custom-class="!bg-transparent"
          nav-class="!bg-transparent"
          line-height="2px"
          line-width="16px"
        >
          <wd-tab title="全部" :value="0" />
          <wd-tab title="系统" :value="1" />
          <wd-tab title="活动" :value="2" />
        </wd-tabs>
      </div>
    </div>

    <!-- 消息列表 -->
    <div v-if="messages.length > 0" class="p-4 space-y-3">
      <div
        v-for="item in messages"
        :key="item.id"
        class="relative flex gap-4 overflow-hidden rounded-2xl bg-white p-4 shadow-[0_4px_20px_rgb(0,0,0,0.02)] transition-all active:scale-[0.98] active:bg-gray-50 dark:bg-gray-900 dark:shadow-none dark:active:bg-gray-800"
        @tap="handleMessageClick(item)"
      >
        <!-- 消息图标 -->
        <div
          class="h-11 w-11 flex flex-shrink-0 items-center justify-center rounded-xl transition-colors"
          :class="isDark ? 'bg-gray-800' : (item.iconBg || getMessageStyle(item).bg)"
        >
          <wd-icon
            :name="item.icon || getMessageStyle(item).icon"
            size="22px"
            :custom-class="isDark ? '!text-gray-400' : (item.iconColor || getMessageStyle(item).color)"
          />
        </div>

        <!-- 消息内容 -->
        <div class="flex-1 overflow-hidden">
          <div class="mb-1 flex items-center justify-between">
            <span class="truncate text-[15px] text-gray-900 font-bold dark:text-gray-100">{{ item.title }}</span>
            <span class="text-[11px] text-gray-400 font-medium dark:text-gray-500">{{ formatTime(item.createTime) }}</span>
          </div>
          <p class="line-clamp-2 text-xs text-gray-500 leading-relaxed dark:text-gray-400">
            {{ item.content }}
          </p>
        </div>

        <!-- 未读红点 -->
        <div v-if="item.readFlag.code === 0" class="absolute right-3 top-3 h-2 w-2 border-2 border-white rounded-full bg-red-500 dark:border-gray-900" />
      </div>
    </div>

    <!-- 空状态 -->
    <EmptyState v-else icon="chat" title="暂无新消息" description="您的消息将会显示在这里" />

    <!-- 一键已读 -->
    <div v-if="hasUnread" class="fixed bottom-24 left-1/2 z-40 -translate-x-1/2">
      <div
        class="flex items-center gap-2 rounded-full bg-gray-900/90 px-5 py-2.5 text-xs text-white font-bold shadow-xl backdrop-blur-md transition-all active:scale-95 dark:bg-gray-800/90"
        @tap="handleMarkAllRead"
      >
        <wd-icon name="check-circle" size="14px" />
        <span>忽略全部未读</span>
      </div>
    </div>
  </div>
</template>
