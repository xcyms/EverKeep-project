# EverKeep - 全栈个人相册管理系统

EverKeep 是一款现代化的全栈相册管理解决方案，包含 Web 管理端、移动端（小程序）以及高性能后端服务。支持图片上传、相册分类、权限管理等核心功能。

## 🚀 项目特性

- **多端支持**：包含基于 Vue 3 的 Web 端和基于 Uni-app 的移动端。
- **高性能后端**：采用 Spring Boot 3 + MyBatis Plus，支持响应式 API。
- **权限控权**：集成 Sa-Token 框架，实现轻量级的登录认证和权限校验。
- **优雅界面**：Web 端使用 Ant Design Vue，移动端支持瀑布流布局和 ECharts 图表展示。
- **容器化部署**：支持 Docker Compose 一键快速部署整个生产环境。

---

## 🛠️ 技术栈

### 后端 (EverKeep-server)
- **核心框架**：Spring Boot 3.5.9
- **数据库**：MySQL 8.0 + Druid 连接池
- **持久层**：MyBatis Plus
- **缓存**：Redis
- **权限**：Sa-Token
- **工具类**：Hutool, Lombok

### Web 端 (EverKeep-web)
- **框架**：Vue 3 + Vite
- **状态管理**：Pinia
- **路由**：Vue Router
- **UI 组件库**：Ant Design Vue
- **样式**：UnoCSS

### 移动端 (EverKeep-mp)
- **框架**：Uni-app (Vue 3 + Vite)
- **UI 库**：Uni-ui
- **图表**：ECharts

---

## 📂 项目结构

```text
EverKeep/
├── EverKeep-server/    # SpringBoot 后端源码
├── EverKeep-web/       # Vue 3 管理端源码
├── EverKeep-mp/        # Uni-app 移动端源码
├── init-sql/           # 数据库初始化脚本 (.sql)
└── docker-compose.yml  # Docker 编排配置
```

---

## 📦 快速部署 (Docker)

### 1. 前置要求
- 已安装 **Docker** 和 **Docker Compose**。
- 如果在国内环境，请配置 Docker 镜像加速器。

### 2. 准备数据库
将您的数据库初始化脚本（如 `schema.sql`）放入根目录下的 `init-sql/` 文件夹中。

### 3. 一键启动
在项目根目录下运行：
```bash
docker-compose up -d --build
```

### 4. 访问服务
- **前端 Web 界面**：`http://localhost`
- **后端 API 地址**：`http://localhost/api`
- **MySQL 地址**：`localhost:3306` (用户: root, 密码: your_password)
- **Redis 地址**：`localhost:6379`

---

## 🔧 本地开发

### 后端
1. 导入 `EverKeep-server` 到 IDEA。
2. 修改 `application-dev.yml` 中的 MySQL 和 Redis 连接信息。
3. 运行 `ApiApplication`。

### Web 端
```bash
cd EverKeep-web
npm install
npm run dev
```

### 移动端
1. 使用 **HBuilderX** 导入 `EverKeep-mp` 目录。
2. 运行到浏览器或小程序模拟器。

---

## 📝 开源协议
本项目遵循 [MIT License](LICENSE) 协议。