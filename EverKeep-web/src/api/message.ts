import type { API } from '../types'
import request from '../utils/request'

/** 分页获取消息 (当前用户) */
export const getMyMessagesPageApi = (params: API.GetMessagePageRequest): Promise<API.PageResult<API.Message>> => {
  return request({ url: '/message/my/page', method: 'get', params })
}

/** 标记消息已读 */
export const readMessageApi = (id: number): Promise<any> => {
  return request({ url: '/message/read', method: 'post', params: { id } })
}

/** 全部标记为已读 */
export const readAllMessagesApi = (): Promise<any> => {
  return request({ url: '/message/readAll', method: 'post' })
}

/** 分页获取所有消息 (管理员) */
export const getMessagePageApi = (params: API.GetMessagePageRequest): Promise<API.PageResult<API.Message>> => {
  return request({ url: '/message/page', method: 'get', params })
}

/** 新增消息 (管理员) */
export const saveMessageApi = (data: Partial<API.Message>): Promise<any> => {
  return request({ url: '/message/save', method: 'post', data })
}

/** 修改消息 (管理员) */
export const updateMessageApi = (data: API.Message): Promise<any> => {
  return request({ url: '/message/update', method: 'post', data })
}

/** 删除消息 (管理员) */
export const deleteMessageApi = (id: number): Promise<any> => {
  return request({ url: '/message/delete', method: 'delete', params: { id } })
}