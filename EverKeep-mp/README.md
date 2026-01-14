<p align="center">
  <img alt="logo" src="./src/static/logo.png" width="120">
</p>

<h1 align="center">
EverKeep
</h1>

<p align="center">一款现代化的<strong>通用移动端图片管理工具</strong></p>

<p align="center">
  基于 <strong>uni-app</strong> + <strong>Vue 3</strong> + <strong>Wot Design Uni</strong> 开发，旨在为各类图床与云存储提供统一的移动端管理体验。
</p>

## ✨ 特性

- 🚀 **通用管理**：架构设计支持对接多种图床后端（如 Lsky Pro, Aliyun OSS, Tencent COS, GitHub 等）。
- 📱 **多端适配**：原生支持 H5、微信小程序、App 等主流平台。
- 🎨 **精美 UI**：深度集成 Wot Design Uni，支持**暗黑模式**、**主题色自定义**及**跟随系统主题**。
- 🖼️ **瀑布流展示**：优化的瀑布流布局，支持海量图片的极速加载与流畅预览。
- 🔍 **智能搜索**：支持多维度相册搜索、图片筛选及灵活的排序功能。
- 📤 **快捷上传**：支持移动端随时随地上传图片，适配多种存储后端。
- 📊 **可视化看板**：直观的存储空间统计，实时掌握数据动态。
- 🛠️ **现代技术栈**：UnoCSS 原子化 CSS、Pinia 状态管理、Alova 极致高效请求。

## 📱 演示体验

<p align="left">
  <img src="./mp-weixin.jpg" width="200" />
  <br />
</p>

## 🛠️ 技术栈

- **框架**: [uni-app](https://uniapp.dcloud.net.cn/) (Vue 3 Composition API)
- **构建工具**: [Vite](https://vitejs.dev/)
- **脚手架**: [wot-starter](https://github.com/wot-ui/wot-starter/)
- **UI 组件库**: [Wot Design Uni](https://wot-ui.cn/)
- **样式引擎**: [UnoCSS](https://unocss.dev/)
- **状态管理**: [Pinia](https://pinia.vuejs.org/)
- **请求工具**: [Alova](https://alova.js.org/)
- **数据可视化**: [ECharts](https://echarts.apache.org/) (via [uni-echarts](https://github.com/lucidity99/uni-echarts))
- **常用 Hooks**: [VueUse](https://vueuse.org/)

## 🚀 快速上手

### 环境要求

- Node.js >= 20
- pnpm >= 9

### 安装

```bash
pnpm install
```

### 开发调试

```bash
# 启动 H5 端
pnpm dev

# 启动微信小程序端
pnpm dev:mp-weixin

# 启动支付宝小程序端
pnpm dev:mp-alipay
```

### 项目打包

```bash
# 打包 H5 端
pnpm build

# 打包微信小程序端
pnpm build:mp-weixin
```

## 📂 项目结构

```text
src/
├── api/            # 接口请求封装与 Mock 数据
├── components/     # 业务公共组件
├── composables/    # 组合式 API (主题、分页、缓存、系统信息等)
├── layouts/        # 页面布局模板 (Tabbar 等)
├── pages/          # 业务页面 (首页、相册、搜索、消息、个人中心等)
├── static/         # 静态资源 (图标、图片等)
├── store/          # Pinia 状态管理
└── utils/          # 常量定义与工具函数
```

## 📄 开源协议

本项目基于 [MIT](./LICENSE) 协议开源。
