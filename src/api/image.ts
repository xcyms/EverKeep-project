import type { API } from '../types';
import request from '../utils/request'

/**
 * 上传图片
 * @param formData 包含 file 和 albumId 的表单数据
 */
export const uploadImageApi = (formData: FormData): Promise<API.Response<API.Image>> => {
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
export const deleteImagesApi = (ids: number[]): Promise<API.Response<string>> => {
  return request({
    url: '/image/delete',
    method: 'post',
    data: ids
  })
}