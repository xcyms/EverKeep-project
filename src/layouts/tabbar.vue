<script lang="ts" setup>
import { useManualTheme } from '@/composables/useManualTheme'

const router = useRouter()
const route = useRoute()
const { activeTabbar, getTabbarItemValue, setTabbarItemActive, tabbarList } = useTabbar()
const { theme, isDark, themeVars } = useManualTheme()

function handleTabbarChange({ value }: { value: string }) {
  setTabbarItemActive(value)
  router.pushTab({ name: value })
}

onMounted(() => {
  // #ifdef APP
  uni.hideTabBar()
  // #endif
  nextTick(() => {
    if (route.name && route.name !== activeTabbar.value.name) {
      setTabbarItemActive(route.name)
    }
  })
})
</script>

<script lang="ts">
export default {
  options: {
    addGlobalClass: true,
    virtualHost: true,
    styleIsolation: 'shared',
  },
}
</script>

<template>
  <wd-config-provider :theme="theme" :theme-vars="themeVars">
    <div class="page-wraper" :class="{ 'wot-theme-dark': isDark }">
      <slot />
      <wd-gap safe-area-bottom height="var(--wot-tabbar-height, 50px)" />
      <wd-tabbar
        :model-value="activeTabbar.name" bordered safe-area-inset-bottom fixed
        @change="handleTabbarChange"
      >
        <wd-tabbar-item
          v-for="(item, index) in tabbarList" :key="index" :name="item.name"
          :value="getTabbarItemValue(item.name)" :title="item.title" :icon="item.icon"
        />
      </wd-tabbar>
    </div>
  </wd-config-provider>
</template>
