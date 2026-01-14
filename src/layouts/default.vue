<script setup lang="ts">
import { useUserStore } from '../store/user'
import { menuItems, type MenuItem } from '../router/menu'
import { logoutApi, getUserInfoApi } from '../api/user'
import { getImageUrl } from '../utils/common'

const showFooter = ref(true)
const collapsed = ref(false)

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 过滤后的菜单
const filteredMenuItems = computed(() => {
  const userRoles = userStore.userInfo.roles || []
  
  const filter = (items: MenuItem[]): MenuItem[] => {
    return items
      .filter(item => {
        // 如果没有设置 roles，默认所有人可见
        if (!item.roles || item.roles.length === 0) return true
        // 如果设置了 roles，检查用户是否有其中之一
        return item.roles.some(role => userRoles.includes(role))
      })
      .map(item => ({
        ...item,
        children: item.children ? filter(item.children) : undefined
      }))
  }
  
  return filter(menuItems)
})

// 消息盒状态
const messageBoxVisible = ref(false)
const unreadCount = ref(0)

const handleBellClick = () => {
  messageBoxVisible.value = true
}

const handleUnreadChange = (count: number) => {
  unreadCount.value = count
}

// 菜单状态
const selectedKeys = ref<string[]>([route.path])
const openKeys = ref<string[]>([])

// 动态面包屑：尝试从菜单项中匹配标签
const breadcrumbs = computed(() => {
  const result: { path: string, label: string }[] = [{ path: '/', label: '首页' }]
  
  if (route.path === '/') return result

  // 递归查找菜单项
  const findLabel = (path: string, items: MenuItem[]): string | null => {
    for (const item of items) {
      if (item.key === path) return item.label
      if (item.children) {
        const found = findLabel(path, item.children)
        if (found) return found
      }
    }
    return null
  }

  const label = findLabel(route.path, filteredMenuItems.value)
  if (label) {
    result.push({ path: route.path, label })
  } else {
    // 如果菜单里没找到，尝试从路由元信息或路径名转换
    const lastMatched = route.matched[route.matched.length - 1]
    const fallbackLabel = (lastMatched?.meta?.label || lastMatched?.name || route.path.split('/').pop() || '未知') as string
    result.push({ path: route.path, label: fallbackLabel })
  }

  return result
})

// 同步路由到菜单
watch(() => route.path, (path) => {
  selectedKeys.value = [path]
}, { immediate: true })

const handleMenuClick = (e: any) => {
  const key = String(e.key)
  if (key && key.startsWith('/') && !key.startsWith('sub-')) {
    router.push(key)
  }
}

const handleUserMenuClick = ({ key }: { key: string }) => {
  if (key === 'logout') {
    handleLogout()
  } else if (key === 'profile') {
    router.push('/profile')
  }
}

const handleLogout = async () => {
  try {
    await logoutApi()
  } catch (error) {
    console.error('退出登录失败:', error)
  }
  userStore.logout()
  router.replace('/login')
}

const checkUserStatus = async () => {
  // 1. 基础校验：如果本地连 Token 都没有，直接拦截
  if (!userStore.isLoggedIn) {
    router.replace('/login')
    return
  }
  
  // 2. 详细信息校验：
  // 如果当前 Store 中还没有用户 ID，说明是新打开页面或刚登录，需要拉取详细信息
  // 如果已经有 ID 了，说明信息已同步，无需在每次内页跳转时都重复请求接口
  if (!userStore.userInfo.id) {
    try {
      const res = await getUserInfoApi()
      // 更新 Store（合并原有 Token 和新获取的用户资料）
      userStore.login({
        ...userStore.userInfo,
        ...res
      })
    } catch (error) {
      // 这里的错误（如 401）已经在 request.ts 拦截器中统一处理（清除状态并跳回登录页）
      // 此处无需额外操作，只需捕获异常防止控制台报错
    }
  }
}

// 使用 watch 监听路由路径变化
// immediate: true 确保了组件挂载时（刷新页面）立即执行一次
// 之后的每次内页跳转也都会触发 checkUserStatus
watch(() => router.currentRoute.value.path, () => {
  checkUserStatus()
}, { immediate: true })

</script>

