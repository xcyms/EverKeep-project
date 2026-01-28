export const DEFAULT_IMAGE = 'https://placehold.co/600x400?text=Image+Not+Found'

export const getImageUrl = (url: string | undefined) => {
  if (!url) return ''
  // 如果是完整的 http 地址则直接返回
  if (url.startsWith('http')) return url
  // 否则拼接公共图片访问前缀
  const baseUrl = import.meta.env.VITE_IMAGE_BASE_URL || ''
  return `${baseUrl}${url}`
}

export const formatSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}