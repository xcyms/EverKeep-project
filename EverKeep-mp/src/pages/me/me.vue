<script lang="ts" setup>
import type {ThemeColorOption} from '@/composables/useManualTheme';
import type { SummaryDTO } from '@/types/type';
import { computed, ref, watch } from 'vue'
import { themeColorOptions, useManualTheme  } from '@/composables/useManualTheme'
import { DEFAULT_AVATAR } from '@/utils/constants'

definePage({
  name: 'me',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '我的',
    navigationStyle: 'custom'
  },
})

const user = useAuthStore()
const router = useRouter()
const toast = useToast()
const loading = ref(false)
const showContactPopup = ref(false)
const { statusBarHeight } = useSystemInfo()
const summary = ref<SummaryDTO>()

// 格式化文件大小
function formatSize (bytes: number) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${Number.parseFloat((bytes / (k ** i)).toFixed(2))} ${sizes[i]}`
}

// 计算存储百分比
const storagePercentage = computed(() => {
  if (!user.isLoggedIn || !summary.value?.totalSize) return 0
  const percentage = (summary.value.storageUsage || 0) / summary.value.totalSize * 100
  return Math.min(100, Number.parseFloat(percentage.toFixed(1)))
})

// 格式化已用空间
const usedSpaceFormatted = computed(() => {
  return user.isLoggedIn ? formatSize(Number(summary.value?.storageUsage) || 0) : '0 B'
})

// 格式化总空间
const totalSpaceFormatted = computed(() => {
  return user.isLoggedIn ? formatSize(Number(summary.value?.totalSize) || 0) : '0 B'
})

// 主题管理
const {
  theme,
  isDark,
  followSystem,
  currentThemeColor,
  toggleTheme,
  setFollowSystem,
  selectThemeColor,
} = useManualTheme()

const showThemeSheet = ref(false)
const showThemeColorSheet = ref(false)

const { send: getUserInfo } = useRequest(() => Apis.everkeep.getUserInfo({}), {
  immediate: false,
})

const { send: logout } = useRequest(() => Apis.everkeep.logout({}), {
  immediate: false,
})

const { send: getSummary } = useRequest(() => Apis.everkeep.getSummary({}), {
  immediate: false,
})

async function doLogout() {
  loading.value = true
  try {
    const res = await logout()
    if (res.code !== 200) {
      throw new Error(res.msg || '退出登录失败')
    }
    toast.success('退出登录成功')
    user.logout()
  } catch (error) {
    console.error('退出登录失败:', error)
    toast.error('退出登录失败')
  } finally {
    loading.value = false
  }
}

function goLogin() {
  if (!user.isLoggedIn) {
    router.push({ name: 'login' })
  }
}

function handleMenuClick(type: string) {
  if (type === 'about') {
    router.push({ name: 'about' })
  } else if (type === 'theme') {
    showThemeSheet.value = true
  } else if (type === 'themeColor') {
    showThemeColorSheet.value = true
  } else {
    showContactPopup.value = true
  }
}

function handleThemeSelect(mode: 'light' | 'dark' | 'system') {
  if (mode === 'system') {
    setFollowSystem(true)
  } else {
    toggleTheme(mode)
  }
  showThemeSheet.value = false
  toast.success(`已切换到${mode === 'light' ? '浅色' : mode === 'dark' ? '深色' : '跟随系统'}模式`)
}

function handleThemeColorSelect(option: ThemeColorOption) {
  selectThemeColor(option)
  showThemeColorSheet.value = false
  toast.success(`已切换到${option.name}主题色`)
}

function copyEmail() {
  uni.setClipboardData({
    data: 'lewisdeemail@163.com',
    success: () => {
      toast.success('邮箱已复制')
      showContactPopup.value = false
    },
  })
}

// 监听登录状态变化，重新加载数据
watch(
  () => user.isLoggedIn,
  async (newVal) => {
    if (newVal) {
      loading.value = true
      try {
        // 并行获取用户信息和统计数据
        const [userRes, summaryRes] = await Promise.all([
          getUserInfo(),
          getSummary(),
        ])

        if (userRes.code === 200 && userRes.data) {
          // 适配 UserDTO 到 Store 中的 User 类型
          user.updateUser(userRes.data)
        }

        if (summaryRes.code === 200 && summaryRes.data) {
          summary.value = summaryRes.data
          // 同步统计数据到 user store 以供全局使用
        }
      } catch (error) {
        console.error('获取个人统计信息失败:', error)
      } finally {
        loading.value = false
      }
    }
  },
  { immediate: true }
)
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-10 dark:bg-black">
    <!-- 顶部背景和用户信息区域 -->
    <div class="relative overflow-hidden bg-white pb-12 dark:bg-gray-900">
      <!-- 动态背景装饰 -->
      <div class="absolute h-80 w-80 rounded-full bg-blue-50/50 blur-3xl -right-20 -top-20 dark:bg-blue-900/10" />
      <div class="absolute top-10 h-60 w-60 rounded-full bg-purple-50/30 blur-3xl -left-10 dark:bg-purple-900/10" />

      <!-- 用户基本信息 -->
      <div class="relative z-10 flex flex-col items-center pt-16" :style="{ paddingTop: `${statusBarHeight + 40}px` }">
        <!-- 左上角功能区 (避开胶囊按钮) -->
        <div
          class="absolute left-6 flex items-center gap-3"
          :style="{ top: `${statusBarHeight + 10}px` }"
        >
          <div
            class="h-9 w-9 flex items-center justify-center rounded-full bg-white/60 shadow-sm backdrop-blur-md transition-all active:scale-90 dark:bg-black/30"
            @tap="handleMenuClick('theme')"
          >
            <div
              :class="followSystem ? 'i-solar-clapperboard-edit-bold-duotone' : (isDark ? 'i-solar-moon-bold-duotone' : 'i-solar-sun-2-bold-duotone')"
              class="text-lg"
              :style="{ color: currentThemeColor.primary }"
            />
          </div>
          <div
            class="h-9 w-9 flex items-center justify-center rounded-full bg-white/60 shadow-sm backdrop-blur-md transition-all active:scale-90 dark:bg-black/30"
            @tap="handleMenuClick('themeColor')"
          >
            <div
              class="h-4.5 w-4.5 border-2 border-white rounded-full shadow-inner dark:border-gray-800"
              :style="{ backgroundColor: currentThemeColor.primary }"
            />
          </div>
        </div>

        <div class="relative mb-4">
          <div class="absolute inset-0 scale-110 rounded-full bg-blue-100/50 blur-md dark:bg-blue-900/30" />
          <image
            :src="user.isLoggedIn ? getImageUrl(user.userAvatar) : DEFAULT_AVATAR" mode="aspectFill"
            class="relative h-24 w-24 border-4 border-white rounded-full shadow-blue-100/50 shadow-xl dark:border-gray-800 dark:shadow-none" />
        </div>
        <div class="flex flex-col items-center" @click="goLogin">
          <div class="text-[16px] text-gray-900 font-600 tracking-tight dark:text-gray-100">
            {{ user.isLoggedIn ? user.userName : '点击登录体验更多' }}
          </div>
          <div
            class="mt-1 flex items-center gap-1.5 rounded-full bg-gray-100 px-3 py-0.5 text-[11px] text-gray-400 font-medium dark:bg-gray-800 dark:text-gray-500">
            <wd-icon name="user" size="12px" />
            <span>{{ user.isLoggedIn ? (user.user?.email || 'Premium User') : '未授权访问' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 核心数据看板 -->
    <div class="relative mx-5">
      <div class="grid grid-cols-2 gap-4">
        <!-- 容量卡片 -->
        <div class="col-span-2 overflow-hidden rounded-3xl bg-white p-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] dark:bg-gray-900 dark:shadow-none">
          <div class="mb-4 flex items-center justify-between">
            <span class="text-sm text-gray-400 font-medium dark:text-gray-500">存储空间</span>
            <span class="text-xs text-blue-600 font-bold dark:text-blue-400">{{ storagePercentage }}% 已用</span>
          </div>
          <div class="mb-3 h-2 w-full overflow-hidden rounded-full bg-gray-100 dark:bg-gray-800">
            <div
              class="h-full from-blue-500 to-indigo-500 bg-gradient-to-r transition-all duration-1000"
              :style="{ width: `${storagePercentage}%` }" />
          </div>
          <div class="flex items-end justify-between">
            <div class="flex flex-col">
              <span class="text-xl text-gray-900 font-bold tracking-tighter dark:text-gray-100">
                {{ usedSpaceFormatted }}
              </span>
              <span class="text-[10px] text-gray-400 tracking-widest uppercase dark:text-gray-500">USED SPACE</span>
            </div>
            <div class="flex flex-col items-end">
              <span class="text-sm text-gray-600 font-bold dark:text-gray-300">
                {{ totalSpaceFormatted }}
              </span>
              <span class="text-[10px] text-gray-400 tracking-widest uppercase dark:text-gray-500">TOTAL</span>
            </div>
          </div>
        </div>

        <!-- 数量统计 -->
        <div
          class="flex flex-col items-center justify-center rounded-3xl bg-white py-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] dark:bg-gray-900 dark:shadow-none">
          <div class="mb-1 text-2xl text-gray-900 font-bold tracking-tighter dark:text-gray-100">
            {{ user.isLoggedIn ? (summary?.imageCount || 0) : '--' }}
          </div>
          <div class="text-[10px] text-gray-400 font-bold tracking-widest uppercase dark:text-gray-500">Images</div>
        </div>
        <div
          class="flex flex-col items-center justify-center rounded-3xl bg-white py-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] dark:bg-gray-900 dark:shadow-none">
          <div class="mb-1 text-2xl text-gray-900 font-bold tracking-tighter dark:text-gray-100">
            {{ user.isLoggedIn ? (summary?.albumCount || 0) : '--' }}
          </div>
          <div class="text-[10px] text-gray-400 font-bold tracking-widest uppercase dark:text-gray-500">Albums</div>
        </div>
      </div>
    </div>

    <!-- 功能菜单列表 -->
    <div class="mt-4 px-5">
      <div class="overflow-hidden rounded-3xl bg-white shadow-[0_8px_30px_rgb(0,0,0,0.04)] dark:bg-gray-900 dark:shadow-none">
        <div class="py-2 space-y-1">
          <div
            class="flex items-center justify-between p-4 transition-colors active:bg-gray-50 dark:active:bg-gray-800"
            @tap="handleMenuClick('about')">
            <div class="flex items-center gap-4">
              <div class="h-10 w-10 flex items-center justify-center rounded-2xl bg-blue-50 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400">
                <div class="i-solar-info-circle-bold-duotone text-xl" />
              </div>
              <span class="text-[15px] text-gray-700 font-medium dark:text-gray-200">关于系统</span>
            </div>
            <div class="i-solar-alt-arrow-right-linear text-gray-300" />
          </div>

          <div class="mx-4 h-[1px] bg-gray-50 dark:bg-gray-800" />

          <div
            class="flex items-center justify-between p-4 transition-colors active:bg-gray-50 dark:active:bg-gray-800"
            @tap="handleMenuClick('support')">
            <div class="flex items-center gap-4">
              <div class="h-10 w-10 flex items-center justify-center rounded-2xl bg-red-50 text-red-500 dark:bg-red-900/20 dark:text-red-400">
                <div class="i-solar-heart-bold-duotone text-xl" />
              </div>
              <span class="text-[15px] text-gray-700 font-medium dark:text-gray-200">赞赏支持</span>
            </div>
            <div class="i-solar-alt-arrow-right-linear text-gray-300" />
          </div>

          <div class="mx-4 h-[1px] bg-gray-50 dark:bg-gray-800" />

          <div
            class="flex items-center justify-between p-4 transition-colors active:bg-gray-50 dark:active:bg-gray-800"
            @tap="handleMenuClick('contact')">
            <div class="flex items-center gap-4">
              <div class="h-10 w-10 flex items-center justify-center rounded-2xl bg-orange-50 text-orange-500 dark:bg-orange-900/20 dark:text-orange-400">
                <div class="i-solar-link-bold-duotone text-xl" />
              </div>
              <span class="text-[15px] text-gray-700 font-medium dark:text-gray-200">合作勾搭</span>
            </div>
            <div class="i-solar-alt-arrow-right-linear text-gray-300" />
          </div>

          <div class="mx-4 h-[1px] bg-gray-50 dark:bg-gray-800" />

          <div
            class="flex items-center justify-between p-4 transition-colors active:bg-gray-50 dark:active:bg-gray-800"
            @tap="handleMenuClick('feedback')">
            <div class="flex items-center gap-4">
              <div class="h-10 w-10 flex items-center justify-center rounded-2xl bg-green-50 text-green-600 dark:bg-green-900/20 dark:text-green-400">
                <div class="i-solar-chat-round-dots-bold-duotone text-xl" />
              </div>
              <span class="text-[15px] text-gray-700 font-medium dark:text-gray-200">问题反馈</span>
            </div>
            <div class="i-solar-alt-arrow-right-linear text-gray-300" />
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <div class="mt-10 px-10" v-if="user.isLoggedIn">
        <div
          class="flex items-center justify-center gap-2 rounded-2xl bg-gray-900 py-4 text-center text-[16px] text-white font-bold shadow-gray-200 shadow-xl transition-all active:scale-[0.98] dark:bg-gray-800 active:opacity-90 dark:shadow-none"
          :class="{ 'opacity-70 pointer-events-none': loading }" @tap="doLogout">
          <wd-loading v-if="loading" size="18px" color="#fff" custom-class="mr-2" />
          <div v-else class="i-solar-logout-2-bold text-lg" />
          <span>{{ loading ? '正在退出...' : '退出当前账号' }}</span>
        </div>
    </div>
    <div class="mt-10 px-10" v-else>
      <div
        class="rounded-2xl bg-blue-600 py-4 text-center text-[16px] text-white font-bold shadow-blue-100 shadow-xl transition-all active:scale-[0.98] active:opacity-90 dark:shadow-none"
        @tap="router.push({ name: 'login' })">
        立即登录
      </div>
    </div>
    <!-- 联系方式弹窗 -->
    <wd-popup
      v-model="showContactPopup" position="bottom" round custom-class="rounded-t-[40rpx] overflow-hidden"
      :z-index="10002" safe-area-inset-bottom>
      <div class="bg-white px-6 pb-8 pt-6 dark:bg-gray-900">
        <div class="mb-6 flex flex-col items-center">
          <div class="mb-4 h-16 w-16 flex items-center justify-center rounded-full bg-blue-50 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400">
            <div class="i-solar-letter-bold-duotone text-3xl" />
          </div>
          <span class="text-lg text-gray-900 font-bold dark:text-gray-100">联系作者</span>
          <span class="mt-1 text-center text-sm text-gray-400 dark:text-gray-500">如果您有任何问题或建议，欢迎随时联系</span>
        </div>

        <div
          class="flex items-center justify-between rounded-2xl bg-gray-50 p-4 transition-all active:scale-[0.98] dark:bg-gray-800"
          @tap="copyEmail">
          <div class="flex flex-col">
            <span class="mb-0.5 text-[10px] text-gray-400 font-bold tracking-wider uppercase dark:text-gray-500">Email Address</span>
            <span class="text-[16px] text-gray-800 font-600 dark:text-gray-200">lewisdeemail@163.com</span>
          </div>
          <div class="border border-blue-50 rounded-lg bg-white px-3 py-1.5 text-xs text-blue-600 font-bold shadow-sm dark:border-blue-900/50 dark:bg-gray-700 dark:text-blue-400">
            点击复制
          </div>
        </div>

        <div
          class="mt-8 w-full rounded-2xl bg-gray-900 py-4 text-center text-[16px] text-white font-bold shadow-gray-200 shadow-lg transition-all active:scale-[0.99] dark:bg-gray-800 active:opacity-90 dark:shadow-none"
          @tap="showContactPopup = false">
          我知道了
        </div>

        <!-- 额外的底部占位，确保在非安全区机型上也有足够间距 -->
        <div class="h-4 w-full" />
      </div>
    </wd-popup>

    <!-- 主题模式选择弹窗 -->
    <wd-popup
      v-model="showThemeSheet" position="bottom" round custom-class="rounded-t-[40rpx] overflow-hidden"
      :z-index="10002" safe-area-inset-bottom>
      <div class="bg-white px-6 pb-8 pt-6 dark:bg-gray-900">
        <div class="mb-6 flex flex-col items-center">
          <div class="mb-4 h-16 w-16 flex items-center justify-center rounded-full bg-purple-50 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400">
            <wd-icon name="translate-bold" size="32px" />
          </div>
          <span class="text-lg text-gray-900 font-bold dark:text-gray-100">选择主题模式</span>
          <span class="mt-1 text-center text-sm text-gray-400 dark:text-gray-500">选择您偏好的界面主题</span>
        </div>

        <div class="space-y-3">
          <!-- 浅色模式 -->
          <div
            class="flex items-center justify-between rounded-2xl p-4 transition-all active:scale-[0.98]"
            :class="!followSystem && theme === 'light' ? 'bg-blue-50 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-800' : 'bg-gray-50 dark:bg-gray-800'"
            @tap="handleThemeSelect('light')">
            <div class="flex items-center gap-4">
              <div
                class="h-12 w-12 flex items-center justify-center rounded-xl"
                :class="!followSystem && theme === 'light' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-700 text-gray-600 dark:text-gray-300'">
                <div class="i-solar-sun-2-bold-duotone text-2xl" />
              </div>
              <div class="flex flex-col">
                <span
                  class="text-[15px] font-semibold"
                  :class="!followSystem && theme === 'light' ? 'text-blue-700 dark:text-blue-400' : 'text-gray-900 dark:text-gray-100'">
                  浅色模式
                </span>
                <span class="text-xs" :class="!followSystem && theme === 'light' ? 'text-blue-600 dark:text-blue-500' : 'text-gray-400 dark:text-gray-500'">
                  始终使用浅色主题
                </span>
              </div>
            </div>
            <div
              v-if="!followSystem && theme === 'light'"
              class="h-6 w-6 flex items-center justify-center rounded-full bg-blue-600">
              <wd-icon name="check" size="14px" color="#fff" />
            </div>
          </div>

          <!-- 深色模式 -->
          <div
            class="flex items-center justify-between rounded-2xl p-4 transition-all active:scale-[0.98]"
            :class="!followSystem && theme === 'dark' ? 'bg-blue-50 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-800' : 'bg-gray-50 dark:bg-gray-800'"
            @tap="handleThemeSelect('dark')">
            <div class="flex items-center gap-4">
              <div
                class="h-12 w-12 flex items-center justify-center rounded-xl"
                :class="!followSystem && theme === 'dark' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-700 text-gray-600 dark:text-gray-300'">
                <div class="i-solar-moon-bold-duotone text-2xl" />
              </div>
              <div class="flex flex-col">
                <span
                  class="text-[15px] font-semibold"
                  :class="!followSystem && theme === 'dark' ? 'text-blue-700 dark:text-blue-400' : 'text-gray-900 dark:text-gray-100'">
                  深色模式
                </span>
                <span class="text-xs" :class="!followSystem && theme === 'dark' ? 'text-blue-600 dark:text-blue-500' : 'text-gray-400 dark:text-gray-500'">
                  始终使用深色主题
                </span>
              </div>
            </div>
            <div
              v-if="!followSystem && theme === 'dark'"
              class="h-6 w-6 flex items-center justify-center rounded-full bg-blue-600">
              <wd-icon name="check" size="14px" color="#fff" />
            </div>
          </div>

          <!-- 跟随系统 -->
          <div
            class="flex items-center justify-between rounded-2xl p-4 transition-all active:scale-[0.98]"
            :class="followSystem ? 'bg-blue-50 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-800' : 'bg-gray-50 dark:bg-gray-800'"
            @tap="handleThemeSelect('system')">
            <div class="flex items-center gap-4">
              <div
                class="h-12 w-12 flex items-center justify-center rounded-xl"
                :class="followSystem ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-700 text-gray-600 dark:text-gray-300'">
                <div class="i-solar-clapperboard-edit-bold-duotone text-2xl" />
              </div>
              <div class="flex flex-col">
                <span class="text-[15px] font-semibold" :class="followSystem ? 'text-blue-700 dark:text-blue-400' : 'text-gray-900 dark:text-gray-100'">
                  跟随系统
                </span>
                <span class="text-xs" :class="followSystem ? 'text-blue-600 dark:text-blue-500' : 'text-gray-400 dark:text-gray-500'">
                  根据系统设置自动切换
                </span>
              </div>
            </div>
            <div v-if="followSystem" class="h-6 w-6 flex items-center justify-center rounded-full bg-blue-600">
              <wd-icon name="check" size="14px" color="#fff" />
            </div>
          </div>
        </div>

        <div
          class="mt-8 w-full rounded-2xl bg-gray-900 py-4 text-center text-[16px] text-white font-bold shadow-gray-200 shadow-lg transition-all active:scale-[0.99] dark:bg-gray-800 active:opacity-90 dark:shadow-none"
          @tap="showThemeSheet = false">
          完成
        </div>

        <div class="h-4 w-full" />
      </div>
    </wd-popup>

    <!-- 主题色选择弹窗 -->
    <wd-popup
      v-model="showThemeColorSheet" position="bottom" round custom-class="rounded-t-[40rpx] overflow-hidden"
      :z-index="10002" safe-area-inset-bottom>
      <div class="bg-white px-6 pb-8 pt-6 dark:bg-gray-900">
        <div class="mb-6 flex flex-col items-center">
          <div class="mb-4 h-16 w-16 flex items-center justify-center rounded-full bg-pink-50 text-pink-600 dark:bg-pink-900/20 dark:text-pink-400">
            <div class="i-solar-palette-bold-duotone text-3xl" />
          </div>
          <span class="text-lg text-gray-900 font-bold dark:text-gray-100">选择主题色</span>
          <span class="mt-1 text-center text-sm text-gray-400 dark:text-gray-500">选择您喜欢的主题颜色</span>
        </div>

        <div class="grid grid-cols-3 gap-3">
          <div
            v-for="option in themeColorOptions" :key="option.value"
            class="flex flex-col items-center gap-3 rounded-2xl p-4 transition-all active:scale-[0.95]"
            :class="currentThemeColor.value === option.value ? 'bg-gray-100' : 'bg-gray-50'"
            @tap="handleThemeColorSelect(option)">
            <div
              class="h-14 w-14 flex items-center justify-center rounded-full transition-all"
              :style="{ backgroundColor: option.primary }">
              <div v-if="currentThemeColor.value === option.value" class="i-solar-check-read-bold text-2xl text-white" />
            </div>
            <span class="text-xs text-gray-700 font-medium">{{ option.name }}</span>
          </div>
        </div>

        <div
          class="mt-8 w-full rounded-2xl bg-gray-900 py-4 text-center text-[16px] text-white font-bold shadow-gray-200 shadow-lg transition-all active:scale-[0.99] active:opacity-90"
          @tap="showThemeColorSheet = false">
          完成
        </div>

        <div class="h-4 w-full" />
      </div>
    </wd-popup>
  </div>
</template>
