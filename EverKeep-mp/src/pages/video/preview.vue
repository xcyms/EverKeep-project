<script lang="ts" setup>
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { formatDuration } from '@/utils'

const { error } = useGlobalToast()

definePage({
  style: {
    navigationBarTitleText: '视频预览',
    navigationBarBackgroundColor: '#000000',
    navigationBarTextStyle: 'white',
    backgroundColor: '#000000',
  },
})

const videoUrl = ref('')
const videoName = ref('')
const duration = ref(0)

onLoad((options) => {
  if (options?.url) {
    videoUrl.value = decodeURIComponent(options.url)
  }
  if (options?.name) {
    videoName.value = decodeURIComponent(options.name)
    uni.setNavigationBarTitle({ title: videoName.value })
  }
  if (options?.duration) {
    duration.value = Number(options.duration)
  }
})

function handleVideoError(e: any) {
  error({ msg: '视频加载失败' })
  console.error('Video error:', e)
}
</script>

<template>
  <view class="video-container">
    <view class="video-content">
      <video
        id="myVideo"
        :src="videoUrl"
        class="video-player"
        autoplay
        controls
        enable-play-gesture
        show-center-play-btn
        object-fit="contain"
        @error="handleVideoError"
      />
    </view>

    <!-- 信息层 -->
    <view class="footer-info">
      <view class="info-content">
        <view v-if="duration" class="tag-row">
          <view class="duration-tag">
            <wd-icon name="time" size="12px" color="rgba(255,255,255,0.9)" />
            <text class="ml-1">{{ formatDuration(duration) }}</text>
          </view>
        </view>
        <view class="video-title">
          {{ videoName }}
        </view>
      </view>
      <!-- 显式占位安全区 -->
      <view class="safe-area-spacer" />
    </view>
  </view>
</template>

<style lang="scss" scoped>
.video-container {
  width: 100vw;
  height: 100vh;
  background-color: #000;
  display: flex;
  flex-direction: column;
}

.video-content {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.video-player {
  width: 100%;
  height: 100%;
}

.footer-info {
  width: 100%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.9) 0%, rgba(0, 0, 0, 0.4) 100%);
  padding: 40rpx 40rpx 0;
  box-sizing: border-box;

  .info-content {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
  }

  .tag-row {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  .duration-tag {
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    color: #fff;
    font-size: 22rpx;
    padding: 4rpx 16rpx;
    border-radius: 6rpx;
    display: flex;
    align-items: center;
  }

  .video-title {
    color: #ffffff;
    font-size: 36rpx;
    font-weight: 600;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }

  .safe-area-spacer {
    height: calc(40rpx + env(safe-area-inset-bottom));
    width: 100%;
  }
}
</style>
