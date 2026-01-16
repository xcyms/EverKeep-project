import type { API } from '../types'
import request from '../utils/request'

/**
 * 获取首页统计概览数据
 */
export const getStatsSummaryApi = (): Promise<API.StatsSummary> => {
  return request({
    url: '/stats/summary',
    method: 'get'
  })
}