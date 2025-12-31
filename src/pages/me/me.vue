<script lang="ts" setup>
definePage({
  name: 'me',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '我的',
    navigationStyle: 'custom',
  },
})
const user = useAuthStore()
const router = useRouter()
const toast = useToast()
const loading = ref(false)
const statusBarHeight = ref(0)

const { send: getUserInfo } = useRequest(() => Apis.lsky.profile({
  headers: {
    Authorization: `Bearer ${user.token}`,
  },
}))

const { send: logout } = useRequest((config) => Apis.lsky.logout({
  ...config,
  headers: {
    ...config.headers,
    Authorization: `Bearer ${user.token}`,
  },
}), {
  immediate: false,
})

async function doLogout() {
  loading.value = true
  await logout({}).then(() => {
    toast.success('退出登录成功')
    user.logout();
  }).finally(() => {
    loading.value = false
  })
}

function goLogin() {
  if (!user.isLoggedIn) {
    router.push({ name: 'login' })
  }
}

// 监听登录状态变化，重新加载数据
watch(() => user.isLoggedIn, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    getUserInfo({}).then((res) => {
      if (res.status) {
        user.user = res.data
      } else {
        toast.error(res.message || '获取用户信息失败')
      }
    })
  }
})

onMounted(() => {
  uni.getSystemInfo({
    success: (res) => {
      statusBarHeight.value = res.statusBarHeight || 0
    },
  })
})

