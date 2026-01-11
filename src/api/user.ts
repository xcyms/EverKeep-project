import request from '../utils/request'
/**
 * 用户登录
 */
export const loginApi = (data: any): Promise<any> => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 */
export const registerApi = (data: any): Promise<any> => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 退出登录
 */
export const logoutApi = (): Promise<any> => {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

/**
 * 获取用户信息
 */
export const getUserInfoApi = (): Promise<any> => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}