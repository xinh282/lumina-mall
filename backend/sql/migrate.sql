-- ============================================================
-- 数据库增量迁移脚本（在已有数据库上执行，不删表不丢数据）
-- 用法: mysql -u root -p lumina_mall < migrate.sql
-- ============================================================

USE lumina_mall;

-- 1. l_order 表补加优惠券字段
ALTER TABLE l_order
    ADD COLUMN coupon_discount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠券抵扣金额' AFTER pay_time,
    ADD COLUMN user_coupon_id  BIGINT        DEFAULT NULL COMMENT '使用的用户优惠券ID' AFTER coupon_discount;

-- 2. 通知表
CREATE TABLE IF NOT EXISTS l_notification (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    user_id     BIGINT         NOT NULL COMMENT '接收用户ID',
    title       VARCHAR(200)   NOT NULL COMMENT '通知标题',
    content     VARCHAR(500)   NOT NULL COMMENT '通知内容',
    is_read     TINYINT        DEFAULT 0 COMMENT '0未读 1已读',
    type        VARCHAR(20)    DEFAULT 'SYSTEM' COMMENT '类型: ORDER/REFUND/COUPON/SYSTEM',
    ref_id      BIGINT         DEFAULT NULL COMMENT '关联业务ID',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 3. 退货退款表
CREATE TABLE IF NOT EXISTS l_refund (
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

-- 4. 优惠券模板表
CREATE TABLE IF NOT EXISTS l_coupon (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '优惠券ID',
    name            VARCHAR(100)   NOT NULL COMMENT '优惠券名称',
    type            VARCHAR(20)    NOT NULL COMMENT '类型: FIXED/PERCENT',
    threshold       DECIMAL(10,2)  NOT NULL COMMENT '使用门槛金额',
    discount_value  DECIMAL(10,2)  NOT NULL COMMENT '优惠值',
    total_count     INT            NOT NULL COMMENT '发行总量',
    used_count      INT            DEFAULT 0 COMMENT '已领取数',
    expire_days     INT            DEFAULT 30 COMMENT '有效天数',
    status          TINYINT        DEFAULT 1 COMMENT '1启用 0禁用',
    create_time     DATETIME       DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- 5. 用户优惠券表
CREATE TABLE IF NOT EXISTS l_user_coupon (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id     BIGINT         NOT NULL COMMENT '用户ID',
    coupon_id   BIGINT         NOT NULL COMMENT '优惠券ID',
    status      VARCHAR(20)    DEFAULT 'UNUSED' COMMENT 'UNUSED/USED/EXPIRED',
    used_time   DATETIME       DEFAULT NULL COMMENT '使用时间',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status),
    INDEX idx_coupon_id (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 6. 商品评价表
CREATE TABLE IF NOT EXISTS l_review (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',
    product_id  BIGINT         NOT NULL COMMENT '商品ID',
    user_id     BIGINT         NOT NULL COMMENT '用户ID',
    order_id    BIGINT         DEFAULT NULL COMMENT '订单ID（关联购买记录）',
    rating      TINYINT        NOT NULL COMMENT '评分 1-5',
    content     VARCHAR(500)   NOT NULL COMMENT '评价内容',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product (product_id),
    INDEX idx_user (user_id),
    INDEX idx_product_rating (product_id, rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- 7. 商品SKU规格表
CREATE TABLE IF NOT EXISTS l_product_sku (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'SKU ID',
    product_id  BIGINT         NOT NULL COMMENT '商品ID',
    specs       VARCHAR(200)   NOT NULL COMMENT '规格组合，如 黑色;M',
    price       DECIMAL(10,2)  DEFAULT NULL COMMENT 'SKU价格（NULL=用商品基础价）',
    stock       INT            DEFAULT 0 COMMENT 'SKU库存',
    sku_code    VARCHAR(50)    DEFAULT '' COMMENT 'SKU编码',
    status      TINYINT        DEFAULT 1 COMMENT '1启用 0禁用',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

-- 8. 支付流水表
CREATE TABLE IF NOT EXISTS l_payment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '支付ID',
    order_id    BIGINT         NOT NULL COMMENT '订单ID',
    order_no    VARCHAR(32)    NOT NULL COMMENT '订单编号',
    trade_no    VARCHAR(64)    DEFAULT '' COMMENT '支付宝交易号',
    amount      DECIMAL(10,2)  NOT NULL COMMENT '支付金额',
    pay_type    VARCHAR(20)    DEFAULT 'ALIPAY' COMMENT '支付方式',
    status      VARCHAR(20)    DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
    pay_time    DATETIME       DEFAULT NULL COMMENT '支付时间',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表';

-- 9. 商品收藏表
CREATE TABLE IF NOT EXISTS l_favorite (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT   NOT NULL,
    product_id  BIGINT   NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';

-- 10. 订单加物流字段
ALTER TABLE l_order ADD COLUMN IF NOT EXISTS tracking_no VARCHAR(50) DEFAULT '' AFTER receiver_address;
ALTER TABLE l_order ADD COLUMN IF NOT EXISTS logistics_company VARCHAR(50) DEFAULT '' AFTER tracking_no;
