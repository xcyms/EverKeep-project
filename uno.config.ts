import {
  defineConfig,
  presetAttributify,
  presetIcons,
  presetWind3, // 默认预设，支持 Tailwind/Windi CSS 语法
  transformerDirectives,
} from 'unocss'

export default defineConfig({
  presets: [
    presetWind3, // 默认预设，支持 Tailwind/Windi CSS 语法
    presetAttributify(), // 支持属性化模式，如 <div flex="~ col">
    presetIcons({
      extraProperties: {
        'display': 'inline-block',
        'vertical-align': 'middle',
      },
    }),
  ],
  safelist: [
    // 基础功能图标
    'i-fa6-solid:house',
    'i-fa6-solid:cloud-arrow-up',
    'i-fa6-solid:image',
    'i-fa6-solid:folder',
    'i-fa6-solid:shield-halved',
    'i-fa6-solid:wrench',
    // 顶部工具栏图标
    'i-fa6-solid:magnifying-glass',
    'i-fa6-solid:bell',
    'i-fa6-solid:user',
    'i-fa6-solid:power-off',
    // 图片页面图标
    'i-fa6-solid:check',
    'i-fa6-solid:circle-info',
    'i-fa6-solid:folder-open',
    'i-fa6-solid:trash-can',
  ],
  transformers: [
    transformerDirectives(), // 支持 @apply 等指令
  ],
})
