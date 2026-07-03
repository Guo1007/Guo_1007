-- 评价评论体系三张核心表
CREATE TABLE IF NOT EXISTS `goods_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单号',
  `order_item_id` bigint NULL DEFAULT NULL COMMENT '订单项id',
  `goods_id` bigint NOT NULL COMMENT '商品id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `score` int NOT NULL COMMENT '星级(1-5)',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价文字',
  `img_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价图片(JSON数组)',
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价视频',
  `is_anonym` tinyint NULL DEFAULT 0 COMMENT '是否匿名(0否1是)',
  `status` tinyint NULL DEFAULT 1 COMMENT '审核状态(0待审1通过2拒绝)',
  `has_append` tinyint NULL DEFAULT 0 COMMENT '是否有追评',
  `latest_append_time` datetime NULL DEFAULT NULL COMMENT '最新追评时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品主评价表' ROW_FORMAT = Dynamic;

CREATE TABLE IF NOT EXISTS `comment_append` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `main_comment_id` bigint NOT NULL COMMENT '主评价id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `append_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '追评文字',
  `append_img` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '追评图片(JSON数组)',
  `append_num` int NOT NULL COMMENT '第几次追评(1/2)',
  `status` tinyint NULL DEFAULT 1 COMMENT '审核状态(0待审1通过2拒绝)',
  `append_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '追评时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_main_comment_id`(`main_comment_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '追评明细表' ROW_FORMAT = Dynamic;

CREATE TABLE IF NOT EXISTS `review_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `review_id` bigint NOT NULL COMMENT '主评价id(goods_comment.id)',
  `user_id` bigint NOT NULL COMMENT '评论用户id',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `reply_to_user_id` bigint NULL DEFAULT NULL COMMENT '回复的目标用户id(为空则为普通评论)',
  `reply_to_comment_id` bigint NULL DEFAULT NULL COMMENT '回复的评论id(为空则为一级评论)',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0删除1正常)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_review_id`(`review_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价评论表' ROW_FORMAT = Dynamic;
