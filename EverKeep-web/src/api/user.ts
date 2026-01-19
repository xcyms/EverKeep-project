import type { API } from '../types'
import request from '../utils/request'
/**
 * 用户登录
 */
export const loginApi = (data: API.LoginRequest): Promise<string> => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 */
export const registerApi = (data: API.RegisterRequest): Promise<string> => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 退出登录
 */
export const logoutApi = (): Promise<string> => {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

/**
 * 获取用户信息
 */
export const getUserInfoApi = (): Promise<API.User> => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 更新个人资料
 */
export const updateProfileApi = (data: Partial<API.User>): Promise<API.User> => {
  return request({
    url: '/user/update',
    method: 'post',
    data
  })
}

/**
 * 修改密码
 */
export const updatePasswordApi = (data: { oldPassword: string; password: string }): Promise<string> => {
  return request({
    url: '/user/password',
    method: 'post',
    data
  })
}

/**
 * 获取用户列表 (管理员)
 */
export const getUserListApi = (
  params: { current: number; size: number },
  data?: { username?: string; nickname?: string }
): Promise<API.PageResult<API.User>> => {
  return request({
    url: '/user/page',
    method: 'post',
    params,
    data
  })
}