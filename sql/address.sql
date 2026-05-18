CREATE TABLE IF NOT EXISTS `user_address`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `user_id`     bigint       NOT NULL,
    `consignee`   varchar(50)  NOT NULL,
    `phone`       varchar(20)  NOT NULL,
    `address`     varchar(200) NOT NULL,
    `is_default`  tinyint  DEFAULT 0,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
