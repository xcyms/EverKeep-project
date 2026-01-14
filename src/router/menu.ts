export interface MenuItem {
  key?: string
  label: string
  icon?: string
  type?: 'group'
  hidden?: boolean
  roles?: string[]
  children?: MenuItem[]
}

export const menuItems: MenuItem[] = [
  {
    label: '首页',
    type: 'group',
    children: [
      { key: '/', label: '工作台', icon: 'i-fa6-solid:house' },
    ],
  },
  {
    label: '我的',
    type: 'group',
    children: [
      { key: '/upload', label: '上传图片', icon: 'i-fa6-solid:cloud-arrow-up' },
      { key: '/images', label: '我的图片', icon: 'i-fa6-solid:image' },
      { key: '/albums', label: '我的相册', icon: 'i-fa6-solid:folder' },
    ],
  },
  {
    label: '配置',
    type: 'group',
    roles: ['ADMIN'],
    children: [
      { key: '/users', label: '用户管理', icon: 'i-fa6-solid:users' },
      { key: '/settings', label: '系统设置', icon: 'i-fa6-solid:wrench' },
    ],
  },
  {
    key: '/profile',
    label: '个人中心',
    hidden: true
  }
]