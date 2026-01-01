<script lang="ts" setup>
import type { MessageItem } from '@/types/page'
import { computed, onMounted, ref } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useManualTheme } from '@/composables/useManualTheme'

definePage({
  name: 'message',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '消息',
    navigationStyle: 'custom',
  },
})

const toast = useToast()
const messageStore = useMessageStore()
const { statusBarHeight } = useSystemInfo()
const { isDark } = useManualTheme()
const activeTab = ref(0)

// 从 store 获取消息数据
const messages = computed(() => {
  if (activeTab.value === 0) {
    return messageStore.messages
  } else if (activeTab.value === 1) {
    return messageStore.messagesByType('system')
  } else {
    return messageStore.messagesByType('activity')
  }
})

const hasUnread = computed(() => messageStore.hasUnread)

// 标记消息为已读
function handleMessageClick(message: MessageItem) {
  if (message.unread) {
    messageStore.markAsRead(message.id)
  }
}

// 标记全部已读
function handleMarkAllRead() {
  messageStore.markAllAsRead()
  toast.success('已标记全部已读')
}

onMounted(() => {
  // 初始化消息数据（如果 store 中没有数据）
  if (messageStore.messages.length === 0) {
    const defaultMessages: MessageItem[] = [
      {
        id: 1,
        type: 'system',
        title: '系统升级通知',
        content: '为了提供更好的服务，系统将于今晚 2:00 进行维护升级，预计耗时 2 小时。',
        time: '10:24',
        unread: true,
        icon: 'notification',
        iconBg: 'bg-blue-50',
        iconColor: 'text-blue-600',
      },
      {
        id: 2,
        type: 'activity',
        title: '新功能上线',
        content: '全新"智能分类"功能已上线，快去相册管理页体验吧！',
        time: '昨天',
        unread: false,
        icon: 'twinkle',
        iconBg: 'bg-purple-50',
        iconColor: 'text-purple-600',
      },
      {
        id: 3,
        type: 'security',
        title: '登录提醒',
        content: '您的账号于 12-30 15:20 在新设备登录，如非本人操作请及时修改密码。',
        time: '前天',
        unread: false,
        icon: 'info-circle',
        iconBg: 'bg-orange-50',
        iconColor: 'text-orange-500',
      },
    ]
    messageStore.setMessages(defaultMessages)
  }

  toast.info('演示消息仅供参考')
})
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa] pb-10 transition-colors duration-300 dark:bg-black">
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
        <div class="h-11 w-11 flex flex-shrink-0 items-center justify-center rounded-xl transition-colors" :class="isDark ? 'bg-gray-800' : item.iconBg">
          <wd-icon :name="item.icon" size="22px" :custom-class="isDark ? '!text-gray-400' : item.iconColor" />
        </div>

        <!-- 消息内容 -->
        <div class="flex-1 overflow-hidden">
          <div class="mb-1 flex items-center justify-between">
            <span class="truncate text-[15px] text-gray-900 font-bold dark:text-gray-100">{{ item.title }}</span>
            <span class="text-[11px] text-gray-400 font-medium dark:text-gray-500">{{ item.time }}</span>
          </div>
          <p class="line-clamp-2 text-xs text-gray-500 leading-relaxed dark:text-gray-400">
            {{ item.content }}
          </p>
        </div>

        <!-- 未读红点 -->
        <div v-if="item.unread" class="absolute right-3 top-3 h-2 w-2 border-2 border-white rounded-full bg-red-500 dark:border-gray-900" />
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
