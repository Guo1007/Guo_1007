-- ============================================
-- 管理员邮件通知配置
-- ============================================

CREATE TABLE IF NOT EXISTS admin_notify_setting (
    id          BIGINT       NOT NULL COMMENT '配置ID（固定为1，单行配置）',
    enabled     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否开启管理员邮件通知(0否1是)',
    admin_ids   VARCHAR(500) DEFAULT NULL COMMENT '接收通知的管理员ID，逗号分隔（为空则不发送）',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '管理员邮件通知配置表';

-- 初始化默认配置（默认关闭，管理员需在后台开启并勾选接收人）
INSERT INTO admin_notify_setting (id, enabled, admin_ids)
VALUES (1, 0, NULL);
