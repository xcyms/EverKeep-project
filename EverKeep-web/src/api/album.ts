import type { API } from '../types';
import request from '../utils/request'

/**
 * 获取我的相册列表
 */
export const getMyAlbumsApi = (): Promise<API.Album[]> => {
  return request({
    url: '/album/list',
    method: 'get'
  })
}

/**
 * 获取我的相册列表
 */
export const getMyAlbumsPageApi = (params: { current: number; size: number; column?: string; asc?: boolean }, data?: any): Promise<API.PageResult<API.Album>> => {
  return request({
    url: '/album/page',
    method: 'post',
    params,
    data
  })
}

/**
 * 创建相册
 */
export const createAlbumApi = (data: API.Album): Promise<string> => {
  return request({
    url: '/album/create',
    method: 'post',
    data
  })
}

/**
 * 修改相册
 */
export const updateAlbumApi = (data: API.Album): Promise<string> => {
  return request({
    url: '/album/update',
    method: 'post',
    data
  })
}

/**
 * 删除相册
 */
export const deleteAlbumApi = (id: number): Promise<string> => {
  return request({
    url: '/album/delete',
    method: 'delete',
    params: { id }
  })
}