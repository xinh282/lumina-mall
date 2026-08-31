# LUMINA 商城部署指南

## 前置条件

- 一台 Linux 服务器（CentOS 7+ / Ubuntu 20.04+），2核4G以上
- 服务器已安装 **Docker** 和 **Docker Compose**
- 域名（可选）

---

## 方式一：Docker Compose 部署（推荐，一条命令上线）

### 1. 把项目上传到服务器

```bash
# 在本地打包项目（不含 node_modules 和 target）
cd d:/claude_deepseek_v4
tar -czf lumina.tar.gz \
  --exclude=node_modules \
  --exclude=target \
  --exclude=.idea \
  --exclude=dist \
  --exclude=.git \
  .

# 上传到服务器
scp lumina.tar.gz root@你的服务器IP:/opt/

# 在服务器上解压
ssh root@你的服务器IP
cd /opt && tar -xzf lumina.tar.gz
```

### 2. 设置环境变量

```bash
cd /opt
cp .env.example .env
vim .env
```

```env
DB_PASSWORD=你的安全密码
JWT_SECRET=随机生成一串字符
DEEPSEEK_API_KEY=sk-xxxxxxxx
```

### 3. 启动

```bash
docker compose up -d
```

等 30 秒让 MySQL 初始化完，访问 `http://你的服务器IP:8080` 就能看到商城。

### 4. 查看日志

```bash
docker compose logs -f app      # 应用日志
docker compose logs -f mysql    # 数据库日志
```

### 5. 停止/重启

```bash
docker compose down             # 停止
docker compose up -d            # 重启
docker compose down -v          # 停止并清空数据（慎用）
```

---

## 方式二：手动部署

### 1. 服务器安装依赖

```bash
# Ubuntu
apt update && apt install -y openjdk-17-jdk mysql-server redis-server

# CentOS
yum install -y java-17-openjdk mysql-server redis
```

### 2. 初始化数据库

```bash
mysql -u root -p < backend/sql/schema.sql
mysql -u root -p < backend/sql/data.sql
mysql -u root -p < backend/sql/seed_products.sql
mysql -u root -p < backend/sql/migrate.sql
```

### 3. 配置环境变量

```bash
export DB_PASSWORD=你的数据库密码
export REDIS_HOST=localhost
export JWT_SECRET=你的JWT密钥
export DEEPSEEK_API_KEY=sk-xxxxxxxx
```

### 4. 启动应用

```bash
java -jar backend/target/lumina-mall-1.0.0.jar --spring.profiles.active=prod
```

---

## 方式三：传统运维部署（用 Nginx 反代 + 域名）

### 1. 安装 Nginx

```bash
apt install -y nginx
```

### 2. 配置 Nginx

```nginx
# /etc/nginx/sites-available/lumina
server {
    listen 80;
    server_name 你的域名.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
ln -s /etc/nginx/sites-available/lumina /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
```

### 3. 配置 HTTPS（Certbot 免费证书）

```bash
apt install -y certbot python3-certbot-nginx
certbot --nginx -d 你的域名.com
```

### 4. 应用设为系统服务（开机自启）

```bash
# /etc/systemd/system/lumina.service
[Unit]
Description=LUMINA Mall
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt
ExecStart=/usr/bin/java -jar /opt/lumina-mall-1.0.0.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
systemctl daemon-reload
systemctl enable lumina
systemctl start lumina
```

---

## 常用运维命令

```bash
# 查看应用状态
systemctl status lumina
docker compose ps

# 查看实时日志
journalctl -u lumina -f
tail -f logs/spring.log

# 数据库备份
mysqldump -u root -p lumina_mall > backup_$(date +%Y%m%d).sql

# 数据库恢复
mysql -u root -p lumina_mall < backup_20260615.sql
```

---

## 文件说明

| 文件 | 用途 |
|------|------|
| `docker-compose.yml` | Docker 一键部署配置（MySQL + Redis + App） |
| `backend/Dockerfile` | 后端 Docker 镜像构建文件 |
| `backend/sql/schema.sql` | 数据库建表 |
| `backend/sql/data.sql` | 基础数据（管理员账号等） |
| `backend/sql/seed_products.sql` | 商品示例数据 |
| `backend/sql/migrate.sql` | 增量迁移脚本 |
| `backend/src/main/resources/application-prod.yml` | 生产环境配置 |
| `frontend/dist/` | 前端构建产物（已内嵌到 jar 中） |
