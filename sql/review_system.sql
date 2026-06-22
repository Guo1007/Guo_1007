-- ============================================
-- 电商评价系统完整表结构
-- ============================================

-- 1. 商品主评价表（第一次下单评价）
CREATE TABLE IF NOT EXISTS goods_comment
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id           BIGINT NOT NULL COMMENT '订单号',
    order_item_id      BIGINT COMMENT '订单项id',
    goods_id           BIGINT NOT NULL COMMENT '商品id',
    user_id            BIGINT NOT NULL COMMENT '用户id',
    score              INT    NOT NULL COMMENT '星级(1-5)',
    content            VARCHAR(500)  DEFAULT NULL COMMENT '评价文字',
    img_url            VARCHAR(1000) DEFAULT NULL COMMENT '评价图片(JSON数组)',
    video_url          VARCHAR(500)  DEFAULT NULL COMMENT '评价视频',
    is_anonym          TINYINT       DEFAULT 0 COMMENT '是否匿名(0否1是)',
    status             TINYINT       DEFAULT 1 COMMENT '审核状态(0待审1通过2拒绝)',
    has_append         TINYINT       DEFAULT 0 COMMENT '是否有追评',
    latest_append_time DATETIME      DEFAULT NULL COMMENT '最新追评时间',
    create_time        DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    INDEX idx_goods_id (goods_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品主评价表';

-- 2. 追评明细表
CREATE TABLE IF NOT EXISTS comment_append
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    main_comment_id BIGINT NOT NULL COMMENT '主评价id',
    user_id         BIGINT NOT NULL COMMENT '用户id',
    append_content  VARCHAR(500)  DEFAULT NULL COMMENT '追评文字',
    append_img      VARCHAR(1000) DEFAULT NULL COMMENT '追评图片(JSON数组)',
    append_num      INT    NOT NULL COMMENT '第几次追评(1/2)',
    status          TINYINT       DEFAULT 1 COMMENT '审核状态(0待审1通过2拒绝)',
    append_time     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '追评时间',
    INDEX idx_main_comment_id (main_comment_id),
    INDEX idx_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='追评明细表';

-- ============================================
-- 数据迁移：将旧 review 表数据迁移到新表（如旧表已删除可跳过）
-- ============================================

-- 迁移评价数据（如果旧表有数据）
-- INSERT INTO goods_comment (order_id, goods_id, user_id, score, content, img_url, video_url, create_time)
-- SELECT order_id, furniture_id, user_id, rating, content, images, video, create_time
-- FROM review
-- WHERE NOT EXISTS (
--     SELECT 1 FROM goods_comment gc
--     WHERE gc.order_id = review.order_id
--     AND gc.goods_id = review.furniture_id
--     AND gc.user_id = review.user_id
-- );

-- ============================================
-- 3. 评价评论表（评论区：其他用户对评价的评论/讨论）
-- ============================================
CREATE TABLE IF NOT EXISTS review_comment
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id           BIGINT       NOT NULL COMMENT '主评价id(goods_comment.id)',
    user_id             BIGINT       NOT NULL COMMENT '评论用户id',
    content             VARCHAR(500) NOT NULL COMMENT '评论内容',
    reply_to_user_id    BIGINT   DEFAULT NULL COMMENT '回复的目标用户id(为空则为普通评论)',
    reply_to_comment_id BIGINT   DEFAULT NULL COMMENT '回复的评论id(为空则为一级评论)',
    status              TINYINT  DEFAULT 1 COMMENT '状态(0删除1正常)',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    INDEX idx_review_id (review_id),
    INDEX idx_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='评价评论表';
