CREATE TABLE IF NOT EXISTS `favorite`
(
    `id`           bigint NOT NULL AUTO_INCREMENT,
    `user_id`      bigint NOT NULL,
    `furniture_id` bigint NOT NULL,
    `create_time`  datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_furniture` (`user_id`, `furniture_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
