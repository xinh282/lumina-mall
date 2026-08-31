# LUMINA 精选好物商城 — 全栈毕业设计项目

## 项目简介

LUMINA 是一个基于 **Vue 3 + Spring Boot 3** 的全栈电商平台，定位精品设计师品牌商城。项目采用前后端分离架构，涵盖用户认证、商品浏览、购物车、订单管理、秒杀抢购等完整电商业务流程。

---

## 技术栈

### 前端
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4+ | 前端框架 (Composition API + `<script setup>`) |
| Vite | 5.x | 构建工具，快速 HMR |
| Element Plus | 2.7+ | UI 组件库 |
| Vue Router | 4.x | 路由管理 + 导航守卫 |
| Pinia | 2.x | 状态管理 (用户/购物车) |
| Axios | 1.x | HTTP 客户端 (封装拦截器) |
| TypeScript | 5.x | 类型安全 |

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.x | 应用框架 (Java 17) |
| MyBatis-Plus | 3.5.x | ORM + 分页插件 |
| MySQL | 8.0+ | 关系数据库 |
| Redis | 7.x | 缓存 + 秒杀队列 |
| Spring Security | 6.x | 认证鉴权 |
| JWT (jjwt) | 0.12.x | 无状态令牌 |
| Knife4j | 4.x | Swagger API 文档 |
| BCrypt | - | 密码加密 |
| Lombok | 1.18.x | 代码简化 |

---

## 功能模块

### 用户模块
- 注册 / 登录 / JWT 令牌认证
- BCrypt 密码加密
- 个人信息查看 / 修改
- 路由守卫 (未登录拦截)

### 商品模块
- 商品分页列表 (多条件筛选 + 排序)
- 商品详情页
- 热门商品推荐 / 新品展示
- 关键词搜索 (MySQL FULLTEXT)
- 分类浏览

### 购物车模块
- 添加商品 (已存在则叠加数量)
- 修改数量 / 删除商品
- 购物车列表 (关联商品信息)
- 侧边栏快速预览

### 订单模块
- 从购物车创建订单 (事务保证)
- 库存校验 + 扣减
- 订单列表 (分页 + 状态筛选)
- 订单详情 (含明细)
- 取消订单 (PENDING状态下恢复库存)

### 秒杀模块
- Redis Lua 脚本原子预扣库存
- 用户限购 (SETNX 防重复)
- 异步下单队列 (Redis List + @Scheduled)
- 双重防超卖 (Lua 原子性 + MySQL 行级锁)

---

## 技术亮点

1. **Redis 缓存体系**: 商品详情 (1h TTL)、热门商品 (30min TTL)、用户信息 (30min TTL)，Cache-Aside 模式
2. **全局异常处理**: `@RestControllerAdvice` 统一捕获业务异常、参数校验异常、系统异常，返回统一格式
3. **统一响应体**: `Result<T>` 封装 code/message/data，`PageResult<T>` 封装分页数据
4. **MyBatis-Plus 分页优化**: 动态 SQL + 多条件查询 + 多种排序策略
5. **Knife4j 接口文档**: 访问 `http://localhost:8080/doc.html` 在线调试所有 API
6. **SLF4J 日志管理**: 分级日志 (dev=debug, prod=info)，关键操作记录
7. **多环境配置分离**: application-dev.yml / application-prod.yml
8. **JWT 无状态认证**: 24h 过期，请求拦截器自动携带，ThreadLocal 用户上下文
9. **秒杀高并发设计**: Lua 脚本原子操作 + 异步队列削峰 + 两阶段库存扣减
10. **响应式适配**: PC / 平板 / 手机三端适配

---

## 项目结构

```
lumina-mall/
├── frontend/                    # 前端项目 (Vue 3 + Vite)
│   ├── src/
│   │   ├── api/                 # API 接口封装
│   │   ├── components/          # 通用组件
│   │   │   ├── layout/          # 布局组件 (Header/Footer)
│   │   │   ├── home/            # 首页组件 (Hero/Features/Categories/Products)
│   │   │   ├── product/         # 商品组件 (Card/Filter/Pagination)
│   │   │   ├── cart/            # 购物车组件 (Sidebar)
│   │   │   ├── common/          # 通用组件 (Toast/EmptyState)
│   │   │   └── user/            # 用户组件 (Profile/OrderList)
│   │   ├── views/               # 页面 (8个)
│   │   ├── router/              # 路由配置 + 守卫
│   │   ├── stores/              # Pinia 状态管理
│   │   ├── styles/              # 样式 (变量/全局/ElementPlus覆盖)
│   │   ├── types/               # TypeScript 类型
│   │   └── utils/               # 工具函数
│   └── vite.config.ts
│
├── backend/                     # 后端项目 (Spring Boot 3)
│   ├── sql/
│   │   ├── schema.sql           # 建表脚本
│   │   └── data.sql             # 初始数据
│   ├── src/main/java/com/lumina/
│   │   ├── config/              # 配置类 (CORS/MP/Redis/Knife4j/Security)
│   │   ├── common/              # 通用类 (Result/Exception/Handler)
│   │   ├── entity/              # 实体类 (6张表)
│   │   ├── dto/                 # 数据传输对象
│   │   ├── vo/                  # 视图对象
│   │   ├── mapper/              # MyBatis Mapper
│   │   ├── service/             # 业务逻辑层
│   │   ├── controller/          # 控制器 (RESTful API)
│   │   ├── security/            # JWT 安全组件
│   │   └── util/                # 工具类
│   └── src/main/resources/
│       ├── application*.yml     # 配置 (dev/prod)
│       ├── mapper/*.xml         # SQL 映射
│       └── seckill.lua          # 秒杀 Lua 脚本
```

