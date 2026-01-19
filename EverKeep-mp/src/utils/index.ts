/**
 * 获取当前页面路径
 * @returns 当前页面路径
 */
export function getCurrentPath() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  return currentPage.route || ''
}

/**
 * 生成随机 UUID
 * @returns UUID 字符串
 */
export function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

export function getImageUrl(url: string | undefined) {
  if (!url) return ''
  // 如果是完整的 http 地址则直接返回
  if (url.startsWith('http')) return url
  // 否则拼接公共图片访问前缀
  const baseUrl = import.meta.env.VITE_IMAGE_BASE_URL || ''
  return `${baseUrl}${url}`
}
