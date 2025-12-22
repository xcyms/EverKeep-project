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
  <div class="h-full flex flex-col bg-gray-100">
    <!-- 顶部背景和用户信息区域 -->
    <div class="relative h-60 flex flex-col items-center justify-center bg-blue-400 pt-10" :style="{ paddingTop: `${statusBarHeight + 20}px` }">
      <!-- 模拟背景图案，实际可替换为图片 -->
      <div class="absolute inset-0 overflow-hidden">
        <div class="absolute h-40 w-40 rounded-full bg-blue-300 opacity-30 -left-10 -top-10"></div>
        <div class="absolute right-0 top-10 h-32 w-32 rounded-full bg-blue-300 opacity-30"></div>
        <div class="absolute bottom-0 left-1/4 h-20 w-20 rounded-full bg-blue-300 opacity-30"></div>
        <div class="absolute bottom-5 right-1/4 h-24 w-24 rounded-full bg-blue-300 opacity-30"></div>
      </div>

      <div class="relative z-10 flex items-center justify-start">
        <image src="https://lsky.navhub.abrdns.com/i/2025/12/22/6948fb68f1834.png" mode="widthFix" class="mr-4 h-12 w-12 border-4 border-white rounded-full shadow-lg" />
        <div class="text-xl text-white font-bold">{{ user.isLoggedIn ? user.user?.name || '游客' : '游客' }}</div>
      </div>
    </div>

    <!-- 统计数据区域 -->
    <div class="z-20 grid grid-cols-2 mx-4 gap-4 rounded-lg bg-white p-4 shadow-md -mt-10">
      <div class="flex flex-col items-center">
        <div class="text-xl text-green-500 font-bold">{{ user.isLoggedIn ? (user.user?.capacity ? `${(user.user?.capacity / (1024 * 1024 * 1024)).toFixed(2)  } GB` : '0 GB') : '--' }}</div>
        <div class="text-sm text-gray-500">总容量</div>
      </div>
      <div class="flex flex-col items-center">
        <div class="text-xl text-purple-500 font-bold">{{ user.isLoggedIn ? (user.user?.used_capacity ? `${(user.user?.used_capacity / (1024 * 1024 * 1024)).toFixed(2)  } GB` : '0 GB') : '--' }}</div>
        <div class="text-sm text-gray-500">已使用</div>
      </div>
      <div class="flex flex-col items-center">
        <div class="text-xl text-blue-500 font-bold">{{ user.isLoggedIn ? (user.user?.image_num || 0) : '--' }}</div>
        <div class="text-sm text-gray-500">图片数量</div>
      </div>
      <div class="flex flex-col items-center">
        <div class="text-xl text-red-500 font-bold">{{ user.isLoggedIn ? (user.user?.album_num || 0) : '--' }}</div>
        <div class="text-sm text-gray-500">相册数量</div>
      </div>
    </div>

    <!-- 菜单列表区域 -->
    <div class="flex-1 overflow-y-auto p-4">
      <!-- 第一组菜单 -->
      <wd-cell-group custom-class="mb-4 rounded-lg shadow-md">
        <wd-cell title="关于" is-link icon="info-circle" />
        <wd-cell title="赞赏" is-link icon="heart" />
      </wd-cell-group>

      <!-- 第二组菜单 -->
      <wd-cell-group custom-class="mb-4 rounded-lg shadow-md">
        <wd-cell title="合作勾搭" is-link icon="link" />
        <wd-cell title="问题反馈" is-link icon="chat" />
      </wd-cell-group>
    </div>

    <!-- 底部退出登录按钮 -->
    <div class="border-t border-gray-200 bg-white p-4" v-if="user.isLoggedIn">
      <wd-button type="primary" block @click="doLogout" :loading="loading">退出登录</wd-button>
    </div>
  </div>
</template>
