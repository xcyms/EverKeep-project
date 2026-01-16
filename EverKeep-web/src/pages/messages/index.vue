<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { getMessagePageApi, saveMessageApi, updateMessageApi, deleteMessageApi } from '../../api/message'
import { useUserStore } from '../../store/user'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import type { API } from '../../types'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo.roles?.includes('ADMIN'))

const loading = ref(false)
const dataSource = ref<API.Message[]>([])
const total = ref(0)
const queryParam = reactive({
  current: 1,
  size: 10,
  title: ''
})

// 表单相关
const modalVisible = ref(false)
const modalTitle = ref('发送消息')
const confirmLoading = ref(false)
const formRef = ref()
const formState = reactive<Partial<API.Message>>({
  id: undefined,
  title: '',
  content: '',
  type: 'info'
})

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '内容', dataIndex: 'content', key: 'content', ellipsis: true },
  { title: '类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '状态', dataIndex: 'readFlag', key: 'readFlag', width: 100 },
  { title: '发送时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 150 }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMessagePageApi(queryParam)
    dataSource.value = res.records
    total.value = Number(res.total || 0)
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParam.current = 1
  loadData()
}

const handleReset = () => {
  queryParam.title = ''
  queryParam.current = 1
  loadData()
}

const handleTableChange = (pagination: any) => {
  queryParam.current = pagination.current
  queryParam.size = pagination.pageSize
  loadData()
}

const handleAdd = () => {
  modalTitle.value = '发送消息'
  formState.id = undefined
  formState.title = ''
  formState.content = ''
  formState.type = 'info'
  modalVisible.value = true
}

const handleEdit = (record: API.Message) => {
  modalTitle.value = '编辑消息'
  Object.assign(formState, record)
  modalVisible.value = true
}

const handleDelete = (id: number) => {
  Modal.confirm({
    title: '确定要删除这条消息吗？',
    content: '删除后用户将无法在消息中心看到此消息。',
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        await deleteMessageApi(id)
        message.success('删除成功')
        loadData()
      } catch (error) {}
    }
  })
}

const handleModalOk = () => {
  formRef.value.validate().then(async () => {
    confirmLoading.value = true
    try {
      if (formState.id) {
        await updateMessageApi(formState as API.Message)
        message.success('修改成功')
      } else {
        await saveMessageApi(formState)
        message.success('发送成功')
      }
      modalVisible.value = false
      loadData()
    } catch (error) {
    } finally {
      confirmLoading.value = false
    }
  })
}

onMounted(loadData)
</script>

<template>
  <div class="max-w-6xl mx-auto py-6">
    <div class="flex items-center justify-between mb-8">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">消息管理</h2>
        <p class="text-gray-500 mt-1 text-sm">向系统用户发送通知、提醒或系统更新消息。</p>
      </div>
      <a-button type="primary" @click="handleAdd" v-if="isAdmin" class="flex items-center gap-1.5">
        <template #icon><div class="i-fa6-solid:plus text-xs" /></template>
        发送消息
      </a-button>
    </div>

    <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
      <div class="flex items-center gap-4 mb-6">
        <a-input
          v-model:value="queryParam.title"
          placeholder="搜索消息标题"
          class="w-64"
          @press-enter="handleSearch"
        >
          <template #prefix><div class="i-fa6-solid:magnifying-glass text-gray-300" /></template>
        </a-input>
        <a-button @click="handleSearch">查询</a-button>
        <a-button @click="handleReset">重置</a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="{
          current: queryParam.current,
          pageSize: queryParam.size,
          total: total,
          showSizeChanger: true,
          showTotal: (total: number) => `共 ${total} 条`
        }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag :color="record.type === 'info' ? 'blue' : record.type === 'success' ? 'green' : record.type === 'warning' ? 'orange' : 'red'">
              {{ record.type.toUpperCase() }}
            </a-tag>
          </template>
          <template v-if="column.key === 'readFlag'">
            <a-badge :status="record.readFlag.code === 1 ? 'default' : 'processing'" :text="record.readFlag.code === 1 ? record.readFlag.desc : '未读'" />
          </template>
          <template v-if="column.key === 'createTime'">
            {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
          </template>
          <template v-if="column.key === 'action'">
            <div class="flex gap-2">
              <a-button type="link" size="small" @click="handleEdit(record as API.Message)" v-if="isAdmin">编辑</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record.id)" v-if="isAdmin">删除</a-button>
              <span v-if="!isAdmin">-</span>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="confirmLoading"
      @ok="handleModalOk"
    >
      <a-form
        ref="formRef"
        :model="formState"
        layout="vertical"
        :rules="{
          title: [{ required: true, message: '请输入标题' }],
          content: [{ required: true, message: '请输入内容' }],
          type: [{ required: true, message: '请选择类型' }]
        }"
      >
        <a-form-item label="消息标题" name="title">
          <a-input v-model:value="formState.title" placeholder="请输入标题" />
        </a-form-item>
        <a-form-item label="消息类型" name="type">
          <a-select v-model:value="formState.type">
            <a-select-option value="info">信息 (Info)</a-select-option>
            <a-select-option value="success">成功 (Success)</a-select-option>
            <a-select-option value="warning">警告 (Warning)</a-select-option>
            <a-select-option value="error">错误 (Error)</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="消息内容" name="content">
          <a-textarea v-model:value="formState.content" placeholder="请输入详细内容" :rows="6" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>