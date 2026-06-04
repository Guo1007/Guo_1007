# 家具商城系统 (Furniture Shop)

一个功能完整的前后端分离家具电商平台，包含用户端和管理后台，支持AI智能客服、购物车、订单管理、支付等核心功能。

## 技术栈

### 后端技术

| 技术                 | 版本       | 说明       |
|--------------------|----------|----------|
| Spring Boot        | 4.0.4    | 核心框架     |
| Spring Security    | -        | 安全认证框架   |
| Spring AI + OpenAI | 2.0.0-M8 | AI智能客服集成 |
| MyBatis Plus       | 3.5.5    | ORM框架    |
| MySQL              | -        | 关系型数据库   |
| Redis              | -        | 缓存与验证码存储 |
| JWT                | -        | Token认证  |
| Lombok             | -        | 代码简化     |
| Knife4j            | 4.5.0    | API文档    |
| WebFlux            | -        | SSE流式响应  |

### 前端技术

| 技术           | 说明      |
|--------------|---------|
| Vue 3        | 前端框架    |
| Vite         | 构建工具    |
| Vue Router   | 路由管理    |
| Pinia        | 状态管理    |
| Element Plus | UI组件库   |
| Axios        | HTTP客户端 |

## 项目结构

```
furniture-system/
├── src/main/java/com/Guo/furnituresystem/
│   ├── config/          # 配置类
│   │   ├── SecurityConfig.java      # Spring Security配置
│   │   ├── WebMvcConfig.java        # Web配置
│   │   └── Knife4jConfig.java       # API文档配置
│   ├── controller/      # 控制器
│   │   ├── AiChatController.java    # AI客服控制器
│   │   ├── CartController.java      # 购物车控制器
│   │   ├── FurnitureController.java # 家具控制器
│   │   ├── OrderController.java     # 订单控制器
│   │   └── UserController.java      # 用户控制器
│   ├── entity/          # 实体类
│   ├── service/         # 服务层
│   ├── mapper/          # 数据访问层
│   └── utils/           # 工具类
│
└── furniture-vue/       # Vue3前端
    ├── src/
    │   ├── api/         # API接口定义
    │   ├── components/  # 公共组件
    │   │   └── AiChat.vue  # AI客服组件
    │   ├── router/      # 路由配置
    │   ├── stores/      # Pinia状态管理
    │   └── views/       # 页面视图
    └── vite.config.js
```

## 核心功能

### 用户端功能

- **用户认证**：手机号/邮箱验证码登录、密码登录、JWT Token认证
- **商品浏览**：家具分类浏览、商品详情查看、收藏功能
- **购物车**：商品加入购物车、数量调整、商品移除
- **订单管理**：订单创建、在线支付、订单状态跟踪
- **收货地址**：地址增删改查、默认地址设置
- **AI客服**：基于DeepSeek的智能客服，支持流式对话

### 管理端功能

- **仪表盘**：销售数据统计、订单概览
- **家具管理**：商品CRUD、上下架管理、图片上传(OSS)
- **分类管理**：家具分类维护
- **订单管理**：订单列表、发货处理、状态更新
- **用户管理**：用户列表、状态管理
- **通知管理**：系统公告发布与管理

### 系统特性

- **安全认证**：Spring Security + JWT，支持Token自动刷新
- **接口限流**：基于Redis的验证码防刷机制
- **AI集成**：DeepSeek deepseek-v4-pro模型，SSE流式响应
- **文件存储**：阿里云OSS对象存储
- **API文档**：Knife4j自动生成接口文档

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

### 后端启动

1. **配置环境变量**
   ```bash
   # Windows PowerShell
   $env:DEEPSEEK_API_KEY="your_api_key_here"
   
   # Linux/Mac
   export DEEPSEEK_API_KEY="your_api_key_here"
   ```

2. **配置数据库**
   修改 `src/main/resources/application.yml` 中的数据库连接信息

3. **启动后端**
   ```bash
   mvn spring-boot:run
   ```
   后端默认运行在 `http://localhost:8080`

### 前端启动

```bash
cd furniture-vue
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`

## 配置说明

### 数据库配置 (application.yml)

```yaml
spring:
  datasource:
     driver-class-name: com.mysql.cj.jdbc.Driver
     url: jdbc:mysql://localhost:3306/furniture?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### Redis配置

```yaml
spring:
   data:
      redis:
         host: localhost
         port: 6379
         password: your_redis_password
```

### AI客服配置

```yaml
spring:
   ai:
      openai:
         base-url: https://api.deepseek.com
         api-key: ${DEEPSEEK_API_KEY}
         chat:
            options:
               model: deepseek-v4-pro
```

### 阿里云OSS配置

```yaml
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: your_access_key
    access-key-secret: your_secret
    bucket-name: your_bucket
```

## API接口示例

### 用户登录

```http
POST /user/login
Content-Type: application/json

{
  "phone": "13800138000",
  "code": "123456"
}
```

### 获取家具列表

```http
GET /furniture/list?typeId=1&current=1&size=10
```

### AI客服对话（流式）

```http
POST /ai/chat/stream
Content-Type: application/json

{
  "message": "推荐一款适合小户型的沙发",
  "history": []
}
```

响应：SSE流式返回

## 页面功能

| 页面   | 功能              |
|------|-----------------|
| 首页   | 热门家具展示、分类导航、搜索  |
| 分类页  | 按类型浏览家具、筛选排序    |
| 商品详情 | 商品信息、评价、加购、收藏   |
| 购物车  | 商品管理、数量调整、结算    |
| 订单页  | 订单创建、支付、状态查看    |
| 个人中心 | 订单管理、地址管理、收藏夹   |
| 管理后台 | 数据统计、商品/订单/用户管理 |

### 项目构建

```bash
# 后端打包
mvn clean package

# 前端构建
cd furniture-vue
npm run build
```

## 许可证

本项目仅供学习交流使用。