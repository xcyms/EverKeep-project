<script lang="ts" setup>
definePage({
  name: 'login',
  style: {
    navigationBarTitleText: '登录',
    navigationStyle: 'custom',
  },
})

const router = useRouter()
const user = useAuthStore()
const { success: showSuccess, error: showError } = useToast()
const mode = ref<'login' | 'register'>('login')
const loginType = ref<'account' | 'phone'>('account')
const statusBarHeight = ref(0) // 获取状态栏高度
const protocolShow = ref(false) // 协议弹窗显示状态
const loading = ref(false) // 按钮loading状态

const model = reactive<{
  username: string
  password: string
  confirmPassword: string
  read: boolean
}>({
  username: '',
  password: '',
  confirmPassword: '',
  read: false
})

onMounted(() => {
  uni.getSystemInfo({
    success: (res) => {
      statusBarHeight.value = res.statusBarHeight || 0
    },
  })
})

function handleProtocolClick() {
  protocolShow.value = true
}

function handleCopyUrl() {
  uni.setClipboardData({
    data: 'https://personal-navhub.site',
    success: () => {
      showSuccess({
        msg: '网址已复制',
      })
    },
  })
}

function handleClose() {
  protocolShow.value = false
}

async function handleSubmit() {
  if (!model.username) {
    showError({
      msg: '请输入用户名'
    })
    return
  }
  if (!model.password) {
    showError({
      msg: '请输入密码'
    })
    return
  }
  // 注册时的密码复杂度校验
  if (mode.value === 'register') {
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/
    if (!passwordRegex.test(model.password)) {
      showError({
        msg: '密码复杂度不足：需至少8位，包含大小写字母、数字及特殊字符'
      })
      return
    }
  }
  if (mode.value === 'register' && model.password !== model.confirmPassword) {
    showError({
      msg: '两次输入的密码不一致'
    })
    return
  }
  if (!model.read) {
    showError({
      msg: '请先勾选协议'
    })
    return
  }

  loading.value = true
  try {
    if (mode.value === 'login') {
      const res = await Apis.everkeep.login({
        data: { username: model.username, password: model.password }
      })
      if (res.code === 200) {
        showSuccess({ msg: '登录成功' })
        user.login({ username: model.username }, res.data || '')
        router.pushTab({ name: 'home' })
      } else {
        showError({ msg: res.message || '登录失败' })
      }
    } else {
      const res = await Apis.everkeep.register({
        data: { username: model.username, password: model.password }
      })
      if (res.code === 200) {
        showSuccess({ msg: '注册成功，请登录' })
        mode.value = 'login'
      } else {
        showError({ msg: res.message || '注册失败' })
      }
    }
  } catch (err: any) {
    showError({ msg: err.message || '网络错误' })
  } finally {
    loading.value = false
  }
}

function toggleMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
}
</script>

