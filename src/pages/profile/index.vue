<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'

const activeKey = ref('1')

// 个人资料表单
const profileForm = reactive({
  nickname: 'Lewis',
  email: 'lewis@example.com',
  phone: '13800138000',
  bio: '保持热爱，奔赴山海。'
})

// 修改密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const handleUpdateProfile = () => {
  message.success('个人资料更新成功')
}

const handleUpdatePassword = () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  message.success('密码修改成功，请重新登录')
}

const handleAvatarChange = (info: any) => {
  if (info.file.status === 'done') {
    message.success(`${info.file.name} 上传成功`)
  } else if (info.file.status === 'error') {
    message.error(`${info.file.name} 上传失败`)
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
              <a-form-item label="个人简介">
                <a-textarea v-model:value="profileForm.bio" :rows="4" placeholder="向大家介绍一下自己吧" />
              </a-form-item>
              <a-form-item>
                <a-button type="primary" @click="handleUpdateProfile">保存更改</a-button>
              </a-form-item>
            </a-form>
          </div>
        </a-tab-pane>

        <!-- 头像设置 -->
        <a-tab-pane key="2" tab="头像设置">
          <div class="flex flex-col items-center py-10">
            <a-avatar :size="120" src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" class="mb-6 shadow-md" />
            <a-upload
              name="avatar"
              action="/api/upload"
              :show-upload-list="false"
              @change="handleAvatarChange"
            >
              <a-button>
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
                <a-button type="primary" danger @click="handleUpdatePassword">修改密码</a-button>
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