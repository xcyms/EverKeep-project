import request from '../utils/request'

/**
 * 获取我的相册列表
 */
export const getMyAlbumsApi = (): Promise<any> => {
  return request({
    url: '/album/list',
    method: 'get'
  })
}

/**
 * 创建相册
 */
export const createAlbumApi = (data: any): Promise<any> => {
  return request({
    url: '/album/create',
    method: 'post',
    data
  })
}