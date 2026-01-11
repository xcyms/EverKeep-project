import request from '../utils/request'
import type { API } from '../types'

/**
 * 上传图片
 * @param formData 包含 file 和 albumId 的表单数据
 */
export const uploadImageApi = (formData: FormData): Promise<any> => {
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
export const getMyImagesApi = (params?: any): Promise<any> => {
  return request({
    url: '/image/list',
    method: 'get',
    params
  })
}