---

## 数据库设计 (6 张表)

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `l_user` | 用户表 | id, username(UK), password(BCrypt), nickname, email, status |
| `l_category` | 商品分类表 | id, name, parent_id, sort_order |
| `l_product` | 商品表 | id, name, description, category_id, price, stock, badge, is_hot, is_new, seckill_* |
| `l_cart` | 购物车表 | id, user_id, product_id (UK pair), quantity |
| `l_order` | 订单表 | id, order_no(UK), user_id, total_amount, status |
| `l_order_item` | 订单明细表 | id, order_id, product_id, product_name(快照), product_price(快照), quantity |

---

## 启动步骤

### 环境要求
- Node.js 18+
- Java 17+
- MySQL 8.0+
- Redis 7.x
- Maven 3.8+ (或使用项目自带 mvnw)

### 1. 克隆项目
```bash
cd lumina-mall
```

### 2. 初始化数据库
```bash
mysql -u root -p < backend/sql/schema.sql
mysql -u root -p lumina_mall < backend/sql/data.sql
```

### 3. 修改后端配置
编辑 `backend/src/main/resources/application-dev.yml`，修改 MySQL 用户名和密码：
```yaml
spring:
  datasource:
    username: root
    password: 你的密码
```

### 4. 启动 Redis
```bash
redis-server
```

### 5. 启动后端 (端口 8080)
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
启动后访问 Knife4j 接口文档: http://localhost:8080/doc.html

### 6. 启动前端 (端口 5173)
```bash
cd frontend
npm install
npm run dev
```
浏览器访问: http://localhost:5173

### 7. 测试账号
- 用户名: `test`
- 密码: `123456`

---

## API 接口概览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户 | POST | `/api/user/register` | 注册 |
| 用户 | POST | `/api/user/login` | 登录 (返回 JWT) |
| 用户 | GET | `/api/user/profile` | 获取个人信息 |
| 分类 | GET | `/api/categories` | 全部分类 |
| 商品 | GET | `/api/products` | 分页列表 (支持筛选/搜索/排序) |
| 商品 | GET | `/api/products/{id}` | 商品详情 |
| 商品 | GET | `/api/products/hot` | 热门商品 |
| 购物车 | GET/POST/PUT/DELETE | `/api/cart` | CRUD |
| 订单 | POST | `/api/orders` | 创建订单 |
| 订单 | GET | `/api/orders` | 订单列表 |
| 秒杀 | POST | `/api/seckill/{productId}` | 执行秒杀 |

---

## 页面截图预览

首页包含：
- Hero 主视觉横幅 (渐变背景 + Shimmer 动画)
- 服务承诺条 (4 列)
- 品类浏览 (4 张分类卡片)
- 精选商品 / 新品上市 (商品网格)

其他页面：
- 分类商品列表 (筛选 + 排序 + 分页)
- 商品详情 (图片 + 信息 + 加购 + 秒杀)
- 购物车侧边栏 / 购物车页面
- 登录 / 注册
- 个人中心 (信息编辑 + 订单管理)

---

## 开发心得 (可写入毕业设计报告)

本项目从前端静态页面出发，完整重构为 Vue 3 工程化项目，后端基于 Spring Boot 3 实现了完整的电商业务链路。核心技术挑战在于：

1. **秒杀并发控制**: 通过 Redis Lua 脚本实现原子库存预扣，结合 MySQL 行级锁形成双重防护，有效防止超卖
2. **缓存策略**: 采用 Cache-Aside 模式，对高频读取的热门商品和商品详情进行 Redis 缓存，降低数据库压力
3. **前后端分离**: RESTful API 设计 + JWT 无状态认证 + Axios 拦截器统一处理，用户体验流畅
4. **工程化实践**: 多环境配置分离、全局异常处理、统一响应格式、Knife4j 自动文档，符合企业级开发标准

---

## License

本项目仅用于毕业设计和个人学习。
