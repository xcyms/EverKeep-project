import { message } from 'ant-design-vue'
import axios from 'axios'
import type { API } from '../types'

import router from '../router'
import { useUserStore } from '../store/user'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 5000,
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res: API.Response<any> = response.data
    
    // 业务状态码处理 (假设后端始终返回 HTTP 200)
    if (res.code === 200) {
      return res.data // 成功，返回数据脱壳
    }

    // 处理特定的业务错误码，例如 401 表示未登录或 Token 过期
    if (res.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      // 避免重复跳转
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
        message.error(res.message || '登录已过期，请重新登录')
      }
      return Promise.reject(new Error(res.message || '未登录'))
    }

    // 其他业务错误（如 500, 403, 400 等）
    message.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    // 处理非 200 的网络错误或服务器宕机等异常
    message.error(error.response?.data?.message || error.message || '网络连接异常')
    return Promise.reject(error)
  },
)

export default request
