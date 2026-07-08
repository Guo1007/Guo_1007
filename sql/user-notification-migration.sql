-- ================================================================
-- 将 notification_read 改造为 user_notification
-- 支持用户级别的已读/删除状态管理
-- ================================================================

-- 1. 重命名表
RENAME TABLE notification_read TO user_notification;

-- 2. 新增加字段
ALTER TABLE user_notification
  ADD COLUMN is_read    TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读 0=未读 1=已读',
  ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '用户是否删除 0=未删 1=已删',
  ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间';

-- 3. 将已有数据的 read_time 不为空视为已读，回填 is_read
UPDATE user_notification SET is_read = 1 WHERE read_time IS NOT NULL;

-- 4. read_time 改为可空（创建记录时可能尚未阅读）
ALTER TABLE user_notification MODIFY read_time DATETIME NULL COMMENT '阅读时间';

-- 5. 添加索引
ALTER TABLE user_notification ADD INDEX idx_user_deleted (user_id, is_deleted);
