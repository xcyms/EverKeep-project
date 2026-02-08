import type { API } from '../types';
import request from '../utils/request'

/**
 * 获取我的视频分页列表
 */
export const getVideoPageApi = (params: { current: number; size: number; column?: string; asc?: boolean }, data?: any): Promise<API.PageResult<API.Video>> => {
  return request({
    url: '/video/page',
    method: 'post',
    params,
    data
  })
}

/**
 * 获取公开视频分页列表
 */
export const getPublicVideoPageApi = (params: { current: number; size: number; column?: string; asc?: boolean }, data?: any): Promise<API.PageResult<API.Video>> => {
  return request({
    url: '/video/public/page',
    method: 'post',
    params,
    data
  })
}

/**
 * 删除视频 (移至回收站)
 */
export const deleteVideosApi = (ids: number[]): Promise<string> => {
  return request({
    url: '/video/delete',
    method: 'delete',
    data: ids
  })
}

/**
 * 更新视频公开状态
 */
export const updateVideoStatusApi = (ids: number[], status: number): Promise<string> => {
  return request({
    url: '/video/updateStatus',
    method: 'post',
    data: {
      ids,
      status
    }
  })
}

/**
 * 移动视频到相册
 */
export const moveVideoToAlbumApi = (videoId: number, albumId: number): Promise<string> => {
  return request({
    url: '/video/move',
    method: 'post',
    params: { videoId, albumId }
  })
}

/**
 * 批量移动视频到相册
 */
export const batchMoveVideosApi = (ids: number[], albumId: number): Promise<string> => {
  return request({
    url: '/video/batchMove',
    method: 'post',
    data: {
      ids,
      albumId
    }
  })
}

/**
 * 获取回收站视频列表
 */
export const getRecycleVideoPageApi = (params: { current: number; size: number }): Promise<API.PageResult<API.Video>> => {
  return request({
    url: '/video/recycle/page',
    method: 'get',
    params
  })
}

/**
 * 从回收站恢复视频
 */
export const restoreVideosApi = (ids: number[]): Promise<string> => {
  return request({
    url: '/video/restore',
    method: 'post',
    data: ids
  })
}

/**
 * 永久删除视频
 */
export const deleteVideosPermanentlyApi = (ids: number[]): Promise<string> => {
  return request({
    url: '/video/deletePermanently',
    method: 'delete',
    data: ids
  })
}

/**
 * 获取所有视频分页列表 (管理员)
 */
export const getAdminVideoPageApi = (params: { current: number; size: number; column?: string; asc?: boolean }, data?: any): Promise<API.PageResult<API.Video>> => {
  return request({
    url: '/video/admin/page',
    method: 'post',
    params,
    data
  })
}