<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getUserListApi } from '../../api/user'
import { getUserConfigsApi, updateConfigApi } from '../../api/config'
import type { API } from '../../types'
import { getImageUrl } from '../../utils/common'

const loading = ref(false)
const userList = ref<API.PageResult<API.User>>({
  records: [],
  total: 0,
  size: 0,
  current: 0
})
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)

// 抽屉状态
const drawerVisible = ref(false)
const drawerLoading = ref(false)
const currentUser = ref<API.User | null>(null)
const userConfigs = ref<API.SysConfig[]>([])

// 搜索表单
const searchForm = ref({
  username: '',
  nickname: ''
})

// 加载用户列表
const loadUsers = async (page = current.value, size = pageSize.value) => {
  loading.value = true
  try {
    const res = await getUserListApi(
      { current: Number(page), size: Number(size) },
      { username: searchForm.value.username, nickname: searchForm.value.nickname }
    )
    userList.value = res
    total.value = Number(res.total)
    current.value = Number(res.current)
    pageSize.value = Number(res.size)
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  current.value = 1
  loadUsers()
}

const handleReset = () => {
  searchForm.value = {
    username: '',
    nickname: ''
  }
  current.value = 1
  loadUsers()
}

const handleTableChange = (pagination: any) => {
  loadUsers(pagination.current, pagination.pageSize)
}

// 打开配置抽屉
const handleOpenConfigs = async (user: API.User) => {
  currentUser.value = user
  drawerVisible.value = true
  drawerLoading.value = true
  try {
    const configs = await getUserConfigsApi(user.id)
    // 过滤掉只能由管理员在系统设置中配置的全局参数
    // 比如 upload_path (默认上传路径) 不允许在用户层级修改
    const systemOnlyKeys = ['upload_path']
    userConfigs.value = configs.filter(item => !systemOnlyKeys.includes(item.configKey))
  } catch (error) {
    console.error('加载用户配置失败:', error)
  } finally {
    drawerLoading.value = false
  }
}

// 保存用户特定配置
const handleSaveConfig = async (config: API.SysConfig) => {
  try {
    // 确保更新的是该用户的配置
    config.userId = currentUser.value?.id
    await updateConfigApi(config)
    message.success(`用户 [${currentUser.value?.username}] 的配置已更新`)
  } catch (error) {
    // 拦截器已处理
  }
}

onMounted(loadUsers)
</script>

<template>
  <div class="max-w-6xl mx-auto py-6">
    <div class="flex items-center justify-between mb-8">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">用户管理</h2>
        <p class="text-gray-500 mt-1 text-sm">查看系统注册用户，并为其配置个性化的存储策略和权限。</p>
      </div>
      <div class="flex items-center gap-3">
        <a-button @click="handleReset" class="flex items-center gap-1.5">
          重置
        </a-button>
        <a-button type="primary" @click="handleSearch" :loading="loading" class="flex items-center gap-1.5">
          <template #icon><span class="i-fa6-solid:magnifying-glass text-[13px]" /></template>
          搜索
        </a-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="bg-white p-6 rounded-xl border border-gray-100 shadow-sm mb-6">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="用户名">
          <a-input v-model:value="searchForm.username" placeholder="请输入用户名" allow-clear @pressEnter="handleSearch" />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="searchForm.nickname" placeholder="请输入用户昵称" allow-clear @pressEnter="handleSearch" />
        </a-form-item>
      </a-form>
    </div>

    <!-- 用户表格 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <a-table 
        :dataSource="userList.records" 
        :loading="loading" 
        :pagination="{
          current: current,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条记录`
        }"
        @change="handleTableChange"
        rowKey="id"
      >
        <a-table-column title="用户ID" dataIndex="id" key="id" width="80" />
        <a-table-column title="用户信息" key="user">
          <template #default="{ record }">
            <div class="flex items-center gap-3">
              <a-avatar :src="getImageUrl(record.avatar)" :size="40" class="flex-shrink-0">
                <template #icon><div class="i-fa6-solid:user" /></template>
              </a-avatar>
              <div class="flex flex-col">
                <span class="font-bold text-gray-800">{{ record.nickname }}</span>
                <span class="text-xs text-gray-400">@{{ record.username }}</span>
              </div>
            </div>
          </template>
        </a-table-column>

        <a-table-column title="角色" dataIndex="roles" key="roles">
          <template #default="{ text }">
            <div class="flex gap-1">
              <a-tag v-for="role in text" :key="role" :color="role === 'ADMIN' ? 'red' : 'blue'">
                {{ role }}
              </a-tag>
            </div>
          </template>
        </a-table-column>

        <a-table-column title="联系方式" key="contact">
          <template #default="{ record }">
            <div class="text-xs space-y-1">
              <div class="flex items-center gap-1.5 text-gray-500">
                <span class="i-fa6-solid:envelope text-[10px]" /> {{ record.email || '-' }}
              </div>
              <div class="flex items-center gap-1.5 text-gray-400">
                <span class="i-fa6-solid:phone text-[10px]" /> {{ record.phone || '-' }}
              </div>
            </div>
          </template>
        </a-table-column>

        <a-table-column title="操作" key="action" align="right">
          <template #default="{ record }">
            <a-button type="link" @click="handleOpenConfigs(record)">
              <template #icon><span class="i-fa6-solid:gear mr-1" /></template>
              配置管理
            </a-button>
          </template>
        </a-table-column>
      </a-table>
    </div>

    <!-- 配置管理抽屉 -->
    <a-drawer
      v-model:open="drawerVisible"
      :title="`配置管理 - ${currentUser?.nickname}`"
      placement="right"
      width="500"
    >
      <div v-if="drawerLoading" class="py-20 text-center">
        <a-spin tip="正在拉取用户个性化配置..." />
      </div>
      <div v-else class="space-y-6">
        <a-alert type="info" show-icon class="mb-4">
          <template #message>
            <div class="text-xs">
              在此修改的配置将仅对该用户生效。例如设置 <b>用户上传子目录</b> (<code>user_upload_dir</code>) 后，该用户的文件将存储在独立文件夹下。
            </div>
          </template>
        </a-alert>
        
        <div v-for="item in userConfigs" :key="item.configKey" class="p-4 rounded-lg border border-gray-100 bg-gray-50/30 space-y-3">
          <div class="flex items-center justify-between">
            <div class="flex flex-col">
              <span class="font-bold text-gray-700">{{ item.configName }}</span>
              <code class="text-[10px] text-blue-500">{{ item.configKey }}</code>
            </div>
            <a-button type="primary" size="small" @click="handleSaveConfig(item)">
              应用设置
            </a-button>
          </div>
          
          <a-input 
            v-model:value="item.configValue" 
            placeholder="请输入配置值"
            class="font-mono"
          >
            <template #prefix v-if="item.configKey === 'user_upload_dir'">
              <span class="i-fa6-solid:folder-open text-gray-400 mr-1" />
            </template>
          </a-input>
          
          <p class="text-[11px] text-gray-400 mb-0" v-if="item.remark">
            {{ item.remark }}
          </p>
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<style scoped>
:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa;
  font-size: 13px;
}
</style>