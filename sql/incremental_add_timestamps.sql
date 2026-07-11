-- ============================================================
-- 增量SQL：为缺少时间字段的表补充 create_time 和 update_time
-- 执行前请先备份数据库！
-- Date: 2026-07-11
-- ============================================================

-- 1. furniture（商品表）— 缺少 create_time / update_time，排序功能必需
ALTER TABLE `furniture`
    ADD COLUMN `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `deleted`,
    ADD COLUMN `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;

-- 现有数据用 id 大致推断（id 越大越新，基于 AUTO_INCREMENT 特性）
-- 根据已有数据的时间跨度，简单回填一个合理值
UPDATE `furniture` SET `create_time` = '2026-05-19 00:00:00', `update_time` = '2026-05-19 00:00:00'
WHERE `create_time` IS NULL;

-- 2. order_item（订单项表）— 缺少 create_time / update_time
ALTER TABLE `order_item`
    ADD COLUMN `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `deleted`,
    ADD COLUMN `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;

-- 回填：订单项创建时间 ≈ 所属订单的创建时间
UPDATE `order_item` oi
    JOIN `order` o ON oi.order_id = o.id
SET oi.create_time = o.create_time
WHERE oi.create_time IS NULL;

-- 3. user（用户表）— 缺少 update_time
ALTER TABLE `user`
    ADD COLUMN `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;

-- 4. sku（SKU表）— 缺少 update_time
ALTER TABLE `sku`
    ADD COLUMN `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;

-- 5. user_notification（用户通知表）— 缺少 create_time
ALTER TABLE `user_notification`
    ADD COLUMN `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `is_deleted`;

-- 6. sku_spec（SKU规格关联表）— 缺少 create_time / update_time
ALTER TABLE `sku_spec`
    ADD COLUMN `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `spec_value_id`;

-- 7. spec_value（规格值表）— 缺少 create_time / update_time
ALTER TABLE `spec_value`
    ADD COLUMN `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `sort`,
    ADD COLUMN `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;

-- 8. spec_group（规格组表）— 缺少 update_time
ALTER TABLE `spec_group`
    ADD COLUMN `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;
