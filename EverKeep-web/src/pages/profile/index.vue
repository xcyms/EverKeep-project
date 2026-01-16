<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useUserStore } from '../../store/user'
import { updateProfileApi, updatePasswordApi, logoutApi } from '../../api/user'
import { useRouter } from 'vue-router'
import { getImageUrl } from '../../utils/common'

const userStore = useUserStore()
const router = useRouter()
const activeKey = ref('1')
const loading = ref(false)

const uploadUrl = `${import.meta.env.VITE_API_BASE_URL}/file/upload`

// 个人资料表单
const profileForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})

// 初始化表单
onMounted(() => {
  const { avatar } = userStore.userInfo
  const nickname = (userStore.userInfo as any).nickname || ''
  const email = (userStore.userInfo as any).email || ''
  const phone = (userStore.userInfo as any).phone || ''
  profileForm.nickname = nickname || ''
  profileForm.email = email || ''
  profileForm.phone = phone || ''
  profileForm.avatar = avatar || ''
})

// 修改密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const handleUpdateProfile = async () => {
  loading.value = true
  try {
    await updateProfileApi(profileForm)
    // 更新本地存储
    userStore.login({
      ...userStore.userInfo,
      ...profileForm
    })
    message.success('个人资料更新成功')
  } catch (error) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

const handleUpdatePassword = async () => {
  if (!passwordForm.oldPassword) {
    message.error('请输入当前密码')
    return
  }
  if (!passwordForm.newPassword) {
    message.error('请输入新密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  
  loading.value = true
  try {
    await updatePasswordApi({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    message.success('密码修改成功，请重新登录')
    // 退出登录
    await logoutApi()
    userStore.logout()
    router.replace('/login')
  } catch (error) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

const handleAvatarChange = (info: any) => {
  if (info.file.status === 'uploading') {
    loading.value = true
    return
  }
  if (info.file.status === 'done') {
    loading.value = false
    const response = info.file.response
    if (response.code === 200) {
      profileForm.avatar = response.data.url
      handleUpdateProfile() // 自动保存头像更新
    } else {
      message.error(response.message || '头像上传失败')
    }
  } else if (info.file.status === 'error') {
    loading.value = false
    message.error('网络错误，头像上传失败')
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto py-8">
    <h2 class="text-2xl font-bold mb-8">个人中心</h2>
    
    <div class="bg-white rounded-lg shadow-sm p-6">
      <a-tabs v-model:activeKey="activeKey">
        <!-- 个人资料 -->
        <a-tab-pane key="1" tab="基本资料">
          <div class="max-w-lg mt-6">
            <a-form layout="vertical" :model="profileForm">
              <a-form-item label="昵称">
                <a-input v-model:value="profileForm.nickname" placeholder="请输入昵称" />
              </a-form-item>
              <a-form-item label="邮箱">
                <a-input v-model:value="profileForm.email" placeholder="请输入邮箱" />
              </a-form-item>
              <a-form-item label="手机号">
                <a-input v-model:value="profileForm.phone" placeholder="请输入手机号" />
              </a-form-item>
              <a-form-item>
                <a-button type="primary" :loading="loading" @click="handleUpdateProfile">保存更改</a-button>
              </a-form-item>
            </a-form>
          </div>
        </a-tab-pane>

        <!-- 头像设置 -->
        <a-tab-pane key="2" tab="头像设置">
          <div class="flex flex-col items-center py-10">
            <a-avatar :size="120" :src="getImageUrl(profileForm.avatar || userStore.avatar)" class="mb-6 shadow-md" />
            <a-upload
              name="file"
              :action="uploadUrl"
              :data="{ category: 'avatar' }"
              :show-upload-list="false"
              :headers="{ Authorization: `Bearer ${userStore.token}` }"
              @change="handleAvatarChange"
            >
              <a-button :loading="loading">
                <template #icon><div class="i-fa6-solid:upload mr-2 inline-block" /></template>
                更换头像
              </a-button>
            </a-upload>
            <p class="mt-4 text-gray-400 text-sm">支持 JPG、PNG 格式，大小不超过 2MB</p>
          </div>
        </a-tab-pane>

        <!-- 安全设置 -->
        <a-tab-pane key="3" tab="安全设置">
          <div class="max-w-lg mt-6">
            <h3 class="text-lg font-medium mb-6">修改密码</h3>
            <a-form layout="vertical" :model="passwordForm">
              <a-form-item label="当前密码">
                <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入当前密码" />
              </a-form-item>
              <a-form-item label="新密码">
                <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
              </a-form-item>
              <a-form-item label="确认新密码">
                <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
              </a-form-item>
              <a-form-item>
                <a-button type="primary" danger :loading="loading" @click="handleUpdatePassword">修改密码</a-button>
              </a-form-item>
            </a-form>
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<style scoped>
:deep(.ant-tabs-nav-wrap) {
  padding-bottom: 8px;
}
</style>