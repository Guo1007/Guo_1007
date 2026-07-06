-- ============================================================
-- 数据库优化迁移：索引 + 唯一约束 + 审计字段
-- 执行方式：source index-constraint-migration.sql
-- 所有操作均为幂等，可重复执行
-- ============================================================

-- ============================================================
-- 1. order 表添加 status 索引（营收统计/订单查询加速）
-- ============================================================
SET @sql = (SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `order` ADD INDEX `idx_status`(`status` ASC) USING BTREE',
    'SELECT 1'
) FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'order' AND INDEX_NAME = 'idx_status');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 2. order 表添加 update_time 审计字段（幂等）
-- ============================================================
SET @sql = (SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `order` ADD COLUMN `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'' AFTER `receive_time`',
    'SELECT 1'
) FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'order' AND COLUMN_NAME = 'update_time');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 3. goods_comment 表添加 status 索引（审核查询加速）
-- ============================================================
SET @sql = (SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `goods_comment` ADD INDEX `idx_status`(`status` ASC) USING BTREE',
    'SELECT 1'
) FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'goods_comment' AND INDEX_NAME = 'idx_status');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 4. user 表 phone 添加唯一索引（防止重复注册）
-- ============================================================
SET @sql = (SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `user` ADD UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE',
    'SELECT 1'
) FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'user' AND INDEX_NAME = 'uk_phone');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 5. user 表 email 添加唯一索引（防止重复注册）
-- ============================================================
SET @sql = (SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `user` ADD UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE',
    'SELECT 1'
) FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'furniture-system' AND TABLE_NAME = 'user' AND INDEX_NAME = 'uk_email');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
