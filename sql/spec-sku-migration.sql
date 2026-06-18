/*
  规格 + SKU 扩展迁移脚本
  执行前请先备份数据库！
  Date: 2026/06/14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 规格组表
-- ============================================================
DROP TABLE IF EXISTS `spec_group`;
CREATE TABLE `spec_group`
(
    `id`           bigint                                                       NOT NULL AUTO_INCREMENT,
    `furniture_id` bigint                                                       NOT NULL COMMENT '关联商品ID',
    `group_name`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规格组名称，如颜色、尺寸',
    `sort`         int                                                          NULL DEFAULT 0 COMMENT '排序',
    `create_time`  datetime                                                     NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_furniture_id` (`furniture_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '规格组表'
  ROW_FORMAT = DYNAMIC;

-- ============================================================
-- 2. 规格值表
-- ============================================================
DROP TABLE IF EXISTS `spec_value`;
CREATE TABLE `spec_value`
(
    `id`            bigint                                                        NOT NULL AUTO_INCREMENT,
    `spec_group_id` bigint                                                        NOT NULL COMMENT '关联规格组ID',
    `value_name`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规格值名称，如米白、三人位',
    `value_image`   varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格值图片URL',
    `sort`          int                                                           NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_spec_group_id` (`spec_group_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '规格值表'
  ROW_FORMAT = DYNAMIC;

-- ============================================================
-- 3. SKU表
-- ============================================================
DROP TABLE IF EXISTS `sku`;
CREATE TABLE `sku`
(
    `id`           bigint                                                        NOT NULL AUTO_INCREMENT,
    `furniture_id` bigint                                                        NOT NULL COMMENT '关联商品ID',
    `sku_code`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT 'SKU编码',
    `price`        decimal(10, 2)                                                NOT NULL COMMENT 'SKU价格',
    `stock`        int                                                           NOT NULL DEFAULT 0 COMMENT 'SKU库存',
    `sku_image`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT 'SKU图片',
    `status`       tinyint                                                       NULL     DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    `create_time`  datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_furniture_id` (`furniture_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SKU表'
  ROW_FORMAT = DYNAMIC;

-- ============================================================
-- 4. SKU-规格关联表
-- ============================================================
DROP TABLE IF EXISTS `sku_spec`;
CREATE TABLE `sku_spec`
(
    `id`            bigint NOT NULL AUTO_INCREMENT,
    `sku_id`        bigint NOT NULL COMMENT '关联SKU ID',
    `spec_group_id` bigint NOT NULL COMMENT '规格组ID',
    `spec_value_id` bigint NOT NULL COMMENT '规格值ID',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_sku_id` (`sku_id` ASC) USING BTREE,
    INDEX `idx_spec_value_id` (`spec_value_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SKU与规格值关联表'
  ROW_FORMAT = DYNAMIC;

-- ============================================================
-- 5. furniture 表增加字段
-- ============================================================
ALTER TABLE `furniture`
    ADD COLUMN `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品详情描述' AFTER `images`;

-- ============================================================
-- 6. order_item 表增加字段
-- ============================================================
ALTER TABLE `order_item`
    ADD COLUMN `sku_id`   bigint                                                        NULL DEFAULT NULL COMMENT 'SKU ID' AFTER `furniture_id`,
    ADD COLUMN `sku_spec` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格快照，如：颜色:米白,尺寸:三人位' AFTER `quantity`;

-- ============================================================
-- 7. 为现有商品创建默认SKU（无规格）
-- ============================================================
INSERT INTO `sku` (`furniture_id`, `sku_code`, `price`, `stock`, `status`)
SELECT `id`, CONCAT('SKU-', `id`), `price`, `stock`, 1
FROM `furniture`;

-- ============================================================
-- 8. 更新 order_item 的 sku_id（关联到对应商品的默认SKU）
-- ============================================================
UPDATE `order_item` oi
    INNER JOIN `sku` s ON s.furniture_id = oi.furniture_id AND s.sku_code = CONCAT('SKU-', oi.furniture_id)
SET oi.sku_id = s.id;

SET FOREIGN_KEY_CHECKS = 1;