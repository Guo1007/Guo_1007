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

 Date: 10/07/2026 15:15:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for comment_append
-- ----------------------------
DROP TABLE IF EXISTS `comment_append`;
CREATE TABLE `comment_append`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `main_comment_id` bigint NOT NULL COMMENT '主评价id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `append_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '追评文字',
  `append_img` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '追评图片(JSON数组)',
  `append_num` int NOT NULL COMMENT '第几次追评(1/2)',
  `status` tinyint NULL DEFAULT 1 COMMENT '审核状态(0待审1通过2拒绝)',
  `append_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '追评时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_main_comment_id`(`main_comment_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_main_append_num`(`main_comment_id` ASC, `append_num` ASC) USING BTREE,
  CONSTRAINT `fk_comment_append_main` FOREIGN KEY (`main_comment_id`) REFERENCES `goods_comment` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_comment_append_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '追评明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment_append
-- ----------------------------
INSERT INTO `comment_append` VALUES (1, 2, 2, '还可以吧', '', 1, 1, '2026-06-22 14:51:20', 0);
INSERT INTO `comment_append` VALUES (2, 10, 1, '确实', '[\"https://gmc-1007.oss-cn-beijing.aliyuncs.com/comment/image/2026/06/22/1896557540fb48b5ac3d8042c5c0e850.jpg\"]', 1, 1, '2026-06-22 15:14:09', 1);

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `furniture_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_furniture`(`user_id` ASC, `furniture_id` ASC) USING BTREE,
  INDEX `fk_favorite_furniture`(`furniture_id` ASC) USING BTREE,
  CONSTRAINT `fk_favorite_furniture` FOREIGN KEY (`furniture_id`) REFERENCES `furniture` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (2, 1, 5, '2026-05-26 14:07:46');
INSERT INTO `favorite` VALUES (4, 1, 2, '2026-06-22 11:39:06');
INSERT INTO `favorite` VALUES (5, 1, 7, '2026-06-22 11:39:08');
INSERT INTO `favorite` VALUES (6, 1, 10, '2026-06-22 11:39:10');
INSERT INTO `favorite` VALUES (7, 1, 3, '2026-06-22 11:39:15');
INSERT INTO `favorite` VALUES (8, 1, 8, '2026-06-22 11:39:20');
INSERT INTO `favorite` VALUES (9, 1, 4, '2026-06-22 11:39:23');
INSERT INTO `favorite` VALUES (10, 1, 9, '2026-06-22 11:39:26');
INSERT INTO `favorite` VALUES (11, 1, 1, '2026-06-22 11:39:32');
INSERT INTO `favorite` VALUES (12, 1, 6, '2026-06-22 11:39:34');

-- ----------------------------
-- Table structure for furniture
-- ----------------------------
DROP TABLE IF EXISTS `furniture`;
CREATE TABLE `furniture`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `f_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `f_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `type_id` bigint NULL DEFAULT NULL,
  `price` decimal(10, 0) NULL DEFAULT NULL,
  `brand` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `stock` int NULL DEFAULT NULL,
  `intro` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '多张图片URL，逗号分隔',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品详情描述',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `type_id`(`type_id` ASC) USING BTREE,
  CONSTRAINT `furniture_ibfk_1` FOREIGN KEY (`type_id`) REFERENCES `furniture_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `furniture_chk_1` CHECK (`stock` >= 0)
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of furniture
-- ----------------------------
INSERT INTO `furniture` VALUES (1, '实木餐桌', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/06/14/f242a16936f445d28a6a377cae588b7c.jpg', 4, 210, '顾家', 30, '进口橡木，环保漆面，可容纳6-8人', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/0653a0da39564c5985f30092c375a88a.jpg,https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/a1e71f6e73a44b23a4ecb363a32bf16c.jpg', '', 0);
INSERT INTO `furniture` VALUES (2, '真皮沙发', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/927db786745d4e0cbbfdec0db54a993e.jpg', 1, 500, '顾家', 40, '头层牛皮，实木框架，三人位', '', NULL, 0);
INSERT INTO `furniture` VALUES (3, '席梦思床垫', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/bc7ca24b2a414907b4fa663436178e51.jpg', 2, 100, '顾家', 20, '独立弹簧，乳胶填充，1.8米', '', NULL, 0);
INSERT INTO `furniture` VALUES (4, '书桌', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/5d2eb2a906734de3a545cb1e919cd538.jpg', 3, 90, '顾家', 60, '简约现代，带抽屉，1.2米宽', '', NULL, 0);
INSERT INTO `furniture` VALUES (5, '衣柜', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/926f844c5bb24caa807003ec4e3223eb.jpg', 2, 500, '顾家', 32, '推拉门设计，大容量收纳，白色', '', NULL, 0);
INSERT INTO `furniture` VALUES (6, '餐椅', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/27f9fbb417c84c899f3ac82d94c071c2.jpg', 4, 50, '顾家', 90, '实木椅腿，皮质坐垫，四把一套', '', NULL, 0);
INSERT INTO `furniture` VALUES (7, '茶几', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/5a6c2ef4f5a8478ea822672aa150e4e2.jpg', 1, 600, '顾家', 30, '钢化玻璃台面，不锈钢支架', '', NULL, 0);
INSERT INTO `furniture` VALUES (8, '床头柜', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/77a4de0a6a3c45c2b61df59479a384a2.jpg', 2, 200, '顾家', 40, '双抽屉，带USB充电口，胡桃木色', '', NULL, 0);
INSERT INTO `furniture` VALUES (9, '书架', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/cdaec05fc761442c919e2ea20460eb5c.jpg', 3, 200, '顾家', 15, '五层开放式，钢木结合，省空间', '', NULL, 0);
INSERT INTO `furniture` VALUES (10, '鞋柜', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/d2e9cad3ed4d4ee8ac56f81fa5891e27.jpg', 1, 150, '顾家', 30, '大容量，透气设计，带换鞋凳', '', NULL, 0);

-- ----------------------------
-- Table structure for furniture_type
-- ----------------------------
DROP TABLE IF EXISTS `furniture_type`;
CREATE TABLE `furniture_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of furniture_type
-- ----------------------------
INSERT INTO `furniture_type` VALUES (1, '门厅系列', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/type/2026/05/19/66dc373e631d452491dcdc5504c4522a.png', '舒适客厅必备！', '2026-03-23 17:57:50', '2026-03-23 17:57:55', 0);
INSERT INTO `furniture_type` VALUES (2, '卧室系列', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/type/2026/05/19/5fafeac81ada40b6b1a6e7bd9b76d42e.png', '温馨睡眠空间', '2026-03-23 17:58:22', '2026-03-23 17:58:25', 0);
INSERT INTO `furniture_type` VALUES (3, '书房系列', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/type/2026/05/19/e17b6e14b3aa4d4ab93f938cb7166c6d.png', '高效办公学习', '2026-03-23 17:59:31', '2026-03-23 17:59:34', 0);
INSERT INTO `furniture_type` VALUES (4, '餐厅系列', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/type/2026/05/19/38390154da2841d79c83ccc8d7ecee08.png', '品质用餐体验', '2026-03-23 17:59:49', '2026-03-23 17:59:50', 0);

-- ----------------------------
-- Table structure for goods_comment
-- ----------------------------
DROP TABLE IF EXISTS `goods_comment`;
CREATE TABLE `goods_comment`  (
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
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `fk_goods_comment_order_item`(`order_item_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_order_user_goods`(`order_id` ASC, `user_id` ASC, `goods_id` ASC) USING BTREE,
  CONSTRAINT `fk_goods_comment_furniture` FOREIGN KEY (`goods_id`) REFERENCES `furniture` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_goods_comment_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_goods_comment_order_item` FOREIGN KEY (`order_item_id`) REFERENCES `order_item` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_goods_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品主评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods_comment
-- ----------------------------
INSERT INTO `goods_comment` VALUES (1, 2056279154531528705, NULL, 6, 1, 5, '非常好，值得推荐！', NULL, NULL, 0, 1, 0, NULL, '2026-05-18 17:55:34', 0);
INSERT INTO `goods_comment` VALUES (2, 2059155009276502018, NULL, 6, 2, 3, '一般般', NULL, NULL, 0, 1, 1, '2026-06-22 14:51:20', '2026-05-26 14:10:53', 0);
INSERT INTO `goods_comment` VALUES (3, 2068906960379793409, NULL, 1, 1, 5, '可以的', NULL, NULL, 0, 1, 0, NULL, '2026-06-22 12:01:57', 0);
INSERT INTO `goods_comment` VALUES (4, 2068931536669130754, NULL, 6, 1, 5, '可以', NULL, NULL, 0, 1, 0, NULL, '2026-06-22 13:38:39', 0);
INSERT INTO `goods_comment` VALUES (5, 2068932766556504066, NULL, 6, 1, 5, '1', NULL, NULL, 0, 1, 0, NULL, '2026-06-22 13:43:41', 0);
INSERT INTO `goods_comment` VALUES (10, 2068937957070610434, NULL, 6, 1, 5, '不错', '[\"https://gmc-1007.oss-cn-beijing.aliyuncs.com/comment/image/2026/06/22/c5d7b3b599934ce6ba0b862c34939c2b.jpg\"]', '', 0, 1, 1, '2026-06-22 15:14:09', '2026-06-22 14:17:35', 1);
INSERT INTO `goods_comment` VALUES (11, 2068957884301307905, NULL, 6, 2, 5, '可以', '', '', 0, 1, 0, NULL, '2026-06-22 15:23:26', 0);

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL COMMENT '接收人ID, NULL=发送给全体用户',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'system' COMMENT '类型: system(系统)/order(订单)/promotion(促销)',
  `create_time` datetime NOT NULL COMMENT '发送时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  `review_id` bigint NULL DEFAULT NULL,
  `goods_id` bigint NULL DEFAULT NULL,
  `review_comment_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_create`(`create_time` DESC) USING BTREE,
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (1, NULL, '欢迎光临', '欢迎光临！！！', 'system', '2026-05-18 14:33:02', 0, NULL, NULL, NULL);
INSERT INTO `notification` VALUES (10, NULL, '12', '12', 'system', '2026-05-21 00:11:09', 1, NULL, NULL, NULL);
INSERT INTO `notification` VALUES (11, 1, '个人简历', '求职简历', 'system', '2026-05-23 18:12:00', 1, NULL, NULL, NULL);
INSERT INTO `notification` VALUES (12, NULL, '简历', '简历', 'system', '2026-05-23 18:16:07', 1, NULL, NULL, NULL);
INSERT INTO `notification` VALUES (13, NULL, '12', '12', 'system', '2026-05-26 14:08:10', 1, NULL, NULL, NULL);
INSERT INTO `notification` VALUES (14, 1, '收到新回复', 'LOPS 回复了你的评论', 'comment_reply', '2026-06-22 17:39:35', 0, 3, 1, NULL);
INSERT INTO `notification` VALUES (15, 2, '收到新回复', 'Glimcy 回复了你的评论', 'comment_reply', '2026-07-03 11:21:26', 0, 3, 1, 15);
INSERT INTO `notification` VALUES (16, NULL, '测试', '测试', 'system', '2026-07-08 10:41:30', 1, NULL, NULL, NULL);
INSERT INTO `notification` VALUES (17, NULL, '1', '1', 'order', '2026-07-08 11:25:08', 1, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `total_price` decimal(10, 2) NULL DEFAULT NULL,
  `status` int NULL DEFAULT NULL,
  `consignee` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `pay_time` datetime NULL DEFAULT NULL,
  `ship_time` datetime NULL DEFAULT NULL,
  `receive_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  `user_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '用户删除(0未删/1已删)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `order_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2074027699771478018 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (2037108025407328258, 1, 380.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '', '2026-03-26 18:02:56', '2026-04-13 16:40:27', '2026-04-13 16:43:04', NULL, NULL, 0, 0);
INSERT INTO `order` VALUES (2037125264768618498, 1, 380.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '', '2026-03-26 19:11:26', NULL, '2026-03-30 14:55:40', NULL, NULL, 0, 0);
INSERT INTO `order` VALUES (2037403055074365442, 1, 380.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '', '2026-03-27 13:35:16', NULL, NULL, NULL, NULL, 0, 0);
INSERT INTO `order` VALUES (2037405128662761473, 1, 380.00, 4, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '', '2026-03-27 13:43:30', NULL, NULL, NULL, NULL, 0, 0);
INSERT INTO `order` VALUES (2050112715682291714, 1, 380.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '', '2026-05-01 15:18:55', '2026-05-01 15:19:07', '2026-05-01 15:19:30', NULL, NULL, 0, 0);
INSERT INTO `order` VALUES (2056279154531528705, 1, 380.00, 5, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '', '2026-05-18 15:42:09', '2026-05-18 15:42:20', '2026-05-18 16:06:14', NULL, NULL, 0, 0);
INSERT INTO `order` VALUES (2057053960734547970, 1, 3680.00, 4, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '', '2026-05-20 19:00:57', NULL, NULL, NULL, NULL, 0, 0);
INSERT INTO `order` VALUES (2057678224726740993, 1, 5680.00, 3, 'LOPS', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '', '2026-05-22 12:21:33', '2026-05-22 12:21:47', '2026-05-22 12:22:16', '2026-05-22 12:22:32', NULL, 0, 0);
INSERT INTO `order` VALUES (2059155009276502018, 2, 380.00, 5, 'LOPS', '13483005181', '河北省邯郸市', '', '2026-05-26 14:09:46', '2026-05-26 14:09:51', NULL, NULL, NULL, 0, 0);
INSERT INTO `order` VALUES (2066819535757152258, 1, 2580.00, 3, '郭晨阳', '13483005180', '河北省邯郸市永年区西苏乡北贾葛村', '', '2026-06-16 17:45:52', '2026-06-16 17:45:57', '2026-06-16 18:05:48', '2026-07-10 14:57:31', '2026-07-10 14:57:30', 0, 0);
INSERT INTO `order` VALUES (2066823026185637890, 1, 380.00, 4, '郭名城', '13444444444', 'UK', '', '2026-06-16 17:59:44', NULL, NULL, NULL, NULL, 0, 0);
INSERT INTO `order` VALUES (2066823965957197826, 1, 3280.00, 4, '郭名城', '13444444444', 'UK', '', '2026-06-16 18:03:28', NULL, NULL, NULL, NULL, 0, 0);
INSERT INTO `order` VALUES (2066824117648396289, 1, 3000.00, 4, '郭名城', '13444444444', 'UK', '', '2026-06-16 18:04:04', NULL, NULL, NULL, NULL, 0, 0);
INSERT INTO `order` VALUES (2066824489049821185, 1, 3000.00, 4, '郭名城', '13444444444', 'UK', '', '2026-06-16 18:05:33', NULL, NULL, NULL, '2026-07-10 15:15:14', 0, 1);
INSERT INTO `order` VALUES (2068906960379793409, 1, 2580.00, 5, 'Glimcy', '13483005180', 'UK', '', '2026-06-22 12:00:32', '2026-06-22 12:00:39', '2026-06-22 12:01:12', '2026-06-22 12:01:31', NULL, 0, 0);
INSERT INTO `order` VALUES (2068931536669130754, 1, 380.00, 5, '郭名城', '13444444444', 'UK', '', '2026-06-22 13:38:12', '2026-06-22 13:38:16', '2026-06-22 13:38:26', '2026-06-22 13:38:33', NULL, 0, 0);
INSERT INTO `order` VALUES (2068932766556504066, 1, 380.00, 5, '郭名城', '13444444444', 'UK', '', '2026-06-22 13:43:05', '2026-06-22 13:43:11', '2026-06-22 13:43:22', '2026-06-22 13:43:28', NULL, 0, 0);
INSERT INTO `order` VALUES (2068937957070610434, 1, 380.00, 5, '郭名城', '13444444444', 'UK', '', '2026-06-22 14:03:43', '2026-06-22 14:03:46', '2026-06-22 14:03:51', '2026-06-22 14:04:04', NULL, 0, 0);
INSERT INTO `order` VALUES (2068957884301307905, 2, 380.00, 5, 'LOPS', '13483005181', '邯郸市', '', '2026-06-22 15:22:54', '2026-06-22 15:22:58', '2026-06-22 15:23:08', '2026-06-22 15:23:20', NULL, 0, 0);
INSERT INTO `order` VALUES (2074016455911972865, 1, 380.00, 4, '郭名城', '13444444444', 'UK', '', '2026-07-06 14:23:51', NULL, NULL, NULL, '2026-07-10 15:15:01', 0, 1);
INSERT INTO `order` VALUES (2074021113363607553, 1, 480.00, 4, '郭名城', '13444444444', 'UK', '', '2026-07-06 14:42:22', NULL, NULL, NULL, '2026-07-10 15:14:58', 0, 1);
INSERT INTO `order` VALUES (2074021187003002881, 1, 380.00, 4, '郭名城', '13444444444', 'UK', '', '2026-07-06 14:42:39', NULL, NULL, NULL, '2026-07-10 15:14:56', 0, 1);
INSERT INTO `order` VALUES (2074027699771478017, 1, 510.00, 4, '郭名城', '13444444444', 'UK', '', '2026-07-06 15:08:32', NULL, NULL, NULL, '2026-07-10 15:14:53', 0, 1);

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NULL DEFAULT NULL,
  `furniture_id` bigint NULL DEFAULT NULL,
  `sku_id` bigint NULL DEFAULT NULL COMMENT 'SKU ID',
  `furniture_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `furniture_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `price` decimal(10, 2) NULL DEFAULT NULL,
  `quantity` int NULL DEFAULT NULL,
  `sku_spec` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格快照，如：颜色:米白,尺寸:三人位',
  `item_total_price` decimal(10, 2) NULL DEFAULT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  INDEX `furniture_id`(`furniture_id` ASC) USING BTREE,
  INDEX `fk_order_item_sku`(`sku_id` ASC) USING BTREE,
  CONSTRAINT `fk_order_item_sku` FOREIGN KEY (`sku_id`) REFERENCES `sku` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `order_item_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `order_item_ibfk_2` FOREIGN KEY (`furniture_id`) REFERENCES `furniture` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2074027699771478020 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (2037108025512185858, 2037108025407328258, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/18/b9f9555f66654f3186a941958d18b862.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2037125264768618499, 2037125264768618498, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/18/b9f9555f66654f3186a941958d18b862.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2037403055074365443, 2037403055074365442, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/18/b9f9555f66654f3186a941958d18b862.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2037405128662761474, 2037405128662761473, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/18/b9f9555f66654f3186a941958d18b862.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2050112715787149314, 2050112715682291714, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/18/b9f9555f66654f3186a941958d18b862.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2056279154531528706, 2056279154531528705, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/18/b9f9555f66654f3186a941958d18b862.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2057053960734547971, 2057053960734547970, 5, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/926f844c5bb24caa807003ec4e3223eb.jpg', '衣柜', 3680.00, 1, NULL, 3680.00, 0);
INSERT INTO `order_item` VALUES (2057678224726740994, 2057678224726740993, 2, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/927db786745d4e0cbbfdec0db54a993e.jpg', '真皮沙发', 5680.00, 1, NULL, 5680.00, 0);
INSERT INTO `order_item` VALUES (2059155009276502019, 2059155009276502018, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/27f9fbb417c84c899f3ac82d94c071c2.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2066819535937507330, 2066819535757152258, 1, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/06/14/f242a16936f445d28a6a377cae588b7c.jpg', '实木餐桌', 2580.00, 1, '颜色:黑色', 2580.00, 0);
INSERT INTO `order_item` VALUES (2066823026185637891, 2066823026185637890, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/27f9fbb417c84c899f3ac82d94c071c2.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2066823965957197827, 2066823965957197826, 3, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/bc7ca24b2a414907b4fa663436178e51.jpg', '席梦思床垫', 3280.00, 1, NULL, 3280.00, 0);
INSERT INTO `order_item` VALUES (2066824117711310849, 2066824117648396289, 1, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/06/14/f242a16936f445d28a6a377cae588b7c.jpg', '实木餐桌', 3000.00, 1, '颜色:白色', 3000.00, 0);
INSERT INTO `order_item` VALUES (2066824489049821186, 2066824489049821185, 1, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/06/14/f242a16936f445d28a6a377cae588b7c.jpg', '实木餐桌', 3000.00, 1, '颜色:白色', 3000.00, 0);
INSERT INTO `order_item` VALUES (2068906960392376322, 2068906960379793409, 1, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/06/14/f242a16936f445d28a6a377cae588b7c.jpg', '实木餐桌', 2580.00, 1, '颜色:黑色', 2580.00, 0);
INSERT INTO `order_item` VALUES (2068931536669130755, 2068931536669130754, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/27f9fbb417c84c899f3ac82d94c071c2.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2068932766556504067, 2068932766556504066, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/27f9fbb417c84c899f3ac82d94c071c2.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2068937957070610435, 2068937957070610434, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/27f9fbb417c84c899f3ac82d94c071c2.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2068957884301307906, 2068957884301307905, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/27f9fbb417c84c899f3ac82d94c071c2.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2074016456083939330, 2074016455911972865, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/27f9fbb417c84c899f3ac82d94c071c2.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2074021113363607554, 2074021113363607553, 8, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/77a4de0a6a3c45c2b61df59479a384a2.jpg', '床头柜', 480.00, 1, NULL, 480.00, 0);
INSERT INTO `order_item` VALUES (2074021187003002882, 2074021187003002881, 6, NULL, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/05/19/27f9fbb417c84c899f3ac82d94c071c2.jpg', '餐椅', 380.00, 1, NULL, 380.00, 0);
INSERT INTO `order_item` VALUES (2074027699771478018, 2074027699771478017, 1, 31, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/06/14/f242a16936f445d28a6a377cae588b7c.jpg', '实木餐桌', 210.00, 1, '大小:0.8×0.8,颜色:黑色', 210.00, 0);
INSERT INTO `order_item` VALUES (2074027699771478019, 2074027699771478017, 1, 29, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/06/14/f242a16936f445d28a6a377cae588b7c.jpg', '实木餐桌', 300.00, 1, '大小:1.5×1.5,颜色:黑色', 300.00, 0);

-- ----------------------------
-- Table structure for review_comment
-- ----------------------------
DROP TABLE IF EXISTS `review_comment`;
CREATE TABLE `review_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `review_id` bigint NOT NULL COMMENT '主评价id(goods_comment.id)',
  `user_id` bigint NOT NULL COMMENT '评论用户id',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `reply_to_user_id` bigint NULL DEFAULT NULL COMMENT '回复的目标用户id(为空则为普通评论)',
  `reply_to_comment_id` bigint NULL DEFAULT NULL COMMENT '回复的评论id(为空则为一级评论)',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0删除1正常)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_review_id`(`review_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `fk_review_comment_reply_user`(`reply_to_user_id` ASC) USING BTREE,
  INDEX `fk_review_comment_parent`(`reply_to_comment_id` ASC) USING BTREE,
  CONSTRAINT `fk_review_comment_main` FOREIGN KEY (`review_id`) REFERENCES `goods_comment` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_review_comment_parent` FOREIGN KEY (`reply_to_comment_id`) REFERENCES `review_comment` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_review_comment_reply_user` FOREIGN KEY (`reply_to_user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_review_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review_comment
-- ----------------------------
INSERT INTO `review_comment` VALUES (1, 2, 2, '行吧', 2, 1, 1, '2026-06-22 14:49:58', 0);
INSERT INTO `review_comment` VALUES (2, 2, 2, '想', 2, 1, 1, '2026-06-22 14:50:09', 0);
INSERT INTO `review_comment` VALUES (4, 11, 1, '好的', NULL, NULL, 1, '2026-06-22 15:57:08', 0);
INSERT INTO `review_comment` VALUES (9, 3, 2, '你好', 1, NULL, 1, '2026-06-22 17:13:23', 0);
INSERT INTO `review_comment` VALUES (11, 3, 1, '你好啊', 2, 9, 1, '2026-06-22 17:14:30', 0);
INSERT INTO `review_comment` VALUES (14, 3, 2, '好啊', 1, 9, 1, '2026-06-22 17:39:27', 0);
INSERT INTO `review_comment` VALUES (15, 3, 1, '必须好', 2, 9, 1, '2026-07-03 11:21:13', 0);

-- ----------------------------
-- Table structure for sku
-- ----------------------------
DROP TABLE IF EXISTS `sku`;
CREATE TABLE `sku`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `furniture_id` bigint NOT NULL COMMENT '关联商品ID',
  `sku_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SKU编码',
  `price` decimal(10, 2) NOT NULL COMMENT 'SKU价格',
  `stock` int NOT NULL DEFAULT 0 COMMENT 'SKU库存',
  `sku_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SKU图片',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sku_code`(`sku_code` ASC) USING BTREE,
  INDEX `idx_furniture_id`(`furniture_id` ASC) USING BTREE,
  CONSTRAINT `fk_sku_furniture` FOREIGN KEY (`furniture_id`) REFERENCES `furniture` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `sku_chk_stock` CHECK (`stock` >= 0)
) ENGINE = InnoDB AUTO_INCREMENT = 132 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SKU表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sku
-- ----------------------------
INSERT INTO `sku` VALUES (29, 1, 'CZ-H-1.5', 300.00, 10, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/07/03/c48e7d8a13144e78878209095aaa59da.jpg', 1, '2026-07-03 10:28:16');
INSERT INTO `sku` VALUES (30, 1, 'CZ-B-1.5', 280.00, 0, '', 1, '2026-07-03 10:28:16');
INSERT INTO `sku` VALUES (31, 1, 'CZ-H-0.8', 210.00, 10, '', 1, '2026-07-03 10:28:16');
INSERT INTO `sku` VALUES (32, 1, 'CZ-B-0.8', 240.00, 10, '', 1, '2026-07-03 10:28:16');
INSERT INTO `sku` VALUES (61, 2, 'hl', 500.00, 10, '', 1, '2026-07-09 14:51:48');
INSERT INTO `sku` VALUES (62, 2, 'hs', 800.00, 10, '', 1, '2026-07-09 14:51:48');
INSERT INTO `sku` VALUES (63, 2, 'bl', 600.00, 10, '', 1, '2026-07-09 14:51:48');
INSERT INTO `sku` VALUES (64, 2, 'bs', 900.00, 10, '', 1, '2026-07-09 14:51:48');
INSERT INTO `sku` VALUES (69, 3, 'r1.8', 300.00, 5, '', 1, '2026-07-09 14:52:00');
INSERT INTO `sku` VALUES (70, 3, 'r1.5', 200.00, 5, '', 1, '2026-07-09 14:52:00');
INSERT INTO `sku` VALUES (71, 3, 'y1.8', 200.00, 5, '', 1, '2026-07-09 14:52:00');
INSERT INTO `sku` VALUES (72, 3, 'y1.5', 100.00, 5, '', 1, '2026-07-09 14:52:00');
INSERT INTO `sku` VALUES (93, 4, 'hys', 200.00, 15, '', 1, '2026-07-09 15:18:40');
INSERT INTO `sku` VALUES (94, 4, 'hyd', 100.00, 15, '', 1, '2026-07-09 15:18:40');
INSERT INTO `sku` VALUES (95, 4, 'bhs', 150.00, 15, '', 1, '2026-07-09 15:18:40');
INSERT INTO `sku` VALUES (96, 4, 'bhd', 90.00, 15, '', 1, '2026-07-09 15:18:40');
INSERT INTO `sku` VALUES (104, 5, 'by', 500.00, 5, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/07/10/435be6b3add741cbb73ae7346cac7c35.webp', 1, '2026-07-10 11:19:35');
INSERT INTO `sku` VALUES (105, 5, 'bh', 600.00, 5, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/07/10/257d36df7c7c4ca8930552dd2baec55b.webp', 1, '2026-07-10 11:19:35');
INSERT INTO `sku` VALUES (106, 5, 'hby', 700.00, 5, '', 1, '2026-07-10 11:19:35');
INSERT INTO `sku` VALUES (107, 5, 'hbh', 800.00, 5, '', 1, '2026-07-10 11:19:35');
INSERT INTO `sku` VALUES (108, 5, 'hy', 700.00, 5, '', 1, '2026-07-10 11:19:35');
INSERT INTO `sku` VALUES (109, 5, 'hh', 900.00, 5, '', 1, '2026-07-10 11:19:35');
INSERT INTO `sku` VALUES (110, 5, 'jh', 15000.00, 2, 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/furniture/2026/07/10/1a7eb97d10384422887d6267b58fa399.webp', 1, '2026-07-10 11:19:35');
INSERT INTO `sku` VALUES (111, 6, 'hr', 80.00, 10, '', 1, '2026-07-10 11:27:28');
INSERT INTO `sku` VALUES (112, 6, 'hz', 70.00, 10, '', 1, '2026-07-10 11:27:28');
INSERT INTO `sku` VALUES (113, 6, 'hycy', 50.00, 10, '', 1, '2026-07-10 11:27:28');
INSERT INTO `sku` VALUES (114, 6, 'br', 80.00, 10, '', 1, '2026-07-10 11:27:28');
INSERT INTO `sku` VALUES (115, 6, 'bz', 70.00, 10, '', 1, '2026-07-10 11:27:28');
INSERT INTO `sku` VALUES (116, 6, 'bycy', 50.00, 10, '', 1, '2026-07-10 11:27:28');
INSERT INTO `sku` VALUES (117, 6, 'cr', 80.00, 10, '', 1, '2026-07-10 11:27:28');
INSERT INTO `sku` VALUES (118, 6, 'cz', 70.00, 10, '', 1, '2026-07-10 11:27:28');
INSERT INTO `sku` VALUES (119, 6, 'cy', 50.00, 10, '', 1, '2026-07-10 11:27:28');
INSERT INTO `sku` VALUES (120, 7, 'hcj', 800.00, 15, '', 1, '2026-07-10 11:28:42');
INSERT INTO `sku` VALUES (121, 7, 'bcj', 600.00, 15, '', 1, '2026-07-10 11:28:42');
INSERT INTO `sku` VALUES (122, 8, 'bm', 200.00, 10, '', 1, '2026-07-10 11:30:03');
INSERT INTO `sku` VALUES (123, 8, 'bbl', 400.00, 10, '', 1, '2026-07-10 11:30:03');
INSERT INTO `sku` VALUES (124, 8, 'hm', 200.00, 10, '', 1, '2026-07-10 11:30:03');
INSERT INTO `sku` VALUES (125, 8, 'hbl', 400.00, 10, '', 1, '2026-07-10 11:30:03');
INSERT INTO `sku` VALUES (126, 9, 'bsj', 200.00, 5, '', 1, '2026-07-10 11:31:08');
INSERT INTO `sku` VALUES (127, 9, 'heisj', 200.00, 5, '', 1, '2026-07-10 11:31:08');
INSERT INTO `sku` VALUES (128, 9, 'hsj', 200.00, 5, '', 1, '2026-07-10 11:31:08');
INSERT INTO `sku` VALUES (129, 10, 'hxg', 150.00, 10, '', 1, '2026-07-10 11:31:52');
INSERT INTO `sku` VALUES (130, 10, 'bxg', 150.00, 10, '', 1, '2026-07-10 11:31:52');
INSERT INTO `sku` VALUES (131, 10, 'heixg', 150.00, 10, '', 1, '2026-07-10 11:31:52');

-- ----------------------------
-- Table structure for sku_spec
-- ----------------------------
DROP TABLE IF EXISTS `sku_spec`;
CREATE TABLE `sku_spec`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sku_id` bigint NOT NULL COMMENT '关联SKU ID',
  `spec_group_id` bigint NOT NULL COMMENT '规格组ID',
  `spec_value_id` bigint NOT NULL COMMENT '规格值ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sku_spec_group`(`sku_id` ASC, `spec_group_id` ASC) USING BTREE,
  INDEX `idx_spec_value_id`(`spec_value_id` ASC) USING BTREE,
  INDEX `fk_sku_spec_group`(`spec_group_id` ASC) USING BTREE,
  CONSTRAINT `fk_sku_spec_group` FOREIGN KEY (`spec_group_id`) REFERENCES `spec_group` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_sku_spec_sku` FOREIGN KEY (`sku_id`) REFERENCES `sku` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_sku_spec_value` FOREIGN KEY (`spec_value_id`) REFERENCES `spec_value` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 183 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SKU与规格值关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sku_spec
-- ----------------------------
INSERT INTO `sku_spec` VALUES (17, 29, 9, 17);
INSERT INTO `sku_spec` VALUES (18, 29, 10, 19);
INSERT INTO `sku_spec` VALUES (19, 30, 9, 17);
INSERT INTO `sku_spec` VALUES (20, 30, 10, 20);
INSERT INTO `sku_spec` VALUES (21, 31, 9, 18);
INSERT INTO `sku_spec` VALUES (22, 31, 10, 19);
INSERT INTO `sku_spec` VALUES (23, 32, 9, 18);
INSERT INTO `sku_spec` VALUES (24, 32, 10, 20);
INSERT INTO `sku_spec` VALUES (49, 61, 30, 59);
INSERT INTO `sku_spec` VALUES (50, 61, 31, 61);
INSERT INTO `sku_spec` VALUES (51, 62, 30, 59);
INSERT INTO `sku_spec` VALUES (52, 62, 31, 62);
INSERT INTO `sku_spec` VALUES (53, 63, 30, 60);
INSERT INTO `sku_spec` VALUES (54, 63, 31, 61);
INSERT INTO `sku_spec` VALUES (55, 64, 30, 60);
INSERT INTO `sku_spec` VALUES (56, 64, 31, 62);
INSERT INTO `sku_spec` VALUES (65, 69, 34, 67);
INSERT INTO `sku_spec` VALUES (66, 69, 35, 69);
INSERT INTO `sku_spec` VALUES (67, 70, 34, 67);
INSERT INTO `sku_spec` VALUES (68, 70, 35, 70);
INSERT INTO `sku_spec` VALUES (69, 71, 34, 68);
INSERT INTO `sku_spec` VALUES (70, 71, 35, 69);
INSERT INTO `sku_spec` VALUES (71, 72, 34, 68);
INSERT INTO `sku_spec` VALUES (72, 72, 35, 70);
INSERT INTO `sku_spec` VALUES (109, 93, 48, 98);
INSERT INTO `sku_spec` VALUES (110, 93, 49, 100);
INSERT INTO `sku_spec` VALUES (111, 93, 50, 102);
INSERT INTO `sku_spec` VALUES (112, 94, 48, 98);
INSERT INTO `sku_spec` VALUES (113, 94, 49, 100);
INSERT INTO `sku_spec` VALUES (114, 94, 50, 103);
INSERT INTO `sku_spec` VALUES (115, 95, 48, 99);
INSERT INTO `sku_spec` VALUES (116, 95, 49, 101);
INSERT INTO `sku_spec` VALUES (117, 95, 50, 102);
INSERT INTO `sku_spec` VALUES (118, 96, 48, 99);
INSERT INTO `sku_spec` VALUES (119, 96, 49, 101);
INSERT INTO `sku_spec` VALUES (120, 96, 50, 103);
INSERT INTO `sku_spec` VALUES (135, 104, 53, 111);
INSERT INTO `sku_spec` VALUES (136, 104, 54, 115);
INSERT INTO `sku_spec` VALUES (137, 105, 53, 111);
INSERT INTO `sku_spec` VALUES (138, 105, 54, 116);
INSERT INTO `sku_spec` VALUES (139, 106, 53, 112);
INSERT INTO `sku_spec` VALUES (140, 106, 54, 115);
INSERT INTO `sku_spec` VALUES (141, 107, 53, 112);
INSERT INTO `sku_spec` VALUES (142, 107, 54, 116);
INSERT INTO `sku_spec` VALUES (143, 108, 53, 113);
INSERT INTO `sku_spec` VALUES (144, 108, 54, 115);
INSERT INTO `sku_spec` VALUES (145, 109, 53, 113);
INSERT INTO `sku_spec` VALUES (146, 109, 54, 116);
INSERT INTO `sku_spec` VALUES (147, 110, 53, 114);
INSERT INTO `sku_spec` VALUES (148, 110, 54, 117);
INSERT INTO `sku_spec` VALUES (149, 111, 61, 136);
INSERT INTO `sku_spec` VALUES (150, 111, 62, 139);
INSERT INTO `sku_spec` VALUES (151, 112, 61, 136);
INSERT INTO `sku_spec` VALUES (152, 112, 62, 140);
INSERT INTO `sku_spec` VALUES (153, 113, 61, 136);
INSERT INTO `sku_spec` VALUES (154, 113, 62, 141);
INSERT INTO `sku_spec` VALUES (155, 114, 61, 137);
INSERT INTO `sku_spec` VALUES (156, 114, 62, 139);
INSERT INTO `sku_spec` VALUES (157, 115, 61, 137);
INSERT INTO `sku_spec` VALUES (158, 115, 62, 140);
INSERT INTO `sku_spec` VALUES (159, 116, 61, 137);
INSERT INTO `sku_spec` VALUES (160, 116, 62, 141);
INSERT INTO `sku_spec` VALUES (161, 117, 61, 138);
INSERT INTO `sku_spec` VALUES (162, 117, 62, 139);
INSERT INTO `sku_spec` VALUES (163, 118, 61, 138);
INSERT INTO `sku_spec` VALUES (164, 118, 62, 140);
INSERT INTO `sku_spec` VALUES (165, 119, 61, 138);
INSERT INTO `sku_spec` VALUES (166, 119, 62, 141);
INSERT INTO `sku_spec` VALUES (167, 120, 63, 142);
INSERT INTO `sku_spec` VALUES (168, 121, 63, 143);
INSERT INTO `sku_spec` VALUES (169, 122, 64, 144);
INSERT INTO `sku_spec` VALUES (170, 122, 65, 146);
INSERT INTO `sku_spec` VALUES (171, 123, 64, 144);
INSERT INTO `sku_spec` VALUES (172, 123, 65, 147);
INSERT INTO `sku_spec` VALUES (173, 124, 64, 145);
INSERT INTO `sku_spec` VALUES (174, 124, 65, 146);
INSERT INTO `sku_spec` VALUES (175, 125, 64, 145);
INSERT INTO `sku_spec` VALUES (176, 125, 65, 147);
INSERT INTO `sku_spec` VALUES (177, 126, 66, 148);
INSERT INTO `sku_spec` VALUES (178, 127, 66, 149);
INSERT INTO `sku_spec` VALUES (179, 128, 66, 150);
INSERT INTO `sku_spec` VALUES (180, 129, 67, 151);
INSERT INTO `sku_spec` VALUES (181, 130, 67, 152);
INSERT INTO `sku_spec` VALUES (182, 131, 67, 153);

-- ----------------------------
-- Table structure for spec_group
-- ----------------------------
DROP TABLE IF EXISTS `spec_group`;
CREATE TABLE `spec_group`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `furniture_id` bigint NOT NULL COMMENT '关联商品ID',
  `group_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规格组名称，如颜色、尺寸',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_furniture_id`(`furniture_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_furniture_group`(`furniture_id` ASC, `group_name` ASC) USING BTREE,
  CONSTRAINT `fk_spec_group_furniture` FOREIGN KEY (`furniture_id`) REFERENCES `furniture` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 68 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '规格组表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of spec_group
-- ----------------------------
INSERT INTO `spec_group` VALUES (9, 1, '大小', 1, '2026-07-03 10:28:16');
INSERT INTO `spec_group` VALUES (10, 1, '颜色', 2, '2026-07-03 10:28:16');
INSERT INTO `spec_group` VALUES (30, 2, '颜色', 0, '2026-07-09 14:51:48');
INSERT INTO `spec_group` VALUES (31, 2, '人数', 1, '2026-07-09 14:51:48');
INSERT INTO `spec_group` VALUES (34, 3, '质地', 0, '2026-07-09 14:52:00');
INSERT INTO `spec_group` VALUES (35, 3, '尺寸（米）', 1, '2026-07-09 14:52:00');
INSERT INTO `spec_group` VALUES (48, 4, '颜色', 0, '2026-07-09 15:18:40');
INSERT INTO `spec_group` VALUES (49, 4, '木质', 1, '2026-07-09 15:18:40');
INSERT INTO `spec_group` VALUES (50, 4, '大小', 2, '2026-07-09 15:18:40');
INSERT INTO `spec_group` VALUES (53, 5, '颜色', 0, '2026-07-10 11:19:35');
INSERT INTO `spec_group` VALUES (54, 5, '材质', 1, '2026-07-10 11:19:35');
INSERT INTO `spec_group` VALUES (61, 6, '颜色', 0, '2026-07-10 11:27:28');
INSERT INTO `spec_group` VALUES (62, 6, '硬度', 1, '2026-07-10 11:27:28');
INSERT INTO `spec_group` VALUES (63, 7, '颜色', 0, '2026-07-10 11:28:42');
INSERT INTO `spec_group` VALUES (64, 8, '颜色', 0, '2026-07-10 11:30:03');
INSERT INTO `spec_group` VALUES (65, 8, '质地', 1, '2026-07-10 11:30:03');
INSERT INTO `spec_group` VALUES (66, 9, '颜色', 0, '2026-07-10 11:31:08');
INSERT INTO `spec_group` VALUES (67, 10, '颜色', 0, '2026-07-10 11:31:52');

-- ----------------------------
-- Table structure for spec_value
-- ----------------------------
DROP TABLE IF EXISTS `spec_value`;
CREATE TABLE `spec_value`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `spec_group_id` bigint NOT NULL COMMENT '关联规格组ID',
  `value_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规格值名称，如米白、三人位',
  `value_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格值图片URL',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_spec_group_id`(`spec_group_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_group_value`(`spec_group_id` ASC, `value_name` ASC) USING BTREE,
  CONSTRAINT `fk_spec_value_group` FOREIGN KEY (`spec_group_id`) REFERENCES `spec_group` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 154 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '规格值表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of spec_value
-- ----------------------------
INSERT INTO `spec_value` VALUES (17, 9, '1.5×1.5', '', 0);
INSERT INTO `spec_value` VALUES (18, 9, '0.8×0.8', '', 1);
INSERT INTO `spec_value` VALUES (19, 10, '黑色', '', 0);
INSERT INTO `spec_value` VALUES (20, 10, '白色', '', 1);
INSERT INTO `spec_value` VALUES (59, 30, '黑色', '', 0);
INSERT INTO `spec_value` VALUES (60, 30, '咖色', '', 1);
INSERT INTO `spec_value` VALUES (61, 31, '两人位', '', 0);
INSERT INTO `spec_value` VALUES (62, 31, '三人位', '', 1);
INSERT INTO `spec_value` VALUES (67, 34, '软', '', 0);
INSERT INTO `spec_value` VALUES (68, 34, '硬', '', 1);
INSERT INTO `spec_value` VALUES (69, 35, '1.8 × 2.0', '', 0);
INSERT INTO `spec_value` VALUES (70, 35, '1.5 × 1.8', '', 1);
INSERT INTO `spec_value` VALUES (98, 48, '黑色', '', 0);
INSERT INTO `spec_value` VALUES (99, 48, '白色', '', 1);
INSERT INTO `spec_value` VALUES (100, 49, '原木', '', 0);
INSERT INTO `spec_value` VALUES (101, 49, '桦木', '', 1);
INSERT INTO `spec_value` VALUES (102, 50, '双人', '', 0);
INSERT INTO `spec_value` VALUES (103, 50, '单人', '', 1);
INSERT INTO `spec_value` VALUES (111, 53, '纯白', '', 0);
INSERT INTO `spec_value` VALUES (112, 53, '黑白', '', 1);
INSERT INTO `spec_value` VALUES (113, 53, '黑色', '', 2);
INSERT INTO `spec_value` VALUES (114, 53, '金黄', '', 3);
INSERT INTO `spec_value` VALUES (115, 54, '原木', '', 0);
INSERT INTO `spec_value` VALUES (116, 54, '桦木', '', 1);
INSERT INTO `spec_value` VALUES (117, 54, '金丝楠木', '', 2);
INSERT INTO `spec_value` VALUES (136, 61, '黑色', '', 0);
INSERT INTO `spec_value` VALUES (137, 61, '白色', '', 1);
INSERT INTO `spec_value` VALUES (138, 61, '橙色', '', 2);
INSERT INTO `spec_value` VALUES (139, 62, '软', '', 0);
INSERT INTO `spec_value` VALUES (140, 62, '中', '', 1);
INSERT INTO `spec_value` VALUES (141, 62, '硬', '', 2);
INSERT INTO `spec_value` VALUES (142, 63, '黑色', '', 0);
INSERT INTO `spec_value` VALUES (143, 63, '白色', '', 1);
INSERT INTO `spec_value` VALUES (144, 64, '白色', '', 0);
INSERT INTO `spec_value` VALUES (145, 64, '黄色', '', 1);
INSERT INTO `spec_value` VALUES (146, 65, '木质', '', 0);
INSERT INTO `spec_value` VALUES (147, 65, '玻璃制', '', 1);
INSERT INTO `spec_value` VALUES (148, 66, '白色', '', 0);
INSERT INTO `spec_value` VALUES (149, 66, '黑色', '', 1);
INSERT INTO `spec_value` VALUES (150, 66, '黄色', '', 2);
INSERT INTO `spec_value` VALUES (151, 67, '黄色', '', 0);
INSERT INTO `spec_value` VALUES (152, 67, '白色', '', 1);
INSERT INTO `spec_value` VALUES (153, 67, '黑色', '', 2);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `pass_word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `consignee` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `consignee_phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `is_admin` int NULL DEFAULT 0,
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '13483005180', '3102777566@qq.com', '$2a$10$3ku3PIB.aOrGLa1IfABf..0PKRSSCdctFjCUUgHzJLjPiZ.aDmt5a', 'Glimcy', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/avatar/2026/05/20/50bbfc8ad6a64616bcd701a83d3cce68.jpg', 'UK', '郭名城', '13444444444', '2026-03-24 18:39:53', 1, 0);
INSERT INTO `user` VALUES (2, '13483005181', '3482439245@qq.com', '$2a$10$JxYiyP/G0Jg9BgcIPyv.q.OuWuZDPry7IODX5ZxXZvQ/QTKLd1Bja', 'LOPS', 'https://gmc-1007.oss-cn-beijing.aliyuncs.com/avatar/2026/06/22/9f63f90629bb43d59418014a9d05a97e.jpg', '河北省邯郸市', '名称', '13483005181', '2026-03-23 18:39:56', 0, 0);

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `consignee` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_default` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_address_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_address
-- ----------------------------
INSERT INTO `user_address` VALUES (1, 1, '郭名城', '13444444444', 'USA', 0, '2026-05-26 14:31:07');
INSERT INTO `user_address` VALUES (2, 1, '郭名城', '13444444444', 'UK', 1, '2026-05-26 14:31:24');

-- ----------------------------
-- Table structure for user_notification
-- ----------------------------
DROP TABLE IF EXISTS `user_notification`;
CREATE TABLE `user_notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `notification_id` bigint NOT NULL COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读 0=未读 1=已读',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '用户是否删除 0=未删 1=已删',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_notification_user`(`notification_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_deleted`(`user_id` ASC, `is_deleted` ASC) USING BTREE,
  CONSTRAINT `fk_notification_read_notification` FOREIGN KEY (`notification_id`) REFERENCES `notification` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_notification_read_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知已读记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_notification
-- ----------------------------
INSERT INTO `user_notification` VALUES (1, 1, 1, '2026-05-26 10:00:00', 1, 1, '2026-07-08 09:57:01');
INSERT INTO `user_notification` VALUES (2, 10, 1, '2026-05-26 10:00:00', 1, 0, NULL);
INSERT INTO `user_notification` VALUES (3, 11, 1, '2026-05-26 10:00:00', 1, 0, NULL);
INSERT INTO `user_notification` VALUES (4, 12, 1, '2026-05-26 12:36:47', 1, 0, NULL);
INSERT INTO `user_notification` VALUES (5, 13, 1, '2026-05-26 14:08:23', 1, 0, NULL);
INSERT INTO `user_notification` VALUES (6, 1, 2, '2026-05-26 14:08:56', 1, 0, NULL);
INSERT INTO `user_notification` VALUES (7, 10, 2, '2026-05-26 14:08:56', 1, 0, NULL);
INSERT INTO `user_notification` VALUES (8, 12, 2, '2026-05-26 14:08:56', 1, 0, NULL);
INSERT INTO `user_notification` VALUES (9, 13, 2, '2026-05-26 14:08:56', 1, 0, NULL);
INSERT INTO `user_notification` VALUES (10, 14, 1, '2026-06-22 17:39:42', 1, 0, NULL);
INSERT INTO `user_notification` VALUES (11, 15, 2, '2026-07-03 11:21:39', 1, 0, NULL);
INSERT INTO `user_notification` VALUES (12, 16, 1, '2026-07-08 10:41:50', 1, 1, '2026-07-08 11:25:28');
INSERT INTO `user_notification` VALUES (13, 17, 1, '2026-07-08 11:25:14', 1, 1, '2026-07-08 11:25:25');

SET FOREIGN_KEY_CHECKS = 1;
