# LUMINA 商城后端

Spring Boot 3 + MyBatis-Plus + MySQL + Redis 电商后端服务

## 快速启动

```bash
# 1. 初始化数据库
mysql -u root -p < sql/schema.sql
mysql -u root -p lumina_mall < sql/data.sql

# 2. 启动 Redis
redis-server

# 3. 启动应用
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

访问 Knife4j 文档: http://localhost:8080/doc.html

## 技术栈

- Spring Boot 3.2.x
- MyBatis-Plus 3.5.x
- Spring Security + JWT
- Redis (缓存 + 秒杀队列)
- MySQL 8.0
- Knife4j (API 文档)

## 项目分层

```
controller/  → RESTful API 控制器
service/     → 业务逻辑层
mapper/      → MyBatis-Plus 数据访问
entity/      → 数据库实体
dto/         → 请求数据传输对象
vo/          → 响应视图对象
common/      → 统一响应、异常处理
config/      → Spring 配置
security/    → JWT 认证过滤器
```
