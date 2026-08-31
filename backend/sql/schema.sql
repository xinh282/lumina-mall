-- ============================================================
-- LUMINA 精选好物商城 - 数据库建表脚本
-- MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS lumina_mall
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;
USE lumina_mall;

-- ============================================================
-- 用户表
-- ============================================================
DROP TABLE IF EXISTS l_order_item;
DROP TABLE IF EXISTS l_order;
DROP TABLE IF EXISTS l_cart;
DROP TABLE IF EXISTS l_address;
DROP TABLE IF EXISTS l_product;
DROP TABLE IF EXISTS l_category;
DROP TABLE IF EXISTS l_user;

CREATE TABLE l_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname    VARCHAR(50)  DEFAULT '' COMMENT '昵称',
    email       VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone       VARCHAR(20)  DEFAULT '' COMMENT '手机号',
    avatar      VARCHAR(255) DEFAULT '' COMMENT '头像URL',
    status      TINYINT      DEFAULT 1 COMMENT '状态: 1正常 0禁用',
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 用户地址表
-- ============================================================
CREATE TABLE l_address (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '地址ID',
    user_id          BIGINT       NOT NULL COMMENT '用户ID',
    receiver_name    VARCHAR(50)  NOT NULL COMMENT '收货人',
    receiver_phone   VARCHAR(20)  NOT NULL COMMENT '收货电话',
    receiver_address VARCHAR(255) NOT NULL COMMENT '收货地址',
    is_default       TINYINT      DEFAULT 0 COMMENT '是否默认: 1默认 0非默认',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_user_default (user_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

-- ============================================================
-- 商品分类表
-- ============================================================
CREATE TABLE l_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name        VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id   BIGINT      DEFAULT 0 COMMENT '父分类ID，0=一级分类',
    sort_order  INT         DEFAULT 0 COMMENT '排序值(越小越前)',
    icon        VARCHAR(100) DEFAULT '' COMMENT '图标标识',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ============================================================
-- 商品表
-- ============================================================
CREATE TABLE l_product (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    name           VARCHAR(200)   NOT NULL COMMENT '商品名称',
    description    TEXT           COMMENT '商品描述',
    category_id    BIGINT         NOT NULL COMMENT '所属分类ID',
    price          DECIMAL(10,2)  NOT NULL COMMENT '售价',
    original_price DECIMAL(10,2)  DEFAULT NULL COMMENT '原价(NULL=无折扣)',
    stock          INT            DEFAULT 0 COMMENT '库存数量',
    badge          VARCHAR(20)    DEFAULT '' COMMENT '徽章类型: new/sale/hot',
    badge_text     VARCHAR(20)    DEFAULT '' COMMENT '徽章文字',
    image          VARCHAR(255)   DEFAULT '' COMMENT '主图URL',
    images         TEXT           COMMENT '图片列表(JSON数组)',
    sales          INT            DEFAULT 0 COMMENT '销量',
    status         TINYINT        DEFAULT 1 COMMENT '状态: 1上架 0下架',
    is_hot         TINYINT        DEFAULT 0 COMMENT '是否热卖',
    is_new         TINYINT        DEFAULT 0 COMMENT '是否新品',
    seckill_stock  INT            DEFAULT 0 COMMENT '秒杀库存',
    seckill_price  DECIMAL(10,2)  DEFAULT NULL COMMENT '秒杀价',
    seckill_start  DATETIME       DEFAULT NULL COMMENT '秒杀开始时间',
    seckill_end    DATETIME       DEFAULT NULL COMMENT '秒杀结束时间',
    sort_order     INT            DEFAULT 0 COMMENT '排序',
    create_time    DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_hot (is_hot),
    INDEX idx_new (is_new),
    INDEX idx_sales (sales),
    INDEX idx_seckill (seckill_start, seckill_end),
    FULLTEXT INDEX ft_name_desc (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ============================================================
-- 购物车表
-- ============================================================
CREATE TABLE l_cart (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车记录ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    product_id  BIGINT NOT NULL COMMENT '商品ID',
    quantity    INT    DEFAULT 1 COMMENT '数量',
    checked     TINYINT DEFAULT 1 COMMENT '是否选中: 1选中 0未选',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ============================================================
-- 订单表
-- ============================================================
CREATE TABLE l_order (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no         VARCHAR(32)    NOT NULL COMMENT '订单编号',
    user_id          BIGINT         NOT NULL COMMENT '用户ID',
    total_amount     DECIMAL(10,2)  NOT NULL COMMENT '订单总金额',
    status           VARCHAR(20)    DEFAULT 'PENDING' COMMENT '状态: PENDING/PAID/SHIPPED/RECEIVED/CANCELLED',
    receiver_name    VARCHAR(50)    DEFAULT '' COMMENT '收货人',
    receiver_phone   VARCHAR(20)    DEFAULT '' COMMENT '收货电话',
    receiver_address VARCHAR(255)   DEFAULT '' COMMENT '收货地址',
    pay_time         DATETIME       DEFAULT NULL COMMENT '支付时间',
    coupon_discount  DECIMAL(10,2)  DEFAULT 0 COMMENT '优惠券抵扣金额',
    user_coupon_id   BIGINT         DEFAULT NULL COMMENT '使用的用户优惠券ID',
    create_time      DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================================
-- 订单明细表
-- ============================================================
CREATE TABLE l_order_item (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '明细ID',
    order_id      BIGINT         NOT NULL COMMENT '订单ID',
    product_id    BIGINT         NOT NULL COMMENT '商品ID',
    product_name  VARCHAR(200)   NOT NULL COMMENT '商品名称(快照)',
    product_price DECIMAL(10,2)  NOT NULL COMMENT '商品单价(快照)',
    quantity      INT            NOT NULL COMMENT '购买数量',
    total_price   DECIMAL(10,2)  NOT NULL COMMENT '小计金额',
    create_time   DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ============================================================
-- 退货退款表
-- ============================================================
CREATE TABLE l_refund (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '退款单ID',
    order_id    BIGINT         NOT NULL COMMENT '订单ID',
    user_id     BIGINT         NOT NULL COMMENT '用户ID',
    reason      VARCHAR(500)   NOT NULL COMMENT '退款原因',
    amount      DECIMAL(10,2)  NOT NULL COMMENT '退款金额',
    status      VARCHAR(20)    DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED/REFUNDED',
    admin_note  VARCHAR(500)   DEFAULT '' COMMENT '管理员备注',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货退款表';

-- ============================================================
-- 通知表
-- ============================================================
CREATE TABLE l_notification (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    user_id     BIGINT         NOT NULL COMMENT '接收用户ID',
    title       VARCHAR(200)   NOT NULL COMMENT '通知标题',
    content     VARCHAR(500)   NOT NULL COMMENT '通知内容',
    is_read     TINYINT        DEFAULT 0 COMMENT '0未读 1已读',
    type        VARCHAR(20)    DEFAULT 'SYSTEM' COMMENT '类型: ORDER/REFUND/COUPON/SYSTEM',
    ref_id      BIGINT         DEFAULT NULL COMMENT '关联业务ID(订单ID/退款单ID等)',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ============================================================
-- 优惠券模板表
-- ============================================================
CREATE TABLE l_coupon (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '优惠券ID',
    name            VARCHAR(100)   NOT NULL COMMENT '优惠券名称',
    type            VARCHAR(20)    NOT NULL COMMENT '类型: FIXED(满减)/PERCENT(折扣)',
    threshold       DECIMAL(10,2)  NOT NULL COMMENT '使用门槛金额',
    discount_value  DECIMAL(10,2)  NOT NULL COMMENT '优惠值(金额或百分比)',
    total_count     INT            NOT NULL COMMENT '发行总量',
    used_count      INT            DEFAULT 0 COMMENT '已领取数',
    expire_days     INT            DEFAULT 30 COMMENT '有效天数',
    status          TINYINT        DEFAULT 1 COMMENT '1启用 0禁用',
    create_time     DATETIME       DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- ============================================================
-- 用户优惠券表
-- ============================================================
CREATE TABLE l_user_coupon (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id     BIGINT         NOT NULL COMMENT '用户ID',
    coupon_id   BIGINT         NOT NULL COMMENT '优惠券ID',
    status      VARCHAR(20)    DEFAULT 'UNUSED' COMMENT '状态: UNUSED/USED/EXPIRED',
    used_time   DATETIME       DEFAULT NULL COMMENT '使用时间',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status),
    INDEX idx_coupon_id (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';
