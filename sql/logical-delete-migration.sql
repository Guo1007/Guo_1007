-- ============================================================
-- 逻辑删除迁移：为有必要的数据表添加 deleted 字段
-- 作用：deleteById / remove 自动变为 UPDATE SET deleted=1
--       所有 select 自动追加 WHERE deleted=0
-- 所有操作均为幂等，可重复执行
-- ============================================================

-- 用户表
SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `user` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0未删/1已删)'' AFTER `is_admin`',
    'SELECT 1'
) FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'deleted');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 家具表
SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `furniture` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0未删/1已删)'' AFTER `description`',
    'SELECT 1'
) FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'furniture' AND COLUMN_NAME = 'deleted');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 家具分类表
SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `furniture_type` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0未删/1已删)'' AFTER `update_time`',
    'SELECT 1'
) FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'furniture_type' AND COLUMN_NAME = 'deleted');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 订单表
SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `order` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0未删/1已删)'' AFTER `update_time`',
    'SELECT 1'
) FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'order' AND COLUMN_NAME = 'deleted');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 订单明细表
SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `order_item` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0未删/1已删)'' AFTER `item_total_price`',
    'SELECT 1'
) FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'order_item' AND COLUMN_NAME = 'deleted');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 主评价表
SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `goods_comment` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0未删/1已删)'' AFTER `create_time`',
    'SELECT 1'
) FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'goods_comment' AND COLUMN_NAME = 'deleted');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 追评表
SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `comment_append` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0未删/1已删)'' AFTER `append_time`',
    'SELECT 1'
) FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'comment_append' AND COLUMN_NAME = 'deleted');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 评论回复表
SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `review_comment` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0未删/1已删)'' AFTER `create_time`',
    'SELECT 1'
) FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'review_comment' AND COLUMN_NAME = 'deleted');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 通知表
SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `notification` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除(0未删/1已删)'' AFTER `create_time`',
    'SELECT 1'
) FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'notification' AND COLUMN_NAME = 'deleted');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
