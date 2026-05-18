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

 Date: 18/05/2026 14:24:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
VALUES (1, '实木餐桌', '/uploads/avatar/table.jpg', 4, 2580, '顾家', 2, '进口橡木，环保漆面，可容纳6-8人');
INSERT INTO `furniture`
VALUES (2, '真皮沙发', '/uploads/furniture/2026/03/30/938f0380fdb548c7a32ecde8b4a55fae.png', 1, 5680, '顾家', 0,
        '头层牛皮，实木框架，三人位');
INSERT INTO `furniture`
VALUES (3, '席梦思床垫', '/uploads/avatar/席梦思床垫.jpg', 2, 3280, '顾家', 0, '独立弹簧，乳胶填充，1.8米');
INSERT INTO `furniture`
VALUES (4, '书桌', '/uploads/avatar/书桌.jpg', 3, 1280, '顾家', 2, '简约现代，带抽屉，1.2米宽');
INSERT INTO `furniture`
VALUES (5, '衣柜', '/uploads/avatar/书柜.jpg', 2, 3680, '顾家', 0, '推拉门设计，大容量收纳，白色');
INSERT INTO `furniture`
VALUES (6, '餐椅', '/uploads/avatar/chair.jpg', 4, 380, '顾家', 14, '实木椅腿，皮质坐垫，四把一套');
INSERT INTO `furniture`
VALUES (7, '茶几', '/uploads/avatar/茶几.jpg', 1, 980, '顾家', 0, '钢化玻璃台面，不锈钢支架');
INSERT INTO `furniture`
VALUES (8, '床头柜', '/uploads/avatar/床头柜.jpg', 2, 480, '顾家', 2, '双抽屉，带USB充电口，胡桃木色');
INSERT INTO `furniture`
VALUES (9, '书架', '/uploads/avatar/书架.jpg', 3, 680, '顾家', 0, '五层开放式，钢木结合，省空间');
INSERT INTO `furniture`
VALUES (10, '鞋柜', '/uploads/avatar/鞋柜.jpg', 1, 580, '顾家', 7, '大容量，透气设计，带换鞋凳');

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
VALUES (1, '门厅系列', '/uploads/type/2026/03/30/2efd02ec33bf46f595a0aa59868bfdc7.png', '舒适客厅必备！',
        '2026-03-23 17:57:50', '2026-03-23 17:57:55');
INSERT INTO `furniture_type`
VALUES (2, '卧室系列', '/uploads/type/2026/03/30/47384f25b4ce455aa552e7ab72ae5bac.png', '温馨睡眠空间',
        '2026-03-23 17:58:22', '2026-03-23 17:58:25');
INSERT INTO `furniture_type`
VALUES (3, '书房系列', '/images/study.png', '高效办公学习', '2026-03-23 17:59:31', '2026-03-23 17:59:34');
INSERT INTO `furniture_type`
VALUES (4, '餐厅系列', '/images/canteen.png', '品质用餐体验', '2026-03-23 17:59:49', '2026-03-23 17:59:50');

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT,
    `user_id`     bigint                                                        NOT NULL,
    `total_price` decimal(10, 2)                                                NULL DEFAULT NULL,
    `status`      int                                                           NULL DEFAULT NULL,
    `consignee`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL,
    `phone`       varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL,
    `address`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `remark`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `create_time` datetime                                                      NULL DEFAULT NULL,
    `pay_time`    datetime                                                      NULL DEFAULT NULL,
    `ship_time`   datetime                                                      NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `user_id` (`user_id` ASC) USING BTREE,
    CONSTRAINT `order_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  AUTO_INCREMENT = 2050112715682291715
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order`
VALUES (2037108025407328258, 1, 380.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-03-26 18:02:56', '2026-04-13 16:40:27', '2026-04-13 16:43:04');
INSERT INTO `order`
VALUES (2037125264768618498, 1, 380.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-03-26 19:11:26', NULL, '2026-03-30 14:55:40');
INSERT INTO `order`
VALUES (2037403055074365442, 1, 380.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-03-27 13:35:16', NULL, NULL);
INSERT INTO `order`
VALUES (2037405128662761473, 1, 380.00, 4, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-03-27 13:43:30', NULL, NULL);
INSERT INTO `order`
VALUES (2050112715682291714, 1, 380.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '',
        '2026-05-01 15:18:55', '2026-05-01 15:19:07', '2026-05-01 15:19:30');

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
  AUTO_INCREMENT = 2050112715787149315
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item`
VALUES (2037108025512185858, 2037108025407328258, 6, '/uploads/avatar/chair.jpg', '餐椅', 380.00, 1, 380.00);
INSERT INTO `order_item`
VALUES (2037125264768618499, 2037125264768618498, 6, '/uploads/avatar/chair.jpg', '餐椅', 380.00, 1, 380.00);
INSERT INTO `order_item`
VALUES (2037403055074365443, 2037403055074365442, 6, '/uploads/avatar/chair.jpg', '餐椅', 380.00, 1, 380.00);
INSERT INTO `order_item`
VALUES (2037405128662761474, 2037405128662761473, 6, '/uploads/avatar/chair.jpg', '餐椅', 380.00, 1, 380.00);
INSERT INTO `order_item`
VALUES (2050112715787149314, 2050112715682291714, 6, '/uploads/avatar/chair.jpg', '餐椅', 380.00, 1, 380.00);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`              bigint                                                        NOT NULL AUTO_INCREMENT,
    `phone`           varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL,
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
VALUES (1, '13483005180', '$2a$10$qzs19TAWQKt8kqMVxCtBIeouKl0bn0PNIIoU4wfVDXFWKJcm582Di', 'LOPS',
        '/uploads/avatar/2026/03/26/9d2e0c0affd34cba9520a4104fe005df.jpg', '河北省邯郸市永年区西苏乡北贾葛村', '郭名城',
        '13483005180', '2026-03-24 18:39:53', 1);
INSERT INTO `user`
VALUES (2, '13483005181', '$2a$10$UJs8MLGk/NxxyxUiQsaR9.9Qs2I06NyIRvNTfAmM5oQyqYcKz.8H2', 'user_MhdQvwWv8W', '', NULL,
        NULL, NULL, '2026-03-23 18:39:56', 0);

SET FOREIGN_KEY_CHECKS = 1;
