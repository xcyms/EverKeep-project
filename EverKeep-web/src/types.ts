namespace API {
  export interface Response<T> {
    code: number
    message: string
    data: T
  }

  export interface LoginRequest {
    username: string
    password: string
  }

  export interface RegisterRequest {
    username: string
    password: string
    confirmPassword: string
  }

  export interface SysConfig {
    userId: number | undefined
    id?: number
    configKey: string
    configValue: string
    configName: string
    remark?: string
  }

  export interface UploadFile {
    id: string
    file: File
    name: string
    size: number
    progress: number
    status: 'pending' | 'uploading' | 'success' | 'error'
    url?: string
    preview?: string
  }

  export interface User {
    id: number
    username: string
    nickname: string
    avatar?: string
    email?: string
    phone?: string
    roles: string[]
  }

  export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
  }

  export interface Album {
    id: number
    name: string
    cover: string
    imageCount: number
    createTime: string
    description: string
  }

  export interface Image {
    id: number
    userId: number
    albumId: number
    url: string
    thumbnailUrl?: string
    name: string
    size: number
    type: string
    status: {
      code: number
      desc: string
    }
    createTime: string
    make?: string
    model?: string
    exposureTime?: string
    fNumber?: string
    iso?: string
    focalLength?: string
    lensModel?: string
    lat?: string
    lng?: string
    takeTime?: string
  }

  export interface StatsSummary {
    uploadTrend: {
      date: string
      count: number
    }[]
    imageCount: number
    albumCount: number
    userCount: number
    storageUsage: number
    totalSize: number
  }

  export interface Notice {
    id: number
    title: string
    content: string
    tag: string
    color: string
    sort: number
    createTime: string
  }

  export interface GetNoticePageRequest {
    current: number
    size: number
    title?: string
  }

  export interface Message {
    id: number
    title: string
    content: string
    type: 'info' | 'success' | 'warning' | 'error'
    readFlag: {
      code: 0 | 1
      desc: string
    }
    createTime: string
  }

  export interface GetMessagePageRequest {
    current: number
    size: number
    title?: string
  }
}

export type { API }