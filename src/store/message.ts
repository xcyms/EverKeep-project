import type { MessageItem } from '@/types/page'
import { defineStore } from 'pinia'

export const useMessageStore = defineStore('message', {
  state: () => ({
    messages: [] as MessageItem[],
    unreadCount: 0,
  }),

  getters: {
    /**
     * 获取未读消息
     */
    unreadMessages: (state) => state.messages.filter(m => m.unread),

    /**
     * 根据类型获取消息
     */
    messagesByType: (state) => (type: string) =>
      state.messages.filter(m => m.type === type),

    /**
     * 是否有未读消息
     */
    hasUnread: (state) => state.unreadCount > 0,
  },

  actions: {
    /**
     * 设置消息列表
     */
    setMessages(messages: MessageItem[]) {
      this.messages = messages
      this.updateUnreadCount()
      // 手动保存到本地存储
      this.saveToStorage()
    },

    /**
     * 添加消息
     */
    addMessage(message: MessageItem) {
      this.messages.unshift(message)
      this.updateUnreadCount()
      this.saveToStorage()
    },

    /**
     * 标记为已读
     */
    markAsRead(id: string | number) {
      const message = this.messages.find(m => m.id === id)
      if (message && message.unread) {
        message.unread = false
        this.updateUnreadCount()
        this.saveToStorage()
      }
    },

    /**
     * 标记全部已读
     */
    markAllAsRead() {
      this.messages.forEach(m => {
        m.unread = false
      })
      this.updateUnreadCount()
      this.saveToStorage()
    },

    /**
     * 更新未读数量
     */
    updateUnreadCount() {
      this.unreadCount = this.messages.filter(m => m.unread).length
    },

    /**
     * 清空消息
     */
    clearMessages() {
      this.messages = []
      this.unreadCount = 0
      this.saveToStorage()
    },

    /**
     * 保存到本地存储
     */
    saveToStorage() {
      try {
        if (typeof uni !== 'undefined') {
          uni.setStorageSync('messages', {
            messages: this.messages,
            unreadCount: this.unreadCount,
          })
        }
      } catch (e) {
        console.error('Save messages to storage error:', e)
      }
    },

    /**
     * 从本地存储加载
     */
    loadFromStorage() {
      try {
        if (typeof uni !== 'undefined') {
          const data = uni.getStorageSync('messages')
          if (data) {
            this.messages = data.messages || []
            this.unreadCount = data.unreadCount || 0
          }
        }
      } catch (e) {
        console.error('Load messages from storage error:', e)
      }
    },
  },
})
