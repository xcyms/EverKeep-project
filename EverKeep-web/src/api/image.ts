import type { API } from '../types';
import request from '../utils/request'

/**
 * 上传图片
 * @param formData 包含 file、albumId、category 的表单数据
 */
export const uploadImageApi = (formData: FormData): Promise<API.Image> => {
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取图片列表
 */
export const getMyImagesApi = (params: { current: number; size: number; column?: string; asc?: boolean }, data?: any): Promise<API.PageResult<API.Image>> => {
  return request({
    url: '/image/page',
    method: 'post',
    params,
    data
  })
}

/**
 * 删除图片
 */
export const deleteImagesApi = (ids: number[]): Promise<string> => {
  return request({
    url: '/image/delete',
    method: 'delete',
    data: ids
  })
}

/**
 * 批量更新图片状态
 */
export const updateImagesStatusApi = (ids: number[], status: number): Promise<string> => {
  return request({
    url: '/image/updateStatus',
    method: 'post',
    data: {
      ids,
      status
    }
  })
}

/**
 * 移动图片到指定相册
 */
export const moveImageToAlbumApi = (imageId: number, albumId: number): Promise<string> => {
  return request({
    url: '/image/move',
    method: 'post',
    params: { imageId, albumId }
  })
}

/**
 * 批量移动图片到指定相册
 */
export const batchMoveImagesApi = (ids: number[], albumId: number): Promise<string> => {
  return request({
    url: '/image/batchMove',
    method: 'post',
    data: {
      ids,
      albumId
    }
  })
}

/**
 * 设置图片为所属相册封面
 */
export const setAlbumCoverApi = (imageId: number): Promise<string> => {
  return request({
    url: '/image/setCover',
    method: 'post',
    params: { imageId }
  })
}