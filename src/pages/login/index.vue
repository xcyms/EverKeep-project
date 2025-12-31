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
const loginType = ref<'account' | 'phone'>('account') // 'account' for username/password, 'phone' for phone/code
const textType = ref<'primary' | 'default'>('primary')
const text = ref('获取验证码')
const isCountingDown = ref(false)
const statusBarHeight = ref(0) // 获取状态栏高度
const protocolShow = ref(false) // 协议弹窗显示状态

const model = reactive<{
  username: string
  password: string
  code: string
  read: boolean
}>({
  username: '',
  password: '',
  code: '',
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

function handleClose() {
  protocolShow.value = false
}

const { loading: loginLoading, send: login } = useRequest(
  (email: string, password: string) => Apis.lsky.login({
    data: {
      email,
      password,
    },
  }),
  {
    immediate: false,
  },
)

async function handleSubmit() {
  if (!model.username) {
    showError({
      msg: loginType.value === 'account' ? '请输入用户名' : '请输入手机号'
    })
    return
  }
  if (loginType.value === 'account' && !model.password) {
    showError({
      msg: '请输入密码'
    })
    return
  }
  if (loginType.value === 'phone' && !model.code) {
    showError({
      msg: '请输入验证码'
    })
    return
  }
  if (!model.read) {
    showError({
      msg: '请先勾选协议'
    })
    return
  }
  if (loginType.value === 'phone') {
    showError({
      msg: '手机号登录暂未开放'
    })
    return
  }
  const res = await login(model.username, model.password)
  if (res.status) {
    showSuccess({
      msg: '登录成功'
    })
    user.login({
      email: model.username,
    }, res.data.token)
    router.pushTab({
      name: 'home'
    })
  } else {
    showError({
      msg: res.message || '登录失败'
    })
  }
}

function toggleLoginType() {
  loginType.value = loginType.value === 'account' ? 'phone' : 'account'
}
function getCode() {
  if (!model.username) {
    showError({
      msg: '请输入手机号'
    })
    return
  }
  textType.value = 'default'
  showSuccess({
    msg: '验证码已发送'
  })
  isCountingDown.value = true
  let countDown = 60;
    text.value = `${countDown}秒后重新获取`;
    const timer = setInterval(() => {
      if (countDown > 1) {
        countDown--;
        text.value = `${countDown}秒后重新获取`;
      } else {
        clearInterval(timer);
        textType.value = 'primary'
        text.value = '获取验证码';
        isCountingDown.value = false
      }
    }, 1000);
}
</script>

<template>
  <view class="relative min-h-screen w-full flex flex-col overflow-hidden bg-[#f8f9fa]">
    <!-- 装饰背景 -->
    <view class="absolute right-[-10%] top-[-5%] h-[400rpx] w-[400rpx] rounded-full bg-blue-400/10 blur-[80px]" />
    <view class="absolute bottom-[-10%] left-[-10%] h-[500rpx] w-[500rpx] rounded-full bg-indigo-400/10 blur-[100px]" />

    <!-- 顶部标题区 -->
    <view class="px-8 pb-10 pt-24" :style="{ paddingTop: `${statusBarHeight + 40}px` }">
      <view class="text-3xl text-gray-900 font-bold tracking-tight">欢迎回来</view>
      <view class="mt-2 text-sm text-gray-400">请登录以管理您的云端资源</view>
    </view>

    <!-- 登录卡片 -->
    <view class="flex-1 px-6">
      <view class="overflow-hidden rounded-3xl bg-white/70 p-8 shadow-[0_8px_30px_rgb(0,0,0,0.04)] backdrop-blur-xl">
        <!-- 登录类型切换 -->
        <view class="relative mb-8 h-12 flex items-center rounded-xl bg-gray-100/80 p-1" @click="toggleLoginType">
          <view class="z-10 flex flex-1 items-center justify-center text-sm font-medium transition-colors duration-300" :class="{ 'text-white': loginType === 'account', 'text-gray-500': loginType !== 'account' }">账号登录</view>
          <view class="z-10 flex flex-1 items-center justify-center text-sm font-medium transition-colors duration-300" :class="{ 'text-white': loginType === 'phone', 'text-gray-500': loginType !== 'phone' }">验证码登录</view>
          <view class="absolute bottom-1 left-1 top-1 w-[calc(50%-4px)] transform-gpu rounded-lg bg-blue-500 shadow-sm transition-transform duration-300" :class="{ 'translate-x-full': loginType === 'phone' }"/>
        </view>

        <!-- 表单区域 -->
        <view class="space-y-6">
          <view>
            <view class="mb-2 ml-1 text-xs text-gray-400 font-medium tracking-wide uppercase">{{ loginType === 'account' ? '用户名 / 邮箱' : '手机号码' }}</view>
            <wd-input
              v-model="model.username"
              :placeholder="loginType === 'account' ? '请输入账号' : '请输入手机号'"
              no-border
              custom-class="!bg-gray-50/80 !rounded-xl !py-3 !px-4 border border-gray-100"
            >
              <template #prefix>
                <wd-icon :name="loginType === 'account' ? 'user' : 'mobile'" size="18px" color="#94a3b8" class="mr-2" />
              </template>
            </wd-input>
          </view>

          <view v-if="loginType === 'account'">
            <view class="mb-2 ml-1 text-xs text-gray-400 font-medium tracking-wide uppercase">登录密码</view>
            <wd-input
              v-model="model.password"
              placeholder="请输入密码"
              type="password"
              no-border
              show-password
              custom-class="!bg-gray-50/80 !rounded-xl !py-3 !px-4 border border-gray-100"
            >
              <template #prefix>
                <wd-icon name="lock-on" size="18px" color="#94a3b8" class="mr-2" />
              </template>
            </wd-input>
          </view>

          <view v-if="loginType === 'phone'">
            <view class="mb-2 ml-1 text-xs text-gray-400 font-medium tracking-wide uppercase">短信验证码</view>
            <wd-input
              v-model="model.code"
              placeholder="请输入验证码"
              no-border
              custom-class="!bg-gray-50/80 !rounded-xl !py-3 !px-4 border border-gray-100"
            >
              <template #prefix>
                <wd-icon name="chat-error" size="18px" color="#94a3b8" class="mr-2" />
              </template>
              <template #suffix>
                <view class="text-sm font-medium transition-opacity active:opacity-60" :class="isCountingDown ? 'text-gray-300' : 'text-blue-500'" @tap="!isCountingDown && getCode()">
                  {{ text }}
                </view>
              </template>
            </wd-input>
          </view>

          <!-- 登录按钮 -->
          <view class="pt-4">
            <wd-button
              block
              size="large"
              custom-class="!rounded-xl !bg-blue-500 !h-12 !text-lg !font-bold shadow-lg shadow-blue-500/20 active:opacity-90"
              @click="handleSubmit"
              :loading="loginLoading"
            >
              立即登录
            </wd-button>
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
        <view class="text-sm text-gray-500 leading-relaxed">
          使用本小程序功能请遵守相关法律法规。本应用作为 Lsky Pro 的移动端管理工具，所有数据存储于您的私有服务器。如您不同意本协议，请立即关闭小程序并停止使用。
        </view>
        <view class="mt-8">
          <wd-button block @tap="handleClose">我知道了</wd-button>
        </view>
      </view>
    </wd-popup>
  </view>
</template>
