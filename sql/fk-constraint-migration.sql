-- ================================================================
-- 外键约束增量迁移（幂等）
-- 为缺失外键约束的表添加外键，确保数据引用完整性
-- 执行方式: docker exec -i furniture-mysql mysql -uroot -proot furniture-system < sql/fk-constraint-migration.sql
-- ================================================================

SET NAMES utf8mb4;

-- ================================================================
-- 1. 评论追评 → 主评价 + 用户
-- ================================================================
SELECT 'Add FK: comment_append → goods_comment' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'comment_append' AND CONSTRAINT_NAME = 'fk_comment_append_main');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE comment_append ADD CONSTRAINT fk_comment_append_main FOREIGN KEY (main_comment_id) REFERENCES goods_comment(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_comment_append_main already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Add FK: comment_append → user' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'comment_append' AND CONSTRAINT_NAME = 'fk_comment_append_user');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE comment_append ADD CONSTRAINT fk_comment_append_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_comment_append_user already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 2. 商品评价 → 订单 + 商品 + 用户 + 订单项
-- ================================================================
SELECT 'Add FK: goods_comment → order' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'goods_comment' AND CONSTRAINT_NAME = 'fk_goods_comment_order');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE goods_comment ADD CONSTRAINT fk_goods_comment_order FOREIGN KEY (order_id) REFERENCES `order`(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_goods_comment_order already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Add FK: goods_comment → furniture' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'goods_comment' AND CONSTRAINT_NAME = 'fk_goods_comment_furniture');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE goods_comment ADD CONSTRAINT fk_goods_comment_furniture FOREIGN KEY (goods_id) REFERENCES furniture(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_goods_comment_furniture already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Add FK: goods_comment → user' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'goods_comment' AND CONSTRAINT_NAME = 'fk_goods_comment_user');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE goods_comment ADD CONSTRAINT fk_goods_comment_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_goods_comment_user already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Add FK: goods_comment → order_item (nullable)' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'goods_comment' AND CONSTRAINT_NAME = 'fk_goods_comment_order_item');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE goods_comment ADD CONSTRAINT fk_goods_comment_order_item FOREIGN KEY (order_item_id) REFERENCES order_item(id) ON DELETE SET NULL ON UPDATE RESTRICT',
    'SELECT ''FK fk_goods_comment_order_item already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 3. 评论回复 → 主评价 + 用户 + 被回复用户 + 被回复评论(自引用)
-- ================================================================
SELECT 'Add FK: review_comment → goods_comment' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'review_comment' AND CONSTRAINT_NAME = 'fk_review_comment_main');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE review_comment ADD CONSTRAINT fk_review_comment_main FOREIGN KEY (review_id) REFERENCES goods_comment(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_review_comment_main already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Add FK: review_comment → user (author)' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'review_comment' AND CONSTRAINT_NAME = 'fk_review_comment_user');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE review_comment ADD CONSTRAINT fk_review_comment_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_review_comment_user already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Add FK: review_comment → user (reply target, nullable)' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'review_comment' AND CONSTRAINT_NAME = 'fk_review_comment_reply_user');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE review_comment ADD CONSTRAINT fk_review_comment_reply_user FOREIGN KEY (reply_to_user_id) REFERENCES user(id) ON DELETE SET NULL ON UPDATE RESTRICT',
    'SELECT ''FK fk_review_comment_reply_user already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Add FK: review_comment → review_comment (self-ref, nullable)' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'review_comment' AND CONSTRAINT_NAME = 'fk_review_comment_parent');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE review_comment ADD CONSTRAINT fk_review_comment_parent FOREIGN KEY (reply_to_comment_id) REFERENCES review_comment(id) ON DELETE SET NULL ON UPDATE RESTRICT',
    'SELECT ''FK fk_review_comment_parent already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 4. 收藏 → 用户 + 家具
-- ================================================================
SELECT 'Add FK: favorite → user' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'favorite' AND CONSTRAINT_NAME = 'fk_favorite_user');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE favorite ADD CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_favorite_user already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Add FK: favorite → furniture' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'favorite' AND CONSTRAINT_NAME = 'fk_favorite_furniture');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE favorite ADD CONSTRAINT fk_favorite_furniture FOREIGN KEY (furniture_id) REFERENCES furniture(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_favorite_furniture already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 5. 通知已读 → 通知 + 用户
-- ================================================================
SELECT 'Add FK: notification_read → notification' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'notification_read' AND CONSTRAINT_NAME = 'fk_notification_read_notification');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE notification_read ADD CONSTRAINT fk_notification_read_notification FOREIGN KEY (notification_id) REFERENCES notification(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_notification_read_notification already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Add FK: notification_read → user' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'notification_read' AND CONSTRAINT_NAME = 'fk_notification_read_user');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE notification_read ADD CONSTRAINT fk_notification_read_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_notification_read_user already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 6. 收货地址 → 用户
-- ================================================================
SELECT 'Add FK: user_address → user' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'user_address' AND CONSTRAINT_NAME = 'fk_user_address_user');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE user_address ADD CONSTRAINT fk_user_address_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_user_address_user already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 7. 通知 → 用户 (nullable, NULL=全体通知)
-- ================================================================
SELECT 'Add FK: notification → user (nullable)' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'notification' AND CONSTRAINT_NAME = 'fk_notification_user');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE notification ADD CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL ON UPDATE RESTRICT',
    'SELECT ''FK fk_notification_user already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 8. SKU → 家具
