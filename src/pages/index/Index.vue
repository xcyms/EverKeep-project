<script setup lang="ts">
const stats = [
  { title: '总访问量', value: '12,489', icon: 'i-ant-design:eye-outlined', color: 'text-blue-500' },
  { title: '新增用户', value: '562', icon: 'i-ant-design:user-add-outlined', color: 'text-green-500' },
  { title: '待办任务', value: '18', icon: 'i-ant-design:schedule-outlined', color: 'text-orange-500' },
  { title: '系统预警', value: '2', icon: 'i-ant-design:warning-outlined', color: 'text-red-500' },
]
</script>

<template>
  <div class="space-y-6">
    <!-- 欢迎栏 -->
    <div class="bg-white p-8 rounded-xl shadow-sm border border-gray-100 flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">早安，Lewis！</h2>
        <p class="text-gray-400 mt-1">今天是你加入 EverKeep 的第 128 天，系统运行状况良好。</p>
      </div>
    </div>

    <!-- 统计卡片：PC 端四列布局 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div v-for="s in stats" :key="s.title" class="bg-white p-6 rounded-xl shadow-sm border border-gray-50 hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-gray-400 text-[14px]">{{ s.title }}</div>
            <div class="text-2xl font-bold mt-1">{{ s.value }}</div>
          </div>
          <div :class="[s.icon, s.color, 'text-3xl']" />
        </div>
      </div>
    </div>

    <!-- 内容区：PC 端左右分栏 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 bg-white p-6 rounded-xl shadow-sm min-h-400px">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-[14px] font-bold">流量趋势</h3>
          <a-radio-group size="small" default-value="7d">
            <a-radio-button value="7d">近7天</a-radio-button>
            <a-radio-button value="30d">近30天</a-radio-button>
          </a-radio-group>
        </div>
        <div class="flex items-center justify-center h-full text-gray-300 italic">
          此处可以集成 ECharts 图表...
        </div>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm">
        <h3 class="text-[14px] font-bold mb-6">活跃公告</h3>
        <a-list item-layout="horizontal">
          <a-list-item v-for="i in 5" :key="i">
            <a-list-item-meta description="2026-01-08 10:00">
              <template #title>
                <a href="#" class="text-[14px] font-medium hover:text-blue-500">系统维护公告 v2.0.{{ i }}</a>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </a-list>
      </div>
    </div>
  </div>
</template>