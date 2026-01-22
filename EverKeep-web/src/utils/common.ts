export const DEFAULT_IMAGE = 'https://placehold.co/600x400?text=Image+Not+Found'

export const getImageUrl = (url: string | undefined) => {
  if (!url) return ''
  // 如果是完整的 http 地址则直接返回
  if (url.startsWith('http')) return url
  // 否则拼接公共图片访问前缀
  const baseUrl = import.meta.env.VITE_IMAGE_BASE_URL || ''
  return `${baseUrl}${url}`
}