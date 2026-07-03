-- 通知表增加 reviewId、goodsId 和 reviewCommentId，用于评论回复通知跳转
ALTER TABLE `notification`
    ADD COLUMN `review_id`          bigint NULL DEFAULT NULL COMMENT '关联评价ID(type=comment_reply时有值)',
    ADD COLUMN `goods_id`           bigint NULL DEFAULT NULL COMMENT '关联商品ID(type=comment_reply时有值,用于前端跳转)',
    ADD COLUMN `review_comment_id`  bigint NULL DEFAULT NULL COMMENT '关联评论ID(type=comment_reply时有值,用于定位到具体回复)';
