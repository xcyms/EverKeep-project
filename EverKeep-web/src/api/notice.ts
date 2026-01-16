import type { API } from '../types'
import request from '../utils/request'

/**
 * 获取系统公告列表 (首页简版)
 */
export const getNoticeListApi = (): Promise<API.Notice[]> => {
  return request({
    url: '/notice/list',
    method: 'get'
  })
}

/**
 * 分页获取系统公告
 */
export const getNoticePageApi = (params: API.GetNoticePageRequest): Promise<API.PageResult<API.Notice>> => {
  return request({
    url: '/notice/page',
    method: 'get',
    params
  })
}

/**
 * 新增公告
 */
export const saveNoticeApi = (data: Partial<API.Notice>): Promise<any> => {
  return request({
    url: '/notice/save',
    method: 'post',
    data
  })
}

/**
 * 修改公告
 */
export const updateNoticeApi = (data: API.Notice): Promise<any> => {
  return request({
    url: '/notice/update',
    method: 'post',
    data
  })
}

/**
 * 删除公告
 */
export const deleteNoticeApi = (id: number): Promise<any> => {
  return request({
    url: '/notice/delete',
    method: 'delete',
    params: { id }
  })
}