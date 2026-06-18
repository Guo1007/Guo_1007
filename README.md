# 🪑 家具商城系统 (FurnitureSystem)

一个全栈家具电商平台，包含用户端商城和管理后台，集成 AI 智能客服功能。

## ✨ 功能特性

### 用户端
- **用户系统** — 邮箱/手机号注册、验证码登录、密码登录、个人信息管理
- **家具浏览** — 首页推荐、分类浏览、商品详情、多图展示
- **购物车** — 加购、数量修改、SKU 规格选择
- **订单系统** — 下单、收货信息填写、订单状态跟踪
- **收货地址** — 多地址管理（增删改查）
- **收藏功能** — 收藏/取消收藏家具
- **评价系统** — 商品评价与展示
- **通知中心** — 站内消息通知、邮件通知（异步推送）
- **AI 智能客服** — 基于 RAG 的家具推荐助手"小智"，支持流式对话

### 管理后台
- **数据仪表盘** — 订单趋势、销售额统计、热门商品排行
- **用户管理** — 用户列表、状态启禁
- **家具管理** — 商品 CRUD、SKU 规格管理、图片上传（OSS）
- **分类管理** — 家具类型维护
- **订单管理** — 订单列表、状态流转
- **通知管理** — 站内通知发布

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| **后端** | Spring Boot 3.5 · Java 17 · MyBatis-Plus · Spring Security |
| **前端** | Vue 3 · Vite 5 · Element Plus · Tailwind CSS 4 · Pinia · Vue Router |
| **数据库** | MySQL 8.0 · Redis 7 |
| **消息队列** | RocketMQ 5.3（订单状态流转、库存告警、邮件通知异步解耦） |
| **AI 能力** | LangChain4j · 通义千问 qwen-max（流式对话 + RAG 知识库检索） |
| **对象存储** | 阿里云 OSS（家具图片上传） |
| **邮件** | Spring Mail + Thymeleaf 模板（验证码、订单通知、库存告警） |
| **部署** | Docker Compose · Nginx 反向代理 |

## 📁 项目结构

```
FurnitureSystem/
├── furniture-vue/          # 前端 Vue 3 项目
│   ├── src/
│   │   ├── api/            # 接口请求
│   │   ├── components/     # 公共组件（AI 聊天、购物车抽屉、通知铃铛等）
│   │   ├── composables/    # 组合式函数
│   │   ├── router/         # 路由配置（含权限守卫）
│   │   ├── stores/         # Pinia 状态管理
│   │   ├── styles/         # 全局样式
│   │   ├── utils/          # 工具函数
│   │   └── views/          # 页面
│   │       ├── admin/      # 管理后台页面
│   │       └── ...         # 用户端页面
│   └── vite.config.js
├── src/main/java/gcy/      # 后端 Java 项目
│   ├── ai/                 # AI 智能客服模块
│   │   ├── aiservice/      # LangChain4j AiService 接口
│   │   ├── config/         # AI 配置（记忆、检索器）
│   │   └── controller/     # AI 聊天接口
│   └── system/             # 业务模块
│       ├── config/         # 全局配置（MVC、异常处理、MyBatis）
│       ├── controller/     # 用户端 Controller
│       ├── controller/admin/ # 管理后台 Controller
│       ├── entity/         # 实体类（POJO / DTO / VO）
│       ├── listener/       # RocketMQ 消息监听器
│       ├── mapper/         # MyBatis-Plus Mapper
│       ├── security/       # Token 认证 + Spring Security
│       ├── service/        # 业务逻辑层
│       ├── task/           # 定时任务（库存告警检测）
│       └── utils/          # 工具类
├── sql/                    # 数据库初始化脚本
├── docker/                 # Docker 配置（Nginx、Dockerfile）
├── docker-compose.yml      # 一键部署编排
└── pom.xml
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0
- Redis 7
- Docker & Docker Compose（推荐）

### 方式一：Docker 一键部署（推荐）

```bash
# 1. 克隆项目
git clone <your-repo-url>
cd FurnitureSystem

# 2. 配置环境变量（复制模板并填写）
cp .env.example .env
# 编辑 .env 填入必要的 API Key

# 3. 一键启动
docker-compose up -d

# 4. 访问
# 前端：http://localhost
# 后端 API：http://localhost:8080
```

### 方式二：本地开发

```bash
# ---- 后端 ----
# 1. 启动 MySQL、Redis（可借助 Docker）
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=furniture-system mysql:8.0
docker run -d --name redis -p 6379:6379 redis:7-alpine

# 2. 导入数据库
mysql -u root -proot furniture-system < sql/furniture-system.sql

# 3. 启动后端
mvn spring-boot:run

# ---- 前端 ----
cd furniture-vue
npm install
npm run dev
# 访问 http://localhost:5173
```

## ⚙️ 配置说明

核心配置通过环境变量注入，主要项：

| 变量 | 说明 | 必填 |
|------|------|------|
| `DEEPSEEK_API_KEY` | AI 聊天 API Key（通义千问） | 否 |
| `OSS_ACCESS_KEY` | 阿里云 OSS AccessKey | 是 |
| `OSS_SECRET_KEY` | 阿里云 OSS SecretKey | 是 |
| `OSS_BUCKET` | OSS Bucket 名称 | 是 |
| `MAIL_USERNAME` | 邮件发送账号 | 否 |
| `MAIL_PASSWORD` | 邮件授权码 | 否 |
| `MYSQL_ROOT_PASSWORD` | MySQL 密码 | 否（默认 root） |

## 🏗 架构设计要点

- **异步解耦**：订单状态变更、库存告警检测、邮件通知通过 RocketMQ 消息队列异步处理，避免阻塞主流程
- **Token 认证**：自定义 Token 认证过滤器，前后端分离无状态鉴权
- **RAG 智能客服**：基于 LangChain4j + Redis 向量数据库，结合通义千问大模型，实现家具知识库问答，支持流式响应
- **定时任务**：库存告警定时扫描，低库存商品自动邮件通知管理员
- **Docker 编排**：MySQL / Redis / RocketMQ / 后端 / 前端五容器编排，开箱即用

## 📄 License

MIT
