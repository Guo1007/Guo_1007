-- 1. 创建拒绝原因模板表
CREATE TABLE IF NOT EXISTS `review_reject_reason` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `reason` VARCHAR(200) NOT NULL COMMENT '拒绝原因内容',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号，越小越靠前',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0=未删除，1=已删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核拒绝原因模板表';

-- 2. 插入常用的拒绝原因模板
INSERT INTO `review_reject_reason` (`reason`, `sort_order`) VALUES
('包含广告或推广信息', 1),
('包含联系方式（手机号、微信号等）', 2),
('包含辱骂或人身攻击', 3),
('包含色情或低俗内容', 4),
('包含政治敏感内容', 5),
('与商品无关的灌水内容', 6),
('虚假评价', 7),
('恶意差评', 8);

-- 3. 商品评价表：新增 AI 拒绝原因（AI审核时写入）和人工拒绝原因（管理员拒绝时写入）
ALTER TABLE `goods_comment`
    ADD COLUMN `ai_reject_reason` VARCHAR(500) DEFAULT NULL COMMENT 'AI审核拒绝原因' AFTER `status`,
    ADD COLUMN `manual_reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '人工审核拒绝原因' AFTER `ai_reject_reason`;

-- 4. 追评表
ALTER TABLE `comment_append`
    ADD COLUMN `ai_reject_reason` VARCHAR(500) DEFAULT NULL COMMENT 'AI审核拒绝原因' AFTER `status`,
    ADD COLUMN `manual_reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '人工审核拒绝原因' AFTER `ai_reject_reason`;

-- 5. 评价回复表
ALTER TABLE `review_comment`
    ADD COLUMN `ai_reject_reason` VARCHAR(500) DEFAULT NULL COMMENT 'AI审核拒绝原因' AFTER `status`,
    ADD COLUMN `manual_reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '人工审核拒绝原因' AFTER `ai_reject_reason`;