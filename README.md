# EverKeep - 全栈个人云相册管理系统

EverKeep 是一款专为个人打造的现代化全栈相册管理解决方案。它不仅提供高性能的图片存储与管理服务，还拥有优雅的 Web 管理端和跨平台的移动端（小程序），支持多维度的统计分析、精细的权限控制以及自动化部署。

## ✨ 核心特性

- **🚀 高性能架构**：后端基于 Spring Boot 3 + Redis，支持 TB 级图片元数据管理，接口响应毫秒级。
- **📊 多维统计报表**：内置仪表盘，实时监控存储空间、图片数量及上传趋势，集成 ECharts 数据可视化。
- **🔐 严密的权限体系**：集成 Sa-Token 权限框架，支持 RBAC 模型，实现登录认证、角色授权、接口防刷。
- **🖼️ 优雅的视觉体验**：
  - **Web 端**：响应式布局，支持图片拖拽上传、瀑布流预览、相册分类管理。
  - **移动端**：原生流畅体验，支持沉浸式导航、图片懒加载、离线缓存。
- **📝 接口文档自动化**：内置自定义接口文档系统，逻辑层与控制层完全分离，支持权限点展示与在线调试。
- **💾 智能配额管理**：支持用户级存储配额限制（默认 100MB），实时计算已用空间。
- **🐳 云原生部署**：提供完善的 Docker Compose 配置，支持一键扩容与快速迁移。

## 🛠️ 技术栈

### 后端 (EverKeep-server)
- **核心框架**：Spring Boot 3.5.9
- **数据库/持久层**：MySQL 8.0 + MyBatis Plus 3.5.7
- **缓存/性能**：Redis (Spring Cache)
- **权限控权**：Sa-Token 1.37.0
- **接口增强**：自定义 ApiDoc 引擎（支持 Service 层解析）
- **工具类**：Hutool, ModelMapper, Lombok

### Web 端 (EverKeep-web)
- **框架**：Vue 3.5 + Vite 6
- **UI 组件库**：Ant Design Vue 4.2
- **样式方案**：UnoCSS (Tailwind 兼容)
- **状态管理**：Pinia
- **图表引擎**：ECharts 5.5

### 移动端 (EverKeep-mp)
- **框架**：Uni-app (Vue 3 + Vite)
- **请求流**：Alova.js (支持自动生成 API)
- **UI 组件**：Wot Design Uni

---

## 📂 项目结构

```text
EverKeep/
├── EverKeep-server/    # Spring Boot 核心后端 (Layered Architecture)
├── EverKeep-web/       # Vue 3 + AntD 管理后台 (Admin Portal)
├── EverKeep-mp/        # Uni-app 移动端 (Mini Program)
├── init-sql/           # 数据库初始化与迁移脚本
└── docker-compose.yml  # 生产环境一键部署配置
```

---

## 🚀 快速开始

### 1. 环境准备
- JDK 17+ / Node.js 18+ / MySQL 8.0+ / Redis 6.0+
- 推荐使用 Docker 部署：已安装 Docker & Docker Compose

### 2. 一键部署 (Docker)
```bash
# 克隆项目并进入目录
git clone https://github.com/xcyms/EverKeep.git
cd EverKeep

# 一键启动全栈服务
docker-compose up -d --build
```

### 3. 本地开发
- **后端**：导入 `EverKeep-server` 到 IDEA，运行 `ApiApplication`。
- **Web 端**：进入 `EverKeep-web`，运行 `npm install && npm run dev`。
- **移动端**：使用 HBuilderX 运行 `EverKeep-mp` 或执行 `npm run dev:mp-weixin`。

---

## 📝 贡献与协议
欢迎提交 Issue 或 Pull Request。本项目基于 **MIT License** 协议开源。