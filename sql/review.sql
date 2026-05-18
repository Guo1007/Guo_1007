CREATE TABLE IF NOT EXISTS `review`
(
    `id`           bigint  NOT NULL AUTO_INCREMENT,
    `user_id`      bigint  NOT NULL,
    `order_id`     bigint           DEFAULT NULL,
    `furniture_id` bigint  NOT NULL,
    `rating`       tinyint NOT NULL DEFAULT 5,
    `content`      varchar(500)     DEFAULT NULL,
    `create_time`  datetime         DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_furniture_id` (`furniture_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