-- ================================================================
SELECT 'Add FK: sku → furniture' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'sku' AND CONSTRAINT_NAME = 'fk_sku_furniture');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE sku ADD CONSTRAINT fk_sku_furniture FOREIGN KEY (furniture_id) REFERENCES furniture(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_sku_furniture already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 9. 规格组 → 家具
-- ================================================================
SELECT 'Add FK: spec_group → furniture' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'spec_group' AND CONSTRAINT_NAME = 'fk_spec_group_furniture');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE spec_group ADD CONSTRAINT fk_spec_group_furniture FOREIGN KEY (furniture_id) REFERENCES furniture(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_spec_group_furniture already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 10. 规格值 → 规格组
-- ================================================================
SELECT 'Add FK: spec_value → spec_group' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'spec_value' AND CONSTRAINT_NAME = 'fk_spec_value_group');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE spec_value ADD CONSTRAINT fk_spec_value_group FOREIGN KEY (spec_group_id) REFERENCES spec_group(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_spec_value_group already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 11. SKU-规格关联 → SKU + 规格组 + 规格值
-- saveSpecAndSku 删除顺序: sku_spec → sku → spec_value → spec_group
-- 因此 FKs 不会被违反
-- ================================================================
SELECT 'Add FK: sku_spec → sku' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'sku_spec' AND CONSTRAINT_NAME = 'fk_sku_spec_sku');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE sku_spec ADD CONSTRAINT fk_sku_spec_sku FOREIGN KEY (sku_id) REFERENCES sku(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_sku_spec_sku already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Add FK: sku_spec → spec_group' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'sku_spec' AND CONSTRAINT_NAME = 'fk_sku_spec_group');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE sku_spec ADD CONSTRAINT fk_sku_spec_group FOREIGN KEY (spec_group_id) REFERENCES spec_group(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_sku_spec_group already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Add FK: sku_spec → spec_value' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'sku_spec' AND CONSTRAINT_NAME = 'fk_sku_spec_value');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE sku_spec ADD CONSTRAINT fk_sku_spec_value FOREIGN KEY (spec_value_id) REFERENCES spec_value(id) ON DELETE RESTRICT ON UPDATE RESTRICT',
    'SELECT ''FK fk_sku_spec_value already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 12. 订单项 → SKU (SET NULL: 删除SKU时保留订单快照数据)
-- ⚠️ 先清理孤儿引用: 将 sku_id 指向不存在 SKU 的记录置 NULL
--    (saveSpecAndSku 物理删 SKU 时曾留下孤儿数据)
-- ================================================================
SELECT 'Clean orphan sku_id in order_item' AS info;
UPDATE order_item SET sku_id = NULL
WHERE sku_id IS NOT NULL AND sku_id NOT IN (SELECT id FROM sku);

SELECT 'Add FK: order_item → sku (SET NULL)' AS info;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'order_item' AND CONSTRAINT_NAME = 'fk_order_item_sku');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE order_item ADD CONSTRAINT fk_order_item_sku FOREIGN KEY (sku_id) REFERENCES sku(id) ON DELETE SET NULL ON UPDATE RESTRICT',
    'SELECT ''FK fk_order_item_sku already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 13. SKU 编码唯一约束（已有唯一校验代码，加数据库兜底）
-- ================================================================
SELECT 'Add UK: sku.sku_code' AS info;
SET @uk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'sku' AND CONSTRAINT_NAME = 'uk_sku_code');
SET @sql = IF(@uk_exists = 0,
    'ALTER TABLE sku ADD CONSTRAINT uk_sku_code UNIQUE (sku_code)',
    'SELECT ''UK uk_sku_code already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ================================================================
-- 14. sku_spec 唯一约束：同一SKU不能重复关联同一规格组
-- ================================================================
SELECT 'Add UK: sku_spec(sku_id, spec_group_id)' AS info;
SET @uk_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'sku_spec' AND CONSTRAINT_NAME = 'uk_sku_spec_group');
SET @sql = IF(@uk_exists = 0,
    'ALTER TABLE sku_spec ADD CONSTRAINT uk_sku_spec_group UNIQUE (sku_id, spec_group_id)',
    'SELECT ''UK uk_sku_spec_group already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT '=== FK 约束迁移完成 ===' AS done;
