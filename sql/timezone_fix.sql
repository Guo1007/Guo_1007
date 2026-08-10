-- ============================================
-- 时区修复：历史数据 UTC → 北京时间（+8 小时）
-- ============================================
-- 适用场景：修改配置（JDBC serverTimezone=Asia/Shanghai + MySQL 东八区）后，
--           若发现**历史数据**仍比实际时间少 8 小时（即历史以 UTC 存储），
--           执行本脚本将所有时间字段 +8 小时。
-- ⚠️ 执行前先确认：当前数据库中时间是否确为 UTC（比北京少 8 小时）。
--    若本已是北京时间，**不要**执行本脚本，否则会多 8 小时。
-- ⚠️ 建议先在测试库执行验证，再在正式库执行。

-- 订单
UPDATE `order`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR),
       pay_time    = DATE_ADD(pay_time, INTERVAL 8 HOUR),
       ship_time   = DATE_ADD(ship_time, INTERVAL 8 HOUR),
       receive_time = DATE_ADD(receive_time, INTERVAL 8 HOUR),
       refund_apply_time = DATE_ADD(refund_apply_time, INTERVAL 8 HOUR),
       refund_approve_time = DATE_ADD(refund_approve_time, INTERVAL 8 HOUR),
       refund_audit_time = DATE_ADD(refund_audit_time, INTERVAL 8 HOUR)
 WHERE create_time IS NOT NULL;

-- 订单明细
UPDATE `order_item`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR),
       update_time = DATE_ADD(update_time, INTERVAL 8 HOUR);

-- 商品
UPDATE `furniture`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR),
       update_time = DATE_ADD(update_time, INTERVAL 8 HOUR);

-- 商品评价 / 追评 / 评价评论
UPDATE `goods_comment`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR),
       latest_append_time = DATE_ADD(latest_append_time, INTERVAL 8 HOUR);
UPDATE `comment_append`
   SET append_time = DATE_ADD(append_time, INTERVAL 8 HOUR);
UPDATE `review_comment`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR);

-- 用户 / 地址 / 收藏
UPDATE `user`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR),
       update_time = DATE_ADD(update_time, INTERVAL 8 HOUR);
UPDATE `user_address`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR);
UPDATE `favorite`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR);

-- 通知 / 用户通知
UPDATE `notification`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR);
UPDATE `user_notification`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR),
       read_time   = DATE_ADD(read_time, INTERVAL 8 HOUR),
       update_time = DATE_ADD(update_time, INTERVAL 8 HOUR);

-- 站点内容
UPDATE `site_content`
   SET updated_at = DATE_ADD(updated_at, INTERVAL 8 HOUR);

-- SKU / 规格
UPDATE `sku`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR),
       update_time = DATE_ADD(update_time, INTERVAL 8 HOUR);
UPDATE `sku_spec`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR);
UPDATE `spec_group`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR),
       update_time = DATE_ADD(update_time, INTERVAL 8 HOUR);
UPDATE `spec_value`
   SET create_time = DATE_ADD(create_time, INTERVAL 8 HOUR),
       update_time = DATE_ADD(update_time, INTERVAL 8 HOUR);
