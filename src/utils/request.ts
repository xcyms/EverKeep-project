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
    // 如果 code 不是 200，视为错误
    if (res.code && res.code !== 200) {
      message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data // 直接返回 data 部分
  },
  (error) => {
    const userStore = useUserStore()
    
    if (error.response?.status === 401) {
      userStore.logout()
      router.push('/login')
      message.error('登录已过期，请重新登录')
    } else {
      message.error(error.response?.data?.message || error.message || '网络错误')
    }
    
    return Promise.reject(error)
  },
)

export default request
