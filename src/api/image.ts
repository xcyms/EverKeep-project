import request from '../utils/request'

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
export const getMyImagesApi = (params: { current: number; size: number; orders?: [string, boolean][] }, data?: any): Promise<any> => {
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
export const deleteImagesApi = (ids: number[]): Promise<any> => {
  return request({
    url: '/image/delete',
    method: 'post',
    data: ids
  })
}