</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa] pb-10">
    <!-- 顶部背景和用户信息区域 -->
    <div class="relative overflow-hidden bg-white pb-12">
      <!-- 动态背景装饰 -->
      <div class="absolute h-80 w-80 rounded-full bg-blue-50/50 blur-3xl -right-20 -top-20" />
      <div class="absolute top-10 h-60 w-60 rounded-full bg-purple-50/30 blur-3xl -left-10" />

      <!-- 用户基本信息 -->
      <div class="relative z-10 flex flex-col items-center pt-16" :style="{ paddingTop: `${statusBarHeight + 40}px` }">
        <div class="relative mb-4">
          <div class="absolute inset-0 scale-110 rounded-full bg-blue-100/50 blur-md" />
          <image
            :src="user.isLoggedIn ? 'https://lsky.navhub.abrdns.com/i/2025/12/22/6948fb68f1834.png' : '/static/default-avatar.png'"
            mode="aspectFill"
            class="relative h-24 w-24 border-4 border-white rounded-full shadow-blue-100/50 shadow-xl"
          />
        </div>
        <div class="flex flex-col items-center" @click="goLogin">
          <div class="text-[16px] text-gray-900 font-600 tracking-tight">
            {{ user.isLoggedIn ? user.user?.name || '管理员用户' : '点击登录体验更多' }}
          </div>
          <div class="mt-1 flex items-center gap-1.5 rounded-full bg-gray-100 px-3 py-0.5 text-[11px] text-gray-400 font-medium">
            <wd-icon name="user" size="12px" />
            <span>{{ user.isLoggedIn ? (user.user?.email || 'Premium User') : '未授权访问' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 核心数据看板 -->
    <div class="relative z-20 mx-5 -mt-8">
      <div class="grid grid-cols-2 gap-4">
        <!-- 容量卡片 -->
        <div class="col-span-2 overflow-hidden rounded-3xl bg-white p-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
          <div class="mb-4 flex items-center justify-between">
            <span class="text-sm text-gray-400 font-medium">存储空间</span>
            <span class="text-xs text-blue-600 font-bold">{{ user.isLoggedIn && user.user?.used_capacity != null && user.user?.capacity ? (user.user.used_capacity / user.user.capacity * 100).toFixed(1) : 0 }}% 已用</span>
          </div>
          <div class="mb-3 h-2 w-full overflow-hidden rounded-full bg-gray-100">
            <div
              class="h-full from-blue-500 to-indigo-500 bg-gradient-to-r transition-all duration-1000"
              :style="{ width: user.isLoggedIn && user.user?.used_capacity != null && user.user?.capacity ? `${(user.user.used_capacity / user.user.capacity * 100).toFixed(1)}%` : '0%' }" />
          </div>
          <div class="flex items-end justify-between">
            <div class="flex flex-col">
              <span class="text-xl text-gray-900 font-bold tracking-tighter">
                {{ user.isLoggedIn && user.user?.used_capacity != null ? (user.user?.used_capacity / 1024).toFixed(2) : '0.00' }} M
              </span>
              <span class="text-[10px] text-gray-400 tracking-widest uppercase">USED SPACE</span>
            </div>
            <div class="flex flex-col items-end">
              <span class="text-sm text-gray-600 font-bold">
                {{ user.isLoggedIn && user.user?.capacity != null ? (user.user?.capacity / 1024).toFixed(0) : '0' }} M
              </span>
              <span class="text-[10px] text-gray-400 tracking-widest uppercase">TOTAL</span>
            </div>
          </div>
        </div>

        <!-- 数量统计 -->
        <div class="flex flex-col items-center justify-center rounded-3xl bg-white py-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
          <div class="mb-1 text-2xl text-gray-900 font-bold tracking-tighter">
            {{ user.isLoggedIn ? (user.user?.image_num || 0) : '--' }}
          </div>
          <div class="text-[10px] text-gray-400 font-bold tracking-widest uppercase">Images</div>
        </div>
        <div class="flex flex-col items-center justify-center rounded-3xl bg-white py-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
          <div class="mb-1 text-2xl text-gray-900 font-bold tracking-tighter">
            {{ user.isLoggedIn ? (user.user?.album_num || 0) : '--' }}
          </div>
          <div class="text-[10px] text-gray-400 font-bold tracking-widest uppercase">Albums</div>
        </div>
      </div>
    </div>

    <!-- 功能菜单列表 -->
    <div class="mt-8 px-5">
      <div class="overflow-hidden rounded-3xl bg-white shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
        <div class="py-2 space-y-1">
          <div class="group flex items-center justify-between p-4 transition-colors active:bg-gray-50">
            <div class="flex items-center gap-4">
              <div class="h-10 w-10 flex items-center justify-center rounded-2xl bg-blue-50 text-blue-600">
                <wd-icon name="info-circle" size="20px" />
              </div>
              <span class="text-[15px] text-gray-700 font-medium">关于系统</span>
            </div>
            <wd-icon name="arrow-right" size="16px" color="#cbd5e1" />
          </div>

          <div class="mx-4 h-[1px] bg-gray-50" />

          <div class="group flex items-center justify-between p-4 transition-colors active:bg-gray-50">
            <div class="flex items-center gap-4">
              <div class="h-10 w-10 flex items-center justify-center rounded-2xl bg-red-50 text-red-500">
                <wd-icon name="heart" size="20px" />
              </div>
              <span class="text-[15px] text-gray-700 font-medium">赞赏支持</span>
            </div>
            <wd-icon name="arrow-right" size="16px" color="#cbd5e1" />
          </div>

          <div class="mx-4 h-[1px] bg-gray-50" />

          <div class="group flex items-center justify-between p-4 transition-colors active:bg-gray-50">
            <div class="flex items-center gap-4">
              <div class="h-10 w-10 flex items-center justify-center rounded-2xl bg-orange-50 text-orange-500">
                <wd-icon name="link" size="20px" />
              </div>
              <span class="text-[15px] text-gray-700 font-medium">合作勾搭</span>
            </div>
            <wd-icon name="arrow-right" size="16px" color="#cbd5e1" />
          </div>

          <div class="mx-4 h-[1px] bg-gray-50" />

          <div class="group flex items-center justify-between p-4 transition-colors active:bg-gray-50">
            <div class="flex items-center gap-4">
              <div class="h-10 w-10 flex items-center justify-center rounded-2xl bg-green-50 text-green-600">
                <wd-icon name="chat" size="20px" />
              </div>
              <span class="text-[15px] text-gray-700 font-medium">问题反馈</span>
            </div>
            <wd-icon name="arrow-right" size="16px" color="#cbd5e1" />
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <div class="mt-10 px-10" v-if="user.isLoggedIn">
      <div
        class="flex items-center justify-center gap-2 rounded-2xl bg-gray-900 py-4 text-center text-[16px] text-white font-bold shadow-gray-200 shadow-xl transition-all active:scale-[0.98] active:opacity-90"
        @tap="doLogout"
      >
        <wd-icon name="logout" size="18px" />
        <span>退出当前账号</span>
      </div>
    </div>
    <div class="mt-10 px-10" v-else>
      <div
        class="rounded-2xl bg-blue-600 py-4 text-center text-[16px] text-white font-bold shadow-blue-100 shadow-xl transition-all active:scale-[0.98] active:opacity-90"
        @tap="router.push({ name: 'login' })"
      >
        立即登录
      </div>
    </div>
  </div>
</template>
