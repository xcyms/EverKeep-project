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

export const formatDuration = (seconds: number | undefined) => {
  if (!seconds) return '00:00'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = Math.floor(seconds % 60)
  
  const parts = []
  if (h > 0) parts.push(h.toString().padStart(2, '0'))
  parts.push(m.toString().padStart(2, '0'))
  parts.push(s.toString().padStart(2, '0'))
  
  return parts.join(':')
}