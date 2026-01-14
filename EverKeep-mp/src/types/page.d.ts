// src/types/page.d.ts

/**
 * 图片数据
 */
export interface ImageItem {
  key: string
  links: {
    url: string
  }
  name?: string
  size?: number
  width?: number
  height?: number
  _error?: boolean
}

/**
 * 相册数据
 */
export interface AlbumItem {
  id: string | number
  name: string
  intro?: string
  cover: string
  image_num: number
  created_at?: string
  updated_at?: string
  _error?: boolean
}

/**
 * 消息数据
 */
export interface MessageItem {
  id: string | number
  type: 'system' | 'activity' | 'security'
  title: string
  content: string
  time: string
  unread: boolean
  icon: string
  iconBg: string
  iconColor: string
}
