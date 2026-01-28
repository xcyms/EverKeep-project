<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useUserStore } from '../../store/user'
import { getStatsSummaryApi } from '../../api/stats'
import { getNoticeListApi } from '../../api/notice'
import type { API } from '../../types'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { formatSize } from '../../utils/common'

const userStore = useUserStore()
const loading = ref(true)
const chartRef = ref<HTMLElement | null>(null)
let myChart: echarts.ECharts | null = null

const summary = ref<API.StatsSummary>({
  imageCount: 0,
  albumCount: 0,
  userCount: 0,
  storageUsage: 0,
  totalSize: 0,
  uploadTrend: []
})

// 公告数据
const notices = ref<API.Notice[]>([])

// 动态统计项
const stats = computed(() => [
  { title: '图片总数', value: summary.value.imageCount.toLocaleString(), icon: 'i-ant-design:picture-outlined', color: ' text-blue-500' },
  { title: '相册总数', value: summary.value.albumCount.toLocaleString(), icon: 'i-ant-design:folder-outlined', color: ' text-green-500' },
  { title: '已用空间', value: formatSize(summary.value.storageUsage), icon: 'i-ant-design:cloud-server-outlined', color: ' text-orange-500' },
  { title: '总存储空间', value: formatSize(summary.value.totalSize), icon: 'i-ant-design:hdd-outlined', color: ' text-purple-500' },
  { title: '系统用户', value: summary.value.userCount.toLocaleString(), icon: 'i-ant-design:team-outlined', color: ' text-indigo-500' },
])

// 初始化图表
const initChart = (trendData: any[]) => {
  if (!chartRef.value) return
  myChart = echarts.init(chartRef.value)
  
  // 处理后端返回的趋势数据
  const dates = trendData.map(item => item.date)
  const counts = trendData.map(item => item.count)

  const option = {
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(255, 255, 255, 0.9)', borderWeight: 0, shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.1)' },
    grid: { left: '20', right: '20', bottom: '10', top: '40', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates.length ? dates : ['无数据'],
      axisLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#999', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1, // 保证 y 轴是整数
      splitLine: { lineStyle: { type: 'dashed', color: '#f5f5f5' } },
      axisLabel: { color: '#999', fontSize: 11 }
    },
    series: [
      {
        name: '上传量',
        type: 'line',
        smooth: true,
        showSymbol: dates.length > 0,
        data: counts.length ? counts : [0],
        itemStyle: { color: '#3b82f6' },
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.2)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0)' }
          ])
        }
      }
    ]
  }
  myChart.setOption(option)
}

const loadData = async () => {
  try {
    const [statsRes, noticeRes] = await Promise.all([
      getStatsSummaryApi(),
      getNoticeListApi()
    ])
    // 强制转换数值类型，防止后端返回字符串导致 Vue 警告
    summary.value = {
      imageCount: Number(statsRes.imageCount || 0),
      albumCount: Number(statsRes.albumCount || 0),
      userCount: Number(statsRes.userCount || 0),
      storageUsage: Number(statsRes.storageUsage || 0),
      uploadTrend: statsRes.uploadTrend || [],
      totalSize: Number(statsRes.totalSize || 0),
    }
    notices.value = noticeRes
    // 在数据加载后初始化图表，传入后端趋势数据
    setTimeout(() => initChart(statsRes.uploadTrend || []), 100)
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 监听窗口缩放
const handleResize = () => myChart?.resize()

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  myChart?.dispose()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 欢迎栏 -->
    <div class="bg-white p-8 rounded-xl shadow-sm border border-gray-100 flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">你好，{{ userStore.name }}！</h2>
        <p class="text-gray-400 mt-1">欢迎回来，EverKeep 运行状况良好，随时准备为您存储精彩瞬间。</p>
      </div>
    </div>

    <!-- 统计卡片：PC 端五列布局 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
      <a-skeleton :loading="loading" active v-for="i in 5" :key="i" v-if="loading">
        <div class="bg-white p-4 rounded-xl border border-gray-50 h-28" />
      </a-skeleton>
      <div v-else v-for="s in stats" :key="s.title" class="bg-white p-4 rounded-xl shadow-sm border border-gray-50 hover:shadow-md transition-all duration-300 group">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-gray-400 text-[13px] mb-1">{{ s.title }}</div>
            <div class="text-xl font-bold tracking-tight">{{ s.value }}</div>
          </div>
          <div :class="[s.icon, s.color, 'text-2xl p-2 rounded-lg transition-transform group-hover:scale-110']" />
        </div>
      </div>
    </div>

    <!-- 内容区：PC 端左右分栏 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 bg-white p-6 rounded-xl shadow-sm min-h-[450px] flex flex-col">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-[15px] font-bold text-gray-800">存储与流量趋势</h3>
          <a-radio-group size="small" default-value="7d">
            <a-radio-button value="7d">近7天</a-radio-button>
            <a-radio-button value="30d">近30天</a-radio-button>
          </a-radio-group>
        </div>
        <div class="flex-1 w-full" ref="chartRef"></div>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-[15px] font-bold text-gray-800">系统公告</h3>
          <router-link to="/notice">
            <a-button type="link" size="small">更多</a-button>
          </router-link>
        </div>
        <a-list item-layout="horizontal" :data-source="notices">
          <template #renderItem="{ item }">
            <a-list-item class="hover:bg-gray-50 px-2 rounded-lg transition-colors cursor-pointer group border-none">
              <a-list-item-meta>
                <template #title>
                  <div class="flex items-center gap-2">
                    <a-tag :color="item.color" class="scale-90 origin-left border-none">{{ item.tag }}</a-tag>
                    <span class="text-[14px] font-medium text-gray-700 truncate group-hover:text-blue-500 transition-colors">
                      {{ item.title }}
                    </span>
                  </div>
                </template>
                <template #description>
                  <span class="text-xs text-gray-400">{{ dayjs(item.createTime).format('YYYY-MM-DD') }}</span>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </div>
    </div>
  </div>
</template>