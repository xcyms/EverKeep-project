<script setup lang="ts">
import { message } from 'ant-design-vue'
import { useUserStore } from '../../store/user'
import { loginApi, registerApi } from '../../api/user'

const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(true)
const loading = ref(false)

const formState = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  remember: true,
})

const toggleMode = () => {
  isLogin.value = !isLogin.value
  formState.username = ''
  formState.password = ''
  formState.confirmPassword = ''
}

const handleSubmit = async () => {
  if (!formState.username || !formState.password) {
    return message.warning('请输入用户名和密码')
  }

  if (!isLogin.value && formState.password !== formState.confirmPassword) {
    return message.warning('两次输入的密码不一致')
  }

  loading.value = true
  try {
    if (isLogin.value) {
      const token = await loginApi({
        username: formState.username,
        password: formState.password
      })
      userStore.login({
        username: formState.username,
        token: token
      })
      message.success('登录成功')
      router.push('/')
    } else {
      await registerApi({
        username: formState.username,
        password: formState.password
      })
      message.success('注册成功，请登录')
      isLogin.value = true
    }
  } catch (error) {
    // 错误已经在 request.ts 拦截器中处理并提示
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center p-4">
    <div class="max-w-md w-full bg-white rounded-xl shadow-lg p-8">
      <div class="text-center mb-8">
        <div class="i-ant-design:appstore-twotone text-5xl mb-2 inline-block" />
        <h1 class="text-2xl font-bold text-gray-800">{{ isLogin ? '欢迎回来' : '创建账号' }}</h1>
        <p class="text-gray-500 text-sm">{{ isLogin ? '请登录您的 EverKeep 账号' : '开启您的 EverKeep 之旅' }}</p>
      </div>

      <a-form layout="vertical" :model="formState" @finish="handleSubmit">
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="formState.username" placeholder="请输入用户名">
            <template #prefix><div class="i-ant-design:user-outlined text-gray-400" /></template>
          </a-input>
        </a-form-item>

        <a-form-item label="密码" name="password">
          <a-input-password v-model:value="formState.password" placeholder="请输入密码">
            <template #prefix><div class="i-ant-design:lock-outlined text-gray-400" /></template>
          </a-input-password>
        </a-form-item>

        <a-form-item v-if="!isLogin" label="确认密码" name="confirmPassword">
          <a-input-password v-model:value="formState.confirmPassword" placeholder="请再次输入密码">
            <template #prefix><div class="i-ant-design:lock-outlined text-gray-400" /></template>
          </a-input-password>
        </a-form-item>

        <div v-if="isLogin" class="flex items-center justify-between mb-6">
          <a-checkbox v-model:checked="formState.remember">记住我</a-checkbox>
          <a class="text-blue-500 text-sm hover:underline cursor-pointer">忘记密码？</a>
        </div>

        <a-button 
          type="primary" 
          html-type="submit" 
          block 
          size="large" 
          :loading="loading"
          class="h-11 rounded-lg"
        >
          {{ isLogin ? '登 录' : '注 册' }}
        </a-button>
      </a-form>
      
      <div class="mt-8 text-center text-sm text-gray-500">
        {{ isLogin ? '还没有账号？' : '已有账号？' }}
        <a class="text-blue-500 font-medium hover:underline cursor-pointer" @click="toggleMode">
          {{ isLogin ? '立即注册' : '立即登录' }}
        </a>
      </div>
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  layout: false
</route>