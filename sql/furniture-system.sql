/*
 Navicat Premium Dump SQL

 Source Server         : MyConnect
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : furniture-system

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 26/05/2026 11:57:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`
(
    `id`           bigint   NOT NULL AUTO_INCREMENT,
    `user_id`      bigint   NOT NULL,
    `furniture_id` bigint   NOT NULL,
    `create_time`  datetime NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_furniture` (`user_id` ASC, `furniture_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------

-- ----------------------------
-- Table structure for furniture
-- ----------------------------
DROP TABLE IF EXISTS `furniture`;
CREATE TABLE `furniture`
(
    `id`      bigint                                                        NOT NULL AUTO_INCREMENT,
    `f_name`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL,
    `f_icon`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `type_id` bigint                                                        NULL DEFAULT NULL,
    `price`   decimal(10, 0)                                                NULL DEFAULT NULL,
    `brand`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `stock`   int                                                           NULL DEFAULT NULL,
    `intro`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL,
    `images`  text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '多张图片URL，逗号分隔',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `type_id` (`type_id` ASC) USING BTREE,
    CONSTRAINT `furniture_ibfk_1` FOREIGN KEY (`type_id`) REFERENCES `furniture_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `furniture_chk_1` CHECK (`stock` >= 0)
) ENGINE = InnoDB
  AUTO_INCREMENT = 13
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of furniture
-- ----------------------------
INSERT INTO `furniture`
VALUES (1, '实木餐桌',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/f9918007dfb543868b8dea885ccf64a3.jpg', 4,
        2580, '顾家', 50, '进口橡木，环保漆面，可容纳6-8人',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/0653a0da39564c5985f30092c375a88a.jpg,https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/a1e71f6e73a44b23a4ecb363a32bf16c.jpg');
INSERT INTO `furniture`
VALUES (2, '真皮沙发',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/927db786745d4e0cbbfdec0db54a993e.jpg', 1,
        5680, '顾家', 49, '头层牛皮，实木框架，三人位', '');
INSERT INTO `furniture`
VALUES (3, '席梦思床垫',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/bc7ca24b2a414907b4fa663436178e51.jpg', 2,
        3280, '顾家', 50, '独立弹簧，乳胶填充，1.8米', '');
INSERT INTO `furniture`
VALUES (4, '书桌',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/5d2eb2a906734de3a545cb1e919cd538.jpg', 3,
        1280, '顾家', 50, '简约现代，带抽屉，1.2米宽', '');
INSERT INTO `furniture`
VALUES (5, '衣柜',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/926f844c5bb24caa807003ec4e3223eb.jpg', 2,
        3680, '顾家', 50, '推拉门设计，大容量收纳，白色', '');
INSERT INTO `furniture`
VALUES (6, '餐椅',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/27f9fbb417c84c899f3ac82d94c071c2.jpg', 4,
        380, '顾家', 50, '实木椅腿，皮质坐垫，四把一套', '');
INSERT INTO `furniture`
VALUES (7, '茶几',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/5a6c2ef4f5a8478ea822672aa150e4e2.jpg', 1,
        980, '顾家', 50, '钢化玻璃台面，不锈钢支架', '');
INSERT INTO `furniture`
VALUES (8, '床头柜',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/77a4de0a6a3c45c2b61df59479a384a2.jpg', 2,
        480, '顾家', 50, '双抽屉，带USB充电口，胡桃木色', '');
INSERT INTO `furniture`
VALUES (9, '书架',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/cdaec05fc761442c919e2ea20460eb5c.jpg', 3,
        680, '顾家', 50, '五层开放式，钢木结合，省空间', '');
INSERT INTO `furniture`
VALUES (10, '鞋柜',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/d2e9cad3ed4d4ee8ac56f81fa5891e27.jpg', 1,
        580, '顾家', 50, '大容量，透气设计，带换鞋凳', '');

-- ----------------------------
-- Table structure for furniture_type
-- ----------------------------
DROP TABLE IF EXISTS `furniture_type`;
CREATE TABLE `furniture_type`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT,
    `name`        varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL,
    `icon`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `title`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `create_time` datetime                                                      NULL DEFAULT NULL,
    `update_time` datetime                                                      NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of furniture_type
-- ----------------------------
INSERT INTO `furniture_type`
VALUES (1, '门厅系列',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/type/2026/05/19/66dc373e631d452491dcdc5504c4522a.png',
        '舒适客厅必备！', '2026-03-23 17:57:50', '2026-03-23 17:57:55');
INSERT INTO `furniture_type`
VALUES (2, '卧室系列',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/type/2026/05/19/5fafeac81ada40b6b1a6e7bd9b76d42e.png',
        '温馨睡眠空间', '2026-03-23 17:58:22', '2026-03-23 17:58:25');
INSERT INTO `furniture_type`
VALUES (3, '书房系列',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/type/2026/05/19/e17b6e14b3aa4d4ab93f938cb7166c6d.png',
        '高效办公学习', '2026-03-23 17:59:31', '2026-03-23 17:59:34');
INSERT INTO `furniture_type`
VALUES (4, '餐厅系列',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/type/2026/05/19/38390154da2841d79c83ccc8d7ecee08.png',
        '品质用餐体验', '2026-03-23 17:59:49', '2026-03-23 17:59:50');

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT,
    `user_id`     bigint                                                        NULL DEFAULT NULL COMMENT '接收人ID, NULL=发送给全体用户',
    `title`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
    `content`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NOT NULL COMMENT '通知内容',
    `type`        varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT 'system' COMMENT '类型: system(系统)/order(订单)/promotion(促销)',
    `create_time` datetime                                                      NOT NULL COMMENT '发送时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_user` (`user_id` ASC) USING BTREE,
    INDEX `idx_create` (`create_time` DESC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 13
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification`
VALUES (1, NULL, '欢迎光临', '欢迎光临！！！', 'system', '2026-05-18 14:33:02');
INSERT INTO `notification`
VALUES (10, NULL, '12', '12', 'system', '2026-05-21 00:11:09');
INSERT INTO `notification`
VALUES (11, 1, '个人简历', '求职简历', 'system', '2026-05-23 18:12:00');
INSERT INTO `notification`
VALUES (12, NULL, '简历', '简历', 'system', '2026-05-23 18:16:07');

-- ----------------------------
-- Table structure for notification_read
-- ----------------------------
DROP TABLE IF EXISTS `notification_read`;
CREATE TABLE `notification_read`
(
    `id`              bigint   NOT NULL AUTO_INCREMENT,
    `notification_id` bigint   NOT NULL COMMENT '通知ID',
    `user_id`         bigint   NOT NULL COMMENT '用户ID',
    `read_time`       datetime NOT NULL COMMENT '阅读时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_notification_user` (`notification_id` ASC, `user_id` ASC) USING BTREE,
    INDEX `idx_user` (`user_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知已读记录表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification_read
-- ----------------------------
INSERT INTO `notification_read`
VALUES (1, 1, 1, '2026-05-26 10:00:00');
INSERT INTO `notification_read`
VALUES (2, 10, 1, '2026-05-26 10:00:00');
INSERT INTO `notification_read`
VALUES (3, 11, 1, '2026-05-26 10:00:00');

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`
(
    `id`           bigint                                                        NOT NULL AUTO_INCREMENT,
    `user_id`      bigint                                                        NOT NULL,
    `total_price`  decimal(10, 2)                                                NULL DEFAULT NULL,
    `status`       int                                                           NULL DEFAULT NULL,
    `consignee`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL,
    `phone`        varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL,
    `address`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `remark`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `create_time`  datetime                                                      NULL DEFAULT NULL,
    `pay_time`     datetime                                                      NULL DEFAULT NULL,
    `ship_time`    datetime                                                      NULL DEFAULT NULL,
    `receive_time` datetime                                                      NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `user_id` (`user_id` ASC) USING BTREE,
    CONSTRAINT `order_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  AUTO_INCREMENT = 2057678224726740994
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order`
VALUES (2037108025407328258, 1, 380.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-03-26 18:02:56', '2026-04-13 16:40:27', '2026-04-13 16:43:04', NULL);
INSERT INTO `order`
VALUES (2037125264768618498, 1, 380.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-03-26 19:11:26', NULL, '2026-03-30 14:55:40', NULL);
INSERT INTO `order`
VALUES (2037403055074365442, 1, 380.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-03-27 13:35:16', NULL, NULL, NULL);
INSERT INTO `order`
VALUES (2037405128662761473, 1, 380.00, 4, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-03-27 13:43:30', NULL, NULL, NULL);
INSERT INTO `order`
VALUES (2050112715682291714, 1, 380.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-05-01 15:18:55', '2026-05-01 15:19:07', '2026-05-01 15:19:30', NULL);
INSERT INTO `order`
VALUES (2056279154531528705, 1, 380.00, 5, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-05-18 15:42:09', '2026-05-18 15:42:20', '2026-05-18 16:06:14', NULL);
INSERT INTO `order`
VALUES (2057053960734547970, 1, 3680.00, 4, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-05-20 19:00:57', NULL, NULL, NULL);
INSERT INTO `order`
VALUES (2057678224726740993, 1, 5680.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-05-22 12:21:33', '2026-05-22 12:21:47', '2026-05-22 12:22:16', '2026-05-22 12:22:32');

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`
(
    `id`               bigint                                                        NOT NULL AUTO_INCREMENT,
    `order_id`         bigint                                                        NULL DEFAULT NULL,
    `furniture_id`     bigint                                                        NULL DEFAULT NULL,
    `furniture_icon`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `furniture_name`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `price`            decimal(10, 2)                                                NULL DEFAULT NULL,
    `quantity`         int                                                           NULL DEFAULT NULL,
    `item_total_price` decimal(10, 2)                                                NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `order_id` (`order_id` ASC) USING BTREE,
    INDEX `furniture_id` (`furniture_id` ASC) USING BTREE,
    CONSTRAINT `order_item_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `order_item_ibfk_2` FOREIGN KEY (`furniture_id`) REFERENCES `furniture` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  AUTO_INCREMENT = 2057678224726740995
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item`
VALUES (2037108025512185858, 2037108025407328258, 6,
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/18/b9f9555f66654f3186a941958d18b862.jpg',
        '餐椅', 380.00, 1, 380.00);
INSERT INTO `order_item`
VALUES (2037125264768618499, 2037125264768618498, 6,
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/18/b9f9555f66654f3186a941958d18b862.jpg',
        '餐椅', 380.00, 1, 380.00);
INSERT INTO `order_item`
VALUES (2037403055074365443, 2037403055074365442, 6,
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/18/b9f9555f66654f3186a941958d18b862.jpg',
        '餐椅', 380.00, 1, 380.00);
INSERT INTO `order_item`
VALUES (2037405128662761474, 2037405128662761473, 6,
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/18/b9f9555f66654f3186a941958d18b862.jpg',
        '餐椅', 380.00, 1, 380.00);
INSERT INTO `order_item`
VALUES (2050112715787149314, 2050112715682291714, 6,
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/18/b9f9555f66654f3186a941958d18b862.jpg',
        '餐椅', 380.00, 1, 380.00);
INSERT INTO `order_item`
VALUES (2056279154531528706, 2056279154531528705, 6,
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/18/b9f9555f66654f3186a941958d18b862.jpg',
        '餐椅', 380.00, 1, 380.00);
INSERT INTO `order_item`
VALUES (2057053960734547971, 2057053960734547970, 5,
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/926f844c5bb24caa807003ec4e3223eb.jpg',
        '衣柜', 3680.00, 1, 3680.00);
INSERT INTO `order_item`
VALUES (2057678224726740994, 2057678224726740993, 2,
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/927db786745d4e0cbbfdec0db54a993e.jpg',
        '真皮沙发', 5680.00, 1, 5680.00);

-- ----------------------------
-- Table structure for review
-- ----------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review`
(
    `id`           bigint                                                        NOT NULL AUTO_INCREMENT,
    `user_id`      bigint                                                        NOT NULL,
    `order_id`     bigint                                                        NULL     DEFAULT NULL,
    `furniture_id` bigint                                                        NOT NULL,
    `rating`       tinyint                                                       NOT NULL DEFAULT 5,
    `content`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL,
    `create_time`  datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_furniture_id` (`furniture_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review
-- ----------------------------
INSERT INTO `review`
VALUES (2, 1, 2056279154531528705, 6, 5, '非常好，值得推荐！', '2026-05-18 17:55:34');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`              bigint                                                        NOT NULL AUTO_INCREMENT,
    `phone`           varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL,
    `email`           varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `pass_word`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `user_name`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL,
    `icon`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `address`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `consignee`       varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL,
    `consignee_phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL,
    `create_time`     datetime                                                      NULL DEFAULT NULL,
    `is_admin`        int                                                           NULL DEFAULT 0,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user`
VALUES (1, '13483005180', '3102777566@qq.com', '$2a$10$3ku3PIB.aOrGLa1IfABf..0PKRSSCdctFjCUUgHzJLjPiZ.aDmt5a', 'LOPS',
        'https://gmc-1007.oss-cn-beijing.aliyuncs.com/avatar/2026/05/20/50bbfc8ad6a64616bcd701a83d3cce68.jpg',
        '河北省邯郸市永年区西苏乡北贾葛村', '郭名城', '13483005180', '2026-03-24 18:39:53', 1);
INSERT INTO `user`
VALUES (2, '13483005181', '3482439245@qq.com', '$2a$10$UJs8MLGk/NxxyxUiQsaR9.9Qs2I06NyIRvNTfAmM5oQyqYcKz.8H2',
        'user_MhdQvwWv8W', '', '河北省邯郸市', '名称', '13483005181', '2026-03-23 18:39:56', 0);

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT,
    `user_id`     bigint                                                        NOT NULL,
    `consignee`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL,
    `phone`       varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL,
    `address`     varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `is_default`  tinyint                                                       NULL DEFAULT 0,
    `create_time` datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_user_id` (`user_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_address
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
