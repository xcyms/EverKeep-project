// src/types/page.d.ts

/**
 * 图片数据
 */
export interface ImageItem {
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

/**
 * 相册数据
 */
export interface AlbumItem {
  id: string | number
  userId: string | number
  name: string
  description: string
  cover: string
  imageCount: number
  createTime: string
  updateTime: string
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
  createTime: string
  readFlag: {
    code: number
    desc: string
  }
  userId: string | number
  icon: string
  iconBg: string
  iconColor: string
}
