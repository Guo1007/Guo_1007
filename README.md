


# 预毕设 —— 家具商城系统

## 项目简介

这是一个前后端分离的家具在线销售与管理系统，使用 Spring Boot + Vue 3 技术栈开发，旨在提供一个完整的电商平台解决方案。项目同时用作毕业设计演示及 Git 版本控制练习。

## 技术栈

### 后端
- **框架**：Spring Boot 2.7.x
- **ORM**：MyBatis Plus
- **缓存**：Redis + Redisson（分布式锁）
- **对象存储**：阿里云 OSS
- **数据库**：MySQL

### 前端
- **框架**：Vue 3 + Vite
- **状态管理**：Pinia
- **路由**：Vue Router
- **HTTP**：Axios

## 功能模块

### 用户端
| 模块 | 说明 |
|------|------|
| 用户登录/注册 | 支持手机验证码、密码登录 |
| 首页浏览 | 家具分类展示与搜索 |
| 商品详情 | 查看家具详细信息 |
| 购物车 | 添加、删除商品 |
| 订单管理 | 创建订单、支付、取消、确认收货 |

### 管理端
| 模块 | 说明 |
|------|------|
| 用户管理 | 查看、编辑、删除用户 |
| 家具管理 | 添加、编辑、删除、上架家具 |
| 分类管理 | 家具类型维护 |
| 订单管理 | 订单发货、状态处理 |

### 系统功能
- 短信验证码登录
- Token 自动刷新
- 接口限流与防刷
- 统一异常处理
- 系统健康监控

## 项目结构

```
├── furniture-vue/          # Vue3 前端项目
│   ├── src/
│   │   ├── api/           # API 接口
│   │   ├── components/    # 公共组件
│   │   ├── composables/   # 组合式函数
│   │   ├── router/        # 路由配置
│   │   ├── stores/       # Pinia 状态管理
│   │   ├── styles/        # 样式文件
│   │   ├── utils/         # 工具函数
│   │   └── views/        # 页面视图
│   └── vite.config.js
│
└── src/main/java/com/example/furnituresystem/
    ├── config/             # 配置类
    ├── controller/        # 控制器
    ├── entity/           # 实体类
    ├── exception/        # 自定义异常
    ├── mapper/           # 数据访问层
    ├── monitor/         # 监控服务
    ├── service/          # 业务逻辑层
    └── utils/            # 工具类
```

## 快速开始

### 后端准备

1. 配置 `src/main/resources/application.yml`，修改数据库、Redis、阿里云OSS等配置

2. 创建数据库并导入表结构

3. 启动后端：
 ```bash
 mvn spring-boot:run
 ```

### 前端准备

```bash
cd furniture-vue
npm install
npm run dev
```

## 配置说明

主要配置项（application.yml）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/furniture
    username: root
    password: your_password
  
  redis:
    host: localhost
    port: 6379

aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    key: your_access_key
    secret: your_secret
    bucket: your_bucket
```

## API 接口示例

### 获取家具列表
```
GET /furniture/list?typeId=1&current=1&size=10
```

### 创建订单
```
POST /order/create
Content-Type: application/json

{
  "consignee": "张三",
  "phone": "13800138000",
  "address": "浙江省杭州市xxx",
  "itemList": [
    {"furnitureId": 1, "quantity": 2}
  ]
}
```

## 页面预览

- **首页**：展示热门家具、分类导航
- **购物车**：侧边抽屉式购物车
- **个人中心**：订单查看、地址管理
- **管理后台**：数据仪表盘、CRUD操作

## 开发环境要求

- JDK 8+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

## 许可证

本项目仅供学习交流使用。