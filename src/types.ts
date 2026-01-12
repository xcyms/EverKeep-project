namespace API {
  export interface Response<T> {
    code: number
    message: string
    data: T
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
}

export type { API }