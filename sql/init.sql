-- ============================================================
-- 家具商城系统 数据库初始化脚本
-- ============================================================

CREATE DATABASE IF NOT EXISTS `furniture-system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `furniture-system`;

-- ============================================================
-- 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `phone`           VARCHAR(20) NOT NULL COMMENT '手机号',
    `user_name`       VARCHAR(50)  DEFAULT NULL COMMENT '用户名',
    `pass_word`       VARCHAR(255) DEFAULT NULL COMMENT '密码(bcrypt加密)',
    `icon`            VARCHAR(255) DEFAULT '' COMMENT '头像URL',
    `address`         VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
    `consignee`       VARCHAR(50)  DEFAULT NULL COMMENT '收货人',
    `consignee_phone` VARCHAR(20)  DEFAULT NULL COMMENT '收货人电话',
    `create_time`     DATETIME    NOT NULL COMMENT '注册时间',
    `is_admin`        TINYINT(1)   DEFAULT 0 COMMENT '是否管理员(1=是,0=否)',
    INDEX `idx_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- ============================================================
-- 家具分类表
-- ============================================================
CREATE TABLE IF NOT EXISTS `furniture_type`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(50) NOT NULL COMMENT '分类名称',
    `title`       VARCHAR(100) DEFAULT NULL COMMENT '分类描述',
    `icon`        VARCHAR(255) DEFAULT '' COMMENT '分类图标URL',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='家具分类表';

-- ============================================================
-- 家具表
-- ============================================================
CREATE TABLE IF NOT EXISTS `furniture`
(
    `id`      BIGINT AUTO_INCREMENT PRIMARY KEY,
    `f_name`  VARCHAR(100)   NOT NULL COMMENT '家具名称',
    `type_id` BIGINT         NOT NULL COMMENT '分类ID',
    `price`   DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `stock`   INT            NOT NULL DEFAULT 0 COMMENT '库存数量',
    `f_icon`  VARCHAR(255)            DEFAULT '' COMMENT '封面图片URL',
    `intro`   TEXT                    DEFAULT NULL COMMENT '简介',
    `brand`   VARCHAR(100)            DEFAULT NULL COMMENT '品牌',
    INDEX `idx_type` (`type_id`),
    INDEX `idx_brand` (`brand`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='家具表';

-- ============================================================
-- 订单表
-- ============================================================
CREATE TABLE IF NOT EXISTS `order`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT         NOT NULL COMMENT '用户ID',
    `total_price` DECIMAL(10, 2) NOT NULL COMMENT '总金额',
    `status`      TINYINT        NOT NULL DEFAULT 0 COMMENT '订单状态(0=待支付,1=已支付,2=已发货,3=已收货,4=已取消)',
    `consignee`   VARCHAR(50)             DEFAULT NULL COMMENT '收货人',
    `phone`       VARCHAR(20)             DEFAULT NULL COMMENT '联系电话',
    `address`     VARCHAR(255)            DEFAULT NULL COMMENT '收货地址',
    `remark`      VARCHAR(255)            DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME       NOT NULL COMMENT '创建时间',
    `pay_time`    DATETIME                DEFAULT NULL COMMENT '支付时间',
    `ship_time`   DATETIME                DEFAULT NULL COMMENT '发货时间',
    INDEX `idx_user` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单表';

-- ============================================================
-- 订单明细表
-- ============================================================
CREATE TABLE IF NOT EXISTS `order_item`
(
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id`         BIGINT         NOT NULL COMMENT '订单ID',
    `furniture_id`     BIGINT         NOT NULL COMMENT '家具ID',
    `furniture_name`   VARCHAR(100)            DEFAULT NULL COMMENT '家具名称(快照)',
    `furniture_icon`   VARCHAR(255)            DEFAULT NULL COMMENT '家具图标(快照)',
    `price`            DECIMAL(10, 2) NOT NULL COMMENT '单价',
    `quantity`         INT            NOT NULL DEFAULT 1 COMMENT '数量',
    `item_total_price` DECIMAL(10, 2) NOT NULL COMMENT '小计金额',
    INDEX `idx_order` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单明细表';

-- ============================================================
-- 通知表
-- ============================================================
CREATE TABLE IF NOT EXISTS `notification`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT       NULL COMMENT '接收人ID, NULL=发送给全体用户',
    `title`       VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content`     TEXT         NOT NULL COMMENT '通知内容',
    `type`        VARCHAR(20) DEFAULT 'system' COMMENT '类型: system(系统)/order(订单)/promotion(促销)',
    `is_read`     TINYINT(1)  DEFAULT 0 COMMENT '是否已读(0=未读,1=已读)',
    `create_time` DATETIME     NOT NULL COMMENT '发送时间',
    INDEX `idx_user_read` (`user_id`, `is_read`),
    INDEX `idx_create` (`create_time` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='通知表';
