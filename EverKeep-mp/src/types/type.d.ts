// src/types/index.d.ts

export interface UserDTO {
  id?: number
  username?: string
  password?: string
  nickname?: string
  avatar?: string
  email?: string
  phone?: string
  status?: {
    code: number
    desc: string
  }
  createTime?: string
  updateTime?: string
  roles?: string[]
}

export interface SummaryDTO {
  imageCount?: number
  albumCount?: number
  storageUsage?: number
  totalSize?: number
}
