<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { getNoticePageApi, saveNoticeApi, updateNoticeApi, deleteNoticeApi } from '../../api/notice'
import { useUserStore } from '../../store/user'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import type { API } from '../../types'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo.roles?.includes('ADMIN'))

const loading = ref(false)
const dataSource = ref<API.Notice[]>([])
const total = ref(0)
const queryParam = ref({
  current: 1,
  size: 10,
  title: ''
})

// 表单相关
const modalVisible = ref(false)
const modalTitle = ref('新增公告')
const confirmLoading = ref(false)
const formRef = ref()
const formState = reactive<Partial<API.Notice>>({
  id: undefined,
  title: '',
  content: '',
  tag: '通知',
  color: 'blue',
  sort: 0
})

// 详情相关
const detailVisible = ref(false)
const currentNotice = ref<Partial<API.Notice>>({})

const handleView = (record: API.Notice) => {
  currentNotice.value = record
  detailVisible.value = true
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getNoticePageApi(queryParam.value)
    dataSource.value = res.records
    // 强制转换为数字类型，解决 Expected Number, got String 警告
    total.value = Number(res.total || 0)
  } catch (error) {
    console.error('加载公告失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  modalTitle.value = '新增公告'
  Object.assign(formState, {
    id: undefined,
    title: '',
    content: '',
    tag: '通知',
    color: 'blue',
    sort: 0
  })
  modalVisible.value = true
}

const handleEdit = (record: API.Notice) => {
  modalTitle.value = '编辑公告'
  Object.assign(formState, { ...record })
  modalVisible.value = true
}

const handleDelete = (id: number) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这条公告吗？',
    onOk: async () => {
      try {
        await deleteNoticeApi(id)
        message.success('删除成功')
        loadData()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

const handleModalOk = async () => {
  try {
    await formRef.value.validateFields()
    confirmLoading.value = true
    if (formState.id) {
      await updateNoticeApi(formState as API.Notice)
      message.success('更新成功')
    } else {
      await saveNoticeApi(formState as API.Notice)
      message.success('新增成功')
    }
    modalVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    confirmLoading.value = false
  }
}

const handleTableChange = (pagination: any) => {
  queryParam.value.current = pagination.current
  queryParam.value.size = pagination.pageSize
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="p-6 space-y-6">
    <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-xl font-bold text-gray-800">系统公告</h2>
        <div class="flex gap-4">
          <a-input-search
            v-model:value="queryParam.title"
            placeholder="搜索公告标题"
            style="width: 250px"
            @search="loadData"
          />
          <a-button type="primary" @click="handleAdd" v-if="isAdmin">新增公告</a-button>
          <a-button @click="loadData">刷新</a-button>
        </div>
      </div>

      <a-table
        :columns="[
          { title: '标题', dataIndex: 'title', key: 'title' },
          { title: '内容', dataIndex: 'content', key: 'content' },
          { title: '类型', dataIndex: 'tag', key: 'tag', width: 100 },
          { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
          { title: '发布时间', dataIndex: 'createTime', key: 'createTime', width: 200 },
          { title: '操作', key: 'action', width: 150, fixed: 'right' },
        ]"
        :data-source="dataSource"
        :pagination="{
          current: queryParam.current,
          pageSize: queryParam.size,
          total: total,
          showSizeChanger: true,
          showTotal: (total: number) => `共 ${total} 条`
        }"
        :loading="loading"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <div class="flex items-center gap-2">
              <span class="font-medium hover:text-blue-500 cursor-pointer" @click="handleView(record as API.Notice)">{{ record.title }}</span>
            </div>
          </template>
          <template v-if="column.key === 'tag'">
            <a-tag :color="record.color">{{ record.tag }}</a-tag>
          </template>
          <template v-if="column.key === 'createTime'">
            {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
          </template>
          <template v-if="column.key === 'action'">
            <div class="flex gap-2">
              <a-button type="link" size="small" @click="handleEdit(record as API.Notice)" v-if="isAdmin">编辑</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record.id)" v-if="isAdmin">删除</a-button>
              <span v-if="!isAdmin">-</span>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:visible="modalVisible"
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
          tag: [{ required: true, message: '请输入标签' }],
          sort: [{ required: true, message: '请输入排序' }]
        }"
      >
        <a-form-item label="标题" name="title">
          <a-input v-model:value="formState.title" placeholder="请输入公告标题" />
        </a-form-item>
        <a-form-item label="内容" name="content">
          <a-textarea v-model:value="formState.content" placeholder="请输入公告内容" :rows="4" />
        </a-form-item>
        <div class="grid grid-cols-2 gap-4">
          <a-form-item label="标签" name="tag">
            <a-input v-model:value="formState.tag" placeholder="如：通知、维护" />
          </a-form-item>
          <a-form-item label="标签颜色" name="color">
            <a-select v-model:value="formState.color" placeholder="请选择颜色">
              <a-select-option value="blue">蓝色</a-select-option>
              <a-select-option value="green">绿色</a-select-option>
              <a-select-option value="orange">橙色</a-select-option>
              <a-select-option value="red">红色</a-select-option>
              <a-select-option value="purple">紫色</a-select-option>
            </a-select>
          </a-form-item>
        </div>
        <a-form-item label="排序" name="sort">
          <a-input-number v-model:value="formState.sort" :min="0" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 公告详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      :title="currentNotice.title"
      :footer="null"
      width="600px"
    >
      <div class="space-y-4">
        <div class="flex items-center gap-2 text-gray-400 text-xs">
          <a-tag :color="currentNotice.color">{{ currentNotice.tag }}</a-tag>
          <span>发布于 {{ dayjs(currentNotice.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
        </div>
        <div class="bg-gray-50 p-4 rounded-lg text-gray-700 leading-relaxed whitespace-pre-wrap min-h-[200px]">
          {{ currentNotice.content || '暂无内容' }}
        </div>
      </div>
    </a-modal>
  </div>
</template>