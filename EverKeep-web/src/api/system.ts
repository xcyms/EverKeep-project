import request from '../utils/request'

/**
 * 获取所有后端接口列表
 */
export interface FieldDoc {
  field: string
  type: string
  description: string
  required: boolean
  example: string
  children?: FieldDoc[]
}

export interface EndpointParam {
  name: string
  type: string
  source: string
  schema?: FieldDoc[]
}

export interface Endpoint {
  name: string
  methodName: string
  controller: string
  path: string[]
  methods: string[]
  description: string
  order: number
  params: EndpointParam[]
  response: FieldDoc[]
}

export const getEndpointsApi = (): Promise<Endpoint[]> => {
  return request({
    url: '/system/endpoints',
    method: 'get'
  })
}