<template>
  <a-layout class="h-screen overflow-hidden">
    <!-- 左侧菜单：由 Layout 自动管理宽度 -->
    <a-layout-sider 
      v-model:collapsed="collapsed" 
      collapsible 
      :trigger="null"
      width="200"
      theme="light"
      class="shadow-sm transition-all duration-300 bg-white"
    >
      <div 
        class="h-12 m-3 rounded flex items-center justify-center overflow-hidden transition-colors">
        <div class="i-fa6-solid:gem text-xl text-blue-500" :class="collapsed ? '' : 'mr-2'" />
        <span v-if="!collapsed" class="font-bold truncate text-sm">EverKeep</span>
      </div>
      
      <a-menu 
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        theme="light" 
        mode="inline" 
        @click="handleMenuClick"
        class="border-none"
      >
        <template v-for="item in filteredMenuItems" :key="item.label">
          <!-- 分组渲染 -->
          <a-menu-item-group v-if="item.type === 'group' && !item.hidden" :title="item.label">
            <template v-for="subItem in item.children" :key="subItem.key || subItem.label">
              <template v-if="!subItem.hidden">
                <!-- 子菜单 -->
                <a-sub-menu v-if="subItem.children" :key="`sub-${subItem.key || subItem.label}`" :title="subItem.label">
                  <template #icon><div v-if="subItem.icon" :class="[subItem.icon, 'menu-icon']" /></template>
                  <a-menu-item v-for="child in subItem.children" :key="child.key!">
                    {{ child.label }}
                  </a-menu-item>
                </a-sub-menu>
                <!-- 普通菜单项 -->
                <a-menu-item v-else :key="subItem.key!">
                  <template #icon><div v-if="subItem.icon" :class="[subItem.icon, 'menu-icon']" /></template>
                  {{ subItem.label }}
                </a-menu-item>
              </template>
            </template>
          </a-menu-item-group>

          <!-- 顶级非分组项 (非隐藏) -->
          <a-menu-item v-else-if="!item.hidden && item.type !== 'group'" :key="item.key!">
            <template #icon><div v-if="item.icon" :class="[item.icon, 'menu-icon']" /></template>
            {{ item.label }}
          </a-menu-item>
        </template>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <!-- 顶部功能栏 -->
      <a-layout-header 
        class="h-12 px-2 flex items-center justify-between shadow-sm border-b border-gray-100 transition-all duration-300"
        :style="{ background: '#ffffff', padding: '0 16px 0 8px' }"
      >
        <div class="flex items-center gap-3">
          <div 
            class="w-9 h-9 flex items-center justify-center cursor-pointer hover:bg-black/5 rounded-md transition-colors text-[14px]" 
            @click="collapsed = !collapsed"
          >
            <div :class="collapsed ? 'i-fa6-solid:bars' : 'i-fa6-solid:bars-staggered'" />
          </div>
          <a-breadcrumb class="text-[14px] flex items-center">
            <a-breadcrumb-item v-for="bc in breadcrumbs" :key="bc.path">
              <span class="opacity-80">{{ bc.label }}</span>
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        
        <div class="flex items-center gap-6">
          <a-badge :count="unreadCount" :offset="[2, 0]" size="small" :show-zero="false">
            <div class="i-fa6-solid:bell text-[14px] cursor-pointer" @click="handleBellClick" />
          </a-badge>
          <a-dropdown placement="bottomRight">
            <div class="flex items-center gap-2 cursor-pointer p-1 hover:bg-gray-50 rounded">
              <a-avatar :size="24" :src="getImageUrl(userStore.avatar)" />
              <span class="text-[14px] font-medium">{{ userStore.name }}</span>
            </div>
            <template #overlay>
              <a-menu class="w-32" @click="({ key }) => handleUserMenuClick({ key: String(key) })">
                <a-menu-item key="profile"><div class="flex items-center gap-2 text-sm"><div class="i-fa6-solid:user" /> 个人中心</div></a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" danger><div class="flex items-center gap-2 text-sm"><div class="i-fa6-solid:power-off" /> 退出</div></a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 内容区 -->
      <a-layout-content class="p-3 bg-[#f0f2f5] overflow-auto">
        <div class="bg-white p-4 rounded min-h-full shadow-sm">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </a-layout-content>

      <!-- 底部栏：高度减小 -->
      <a-layout-footer v-if="showFooter" class="p-2 text-center text-gray-400 text-[14px] bg-white border-t border-gray-100">
        EverKeep ©2026 Created by Lewis
      </a-layout-footer>
    </a-layout>

    <MessageBox 
      v-model:visible="messageBoxVisible" 
      @unread-change="handleUnreadChange" 
    />
  </a-layout>
</template>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

:deep(.ant-menu-item-group-title) {
  font-size: 12px;
  padding-left: 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: rgba(0, 0, 0, 0.35);
}
</style>
