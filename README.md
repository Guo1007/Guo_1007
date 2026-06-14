# 家具商城系统

前后端分离的家具电商平台，包含用户端商城和管理后台，集成AI智能客服、商品多规格SKU管理等功能。

## 技术栈

**后端**

- Spring Boot 4.0.4 + Java 17
- Spring Security + JWT 认证
- MyBatis Plus 3.5.15
- MySQL + Redis + Redisson
- Spring AI (DeepSeek) + WebFlux SSE
- 阿里云OSS对象存储
- RocketMQ消息队列

**前端**

- Vue 3 + Vite
- Element Plus
- Pinia + Vue Router + Axios

## 项目结构

```
furniture-system/
├── src/main/java/com/Guo/furnituresystem/
│   ├── config/                    # 配置类
│   │   ├── MvcConfig.java
│   │   ├── MybatisConfig.java
│   │   ├── AliYunOssConfig.java
│   │   ├── RedissonConfig.java
│   │   └── MyExceptionHandler.java
│   ├── controller/                # 控制器
│   │   ├── AiChatController.java  # AI客服
│   │   ├── FurnitureController.java
│   │   ├── FurnitureTypeController.java
│   │   ├── OrderController.java
│   │   ├── UserController.java
│   │   ├── AddressController.java
│   │   ├── FavoriteController.java
│   │   ├── ReviewController.java
│   │   ├── NotificationController.java
│   │   └── admin/                 # 管理端接口
│   │       ├── DashboardController.java
│   │       ├── FurnitureManageController.java
│   │       ├── FurnitureTypeManageController.java
│   │       ├── OrderManageController.java
│   │       ├── UserManageController.java
│   │       ├── NotificationManageController.java
│   │       └── SpecController.java     # 规格SKU管理
│   ├── entity/
│   │   ├── dto/                   # 数据传输对象
│   │   │   └── admin/
│   │   │       └── FurnitureSpecDTO.java # 规格SKU保存DTO
│   │   ├── pojo/                  # 实体类
│   │   │   ├── Furniture.java
│   │   │   ├── SpecGroup.java     # 规格组
│   │   │   ├── SpecValue.java     # 规格值
│   │   │   ├── Sku.java           # SKU
│   │   │   └── SkuSpec.java       # SKU规格关联
│   │   └── vo/                    # 视图对象
│   │       └── FurnitureSpecVO.java # 规格SKU展示VO
│   ├── service/                   # 业务逻辑层
│   │   ├── ISpecService.java      # 规格服务接口
│   │   └── Impl/
│   │       └── SpecServiceImpl.java
│   ├── mapper/                    # MyBatis Mapper
│   │   ├── SpecGroupMapper.java
│   │   ├── SpecValueMapper.java
│   │   ├── SkuMapper.java
│   │   └── SkuSpecMapper.java
│   ├── task/                      # 定时任务
│   └── utils/                     # 工具类
│
└── furniture-vue/                 # 前端项目
    └── src/
        ├── api/                   # 接口定义
        │   ├── request.js         # Axios封装
        │   ├── user.js
        │   ├── furniture.js       # 含规格查询接口
        │   ├── order.js
        │   ├── address.js
        │   ├── favorite.js
        │   ├── review.js
        │   ├── notification.js
        │   ├── ai.js
        │   └── admin/             # 管理端接口
        │       ├── furniture.js
        │       ├── spec.js        # 规格SKU管理API
        │       └── ...
        ├── components/            # 公共组件
        │   ├── AiChat.vue         # AI客服浮窗（可拖拽）
        │   ├── CartDrawer.vue     # 购物车抽屉（展示规格）
        │   └── NotificationBell.vue # 通知铃铛
        ├── views/                 # 页面
        │   ├── HomeView.vue       # 首页
        │   ├── LoginView.vue      # 登录
        │   ├── RegisterView.vue   # 注册
        │   ├── FurnitureDetailView.vue # 商品详情
        │   ├── TypeDetailView.vue # 分类详情
        │   ├── UserOrdersView.vue # 我的订单
        │   ├── OrderPayView.vue   # 订单支付
        │   ├── ProfileView.vue    # 个人中心
        │   ├── AddressView.vue    # 收货地址
        │   ├── UserFavoritesView.vue # 我的收藏
        │   ├── NotificationView.vue # 通知列表
        │   └── admin/             # 管理后台
        │       ├── AdminLayout.vue
        │       ├── AdminDashboard.vue
        │       ├── FurnitureManage.vue
        │       ├── FurnitureTypeManage.vue
        │       ├── OrderManage.vue
        │       ├── UserManage.vue
        │       └── NotificationManage.vue
        ├── router/                # 路由配置
        ├── stores/                # Pinia状态
        └── utils/                 # 工具函数
```