<template>
  <view class="relative min-h-screen w-full flex flex-col overflow-hidden bg-[#f8f9fa]">
    <!-- 装饰背景 -->
    <view class="absolute right-[-10%] top-[-5%] h-[400rpx] w-[400rpx] rounded-full bg-blue-400/10 blur-[80px]" />
    <view class="absolute bottom-[-10%] left-[-10%] h-[500rpx] w-[500rpx] rounded-full bg-indigo-400/10 blur-[100px]" />

    <!-- 顶部标题区 -->
    <view class="px-8 pb-10 pt-24" :style="{ paddingTop: `${statusBarHeight + 40}px` }">
      <view class="text-3xl text-gray-900 font-bold tracking-tight">
        {{ mode === 'login' ? '欢迎回来' : '开启新旅程' }}
      </view>
      <view class="mt-2 text-sm text-gray-400">
        {{ mode === 'login' ? '请登录以管理您的云端资源' : '创建一个账号来存储您的珍贵记忆' }}
      </view>
    </view>

    <!-- 登录卡片 -->
    <view class="flex-1 px-6">
      <view class="overflow-hidden border border-white/50 rounded-3xl bg-white/80 p-8 shadow-[0_20px_50px_rgba(0,0,0,0.08)] backdrop-blur-2xl">
        <!-- 登录类型切换 (仅在登录模式显示) -->
        <view v-if="mode === 'login'" class="relative mb-8 h-12 flex items-center rounded-xl bg-gray-100/80 p-1">
          <view
            class="z-10 flex flex-1 items-center justify-center text-sm font-medium transition-colors duration-300"
            :class="{ 'text-white': loginType === 'account', 'text-gray-500': loginType !== 'account' }"
            @click="loginType = 'account'"
          >
            账号登录
          </view>
          <view
            class="z-10 flex flex-1 items-center justify-center text-sm font-medium opacity-50 transition-colors duration-300"
            :class="{ 'text-white': loginType === 'phone', 'text-gray-500': loginType !== 'phone' }"
            @click="showError({ msg: '手机号登录暂未开放' })"
          >
            验证码登录
          </view>
          <view class="absolute bottom-1 left-1 top-1 w-[calc(50%-4px)] transform-gpu rounded-lg bg-blue-500 shadow-sm transition-transform duration-300" :class="{ 'translate-x-full': loginType === 'phone' }"/>
        </view>
        <view v-else class="mb-8 text-center text-xl text-gray-800 font-bold">用户注册</view>

        <!-- 表单区域 -->
        <view class="space-y-6">
          <view>
            <view class="mb-2 ml-1 text-xs text-gray-400 font-medium tracking-wide uppercase">用户名 / 邮箱</view>
            <wd-input
              v-model="model.username"
              placeholder="请输入用户名"
              no-border
              custom-class="!bg-gray-50/80 !rounded-xl !py-3 !px-4 border border-gray-100"
            >
              <template #prefix>
                <wd-icon name="user" size="18px" color="#94a3b8" class="mr-2" />
              </template>
            </wd-input>
          </view>

          <view>
            <view class="mb-2 ml-1 text-xs text-gray-400 font-medium tracking-wide uppercase">登录密码</view>
            <wd-input
              v-model="model.password"
              placeholder="请输入密码"
              no-border
              show-password
              custom-class="!bg-gray-50/80 !rounded-xl !py-3 !px-4 border border-gray-100"
            >
              <template #prefix>
                <wd-icon name="lock-on" size="18px" color="#94a3b8" class="mr-2" />
              </template>
            </wd-input>
          </view>

          <view v-if="mode === 'register'">
            <view class="mb-2 ml-1 text-xs text-gray-400 font-medium tracking-wide uppercase">确认密码</view>
            <wd-input
              v-model="model.confirmPassword"
              placeholder="请再次输入密码"
              no-border
              show-password
              custom-class="!bg-gray-50/80 !rounded-xl !py-3 !px-4 border border-gray-100"
            >
              <template #prefix>
                <wd-icon name="lock-on" size="18px" color="#94a3b8" class="mr-2" />
              </template>
            </wd-input>
          </view>

          <!-- 登录/注册按钮 -->
          <view class="pt-4">
            <wd-button
              block
              size="large"
              custom-class="!rounded-xl !bg-blue-500 !h-12 !text-lg !font-bold shadow-lg shadow-blue-500/20 active:opacity-90"
              @click="handleSubmit"
              :loading="loading"
            >
              {{ mode === 'login' ? '立即登录' : '立即注册' }}
            </wd-button>
          </view>

          <!-- 模式切换 -->
          <view class="flex justify-center text-sm">
            <text class="text-gray-400">{{ mode === 'login' ? '还没有账号？' : '已有账号？' }}</text>
            <text class="ml-1 text-blue-500 font-medium" @tap="toggleMode">
              {{ mode === 'login' ? '立即注册' : '返回登录' }}
            </text>
          </view>

          <!-- 协议勾选 -->
          <view class="flex items-center justify-center py-2">
            <wd-checkbox v-model="model.read" shape="square" custom-class="!flex !items-center">
              <view class="flex items-center text-xs">
                <text class="text-gray-400">我已阅读并同意</text>
                <text class="text-blue-500 font-medium" @tap.stop="handleProtocolClick">《用户协议与隐私条款》</text>
              </view>
            </wd-checkbox>
          </view>
        </view>
      </view>
    </view>

    <!-- 协议弹出层 -->
    <wd-popup
      v-model="protocolShow"
      position="bottom"
      round
      custom-class="rounded-t-3xl"
      @close="handleClose"
    >
      <view class="p-8 pb-12">
        <view class="mb-4 text-center text-lg text-gray-900 font-bold">用户协议</view>
        <view class="text-sm text-gray-500 leading-relaxed space-y-3">
          <view class="text-gray-900 font-medium">
            《光影秘匣》 是一款由 AI 辅助开发的云端图片管理工具，支持 PC 网页端
            <text class="mx-1 text-blue-500 underline active:opacity-60" @tap="handleCopyUrl">（https://personal-navhub.site）</text>
            与小程序端同步。本应用致力于提供安全、便捷的图片存储与智能管理体验。
          </view>
          <view class="text-xs space-y-1">
            <view>1. 本小程序仅作为技术工具，不对用户上传内容的合法性负责。</view>
            <view>2. 用户严禁上传违反法律法规、危害国家安全 or 侵犯他人版权的内容。</view>
            <view>3. 应用处于持续完善阶段，建议用户对重要数据自行备份，开发者不对因不可抗力导致的数据丢失承担赔偿责任。</view>
            <view>4. 一经发现违规内容，开发者有权在不通知的情况下采取删除、封禁账号等措施。</view>
          </view>
        </view>
        <view class="mt-8">
          <wd-button block @tap="handleClose">我知道了</wd-button>
        </view>
      </view>
    </wd-popup>
  </view>
</template>
