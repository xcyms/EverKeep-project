import request from '../utils/request'

/**
 * 获取所有后端接口列表
 */
export interface EndpointParam {
  name: string
  type: string
  source: string
}

export interface Endpoint {
  name: string
  controller: string
  path: string[]
  methods: string[]
  description: string
  params: EndpointParam[]
  returnType: string
}

export const getEndpointsApi = (): Promise<Endpoint[]> => {
  return request({
    url: '/system/endpoints',
    method: 'get'
  })
}