## 功能模块

**用户端**

- 登录注册（手机/邮箱验证码 + 密码）
- 首页展示、分类浏览、商品详情
- 商品多规格选择（颜色、尺寸等，动态匹配SKU价格/库存）
- 购物车（同商品不同规格独立条目，展示已选规格）
- 订单创建与支付（基于SKU扣减库存）
- 收货地址管理
- 商品收藏、评价
- 系统通知
- AI智能客服（DeepSeek流式对话，可拖拽定位，登录/注册页自动隐藏）

**管理后台**

- 数据仪表盘
- 家具管理（增删改查、OSS图片上传）
- **商品规格与SKU管理**（规格组/值维护、SKU组合自动生成、独立定价/库存/图片）
- 分类管理
- 订单管理（发货、状态流转，订单明细记录规格快照）
- 用户管理
- 通知公告管理

## 商品规格系统设计

### 数据库表结构

```
furniture（商品主表）
  ├── spec_group（规格组表）         如：颜色、尺寸
  │     └── spec_value（规格值表）   如：米白、浅灰、三人位
  ├── sku（SKU表）                  每个规格组合对应一个独立SKU
  └── sku_spec（SKU-规格关联表）     关联SKU与规格值（多对多）
```

### 核心流程

**管理端：** 新建商品 → 添加规格组/值 → 自动生成SKU组合（笛卡尔积）→ 为每个SKU设置价格/库存/图片 → 保存

**用户端：** 商品详情页加载规格 → 点选规格值 → 系统匹配SKU → 价格/库存/图片动态更新 → 加入购物车 → 下单从SKU扣库存

### 设计要点

- **关联表方案**：使用 `sku_spec` 关联表替代JSON存储，避免键顺序问题，支持精确查询
- **规格快照**：订单明细保存下单时的规格文本（`order_item.sku_spec`），商品规格变更不影响历史订单
- **兼容性**：无规格商品自动生成默认SKU，新旧流程并存
- **库存双写**：SKU表存精确库存，商品表存汇总库存（列表展示用），事务保证一致

## 快速启动

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

### 后端

1. 创建数据库并导入表结构
   ```bash
   mysql -u root -p < sql/furniture-system.sql
   ```

2. 执行规格SKU迁移脚本
   ```bash
   mysql -u root -p furniture-system < sql/spec-sku-migration.sql
   ```

3. 配置 `application.yml`（数据库、Redis、OSS等）

4. 设置环境变量（AI客服功能）
   ```bash
   # Windows
   set DEEPSEEK_API_KEY=your_api_key

   # Linux/Mac
   export DEEPSEEK_API_KEY=your_api_key
   ```

5. 启动
   ```bash
   mvn spring-boot:run
   ```

### 前端

```bash
cd furniture-vue
npm install
npm run dev
```

访问 `http://localhost:5173`

## 配置说明

`application.yml` 主要配置：

```yaml
spring:
  datasource:
     url: jdbc:mysql://localhost:3306/furniture?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
     username: root
     password: your_password

  data:
     redis:
        host: localhost
        port: 6379
        password: your_redis_password

  ai:
     openai:
        base-url: https://api.deepseek.com
        api-key: ${DEEPSEEK_API_KEY}
        chat:
           options:
              model: deepseek-v4-pro

aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: your_access_key
    access-key-secret: your_secret
    bucket-name: your_bucket
```

## 构建部署

```bash
# 后端打包
mvn clean package

# 前端构建
cd furniture-vue
npm run build
```

## 许可证

本项目仅供学习交流使用。