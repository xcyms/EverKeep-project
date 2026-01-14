/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : localhost:3306
 Source Schema         : everkeep

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 14/01/2026 11:29:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_album
-- ----------------------------
DROP TABLE IF EXISTS `biz_album`;
CREATE TABLE `biz_album`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '相册名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '相册表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_album
-- ----------------------------
INSERT INTO `biz_album` VALUES (1, 1, '测试相册', '这是一个用于测试的相册', '/uploads/83cf7e02-c2bf-4f75-91d6-273646bb6e10.jpg', '2026-01-13 11:51:57', '2026-01-13 16:22:24', 0);

-- ----------------------------
-- Table structure for biz_image
-- ----------------------------
DROP TABLE IF EXISTS `biz_image`;
CREATE TABLE `biz_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '上传者ID',
  `album_id` bigint NULL DEFAULT NULL COMMENT '所属相册ID',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片名称',
  `size` bigint NULL DEFAULT NULL COMMENT '大小(bytes)',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '格式(jpg/png)',
  `status` tinyint NULL DEFAULT 0 COMMENT '是否公开',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 189 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_image
-- ----------------------------
INSERT INTO `biz_image` VALUES (1, 1, NULL, '/uploads/55fb3c7e-967a-46c1-9d57-72c4e28ea9c3.png', 'logos--instagram.png', 67727, 'png', 0, '2026-01-12 14:46:07', 0);
INSERT INTO `biz_image` VALUES (2, 1, NULL, '/uploads/8285f19b-ed3e-419c-83f5-1059ef41202f.png', 'logos--instagram.png', 67727, 'png', 0, '2026-01-12 14:47:18', 0);
INSERT INTO `biz_image` VALUES (3, 1, NULL, '/uploads/a47d67cd-2b5f-44b3-90b1-55965d968712.jpg', '非常用心的敷衍一个视频#扭一扭.jpg', 19478, 'jpg', 0, '2026-01-12 15:19:31', 0);
INSERT INTO `biz_image` VALUES (4, 1, NULL, '/uploads/89768957-0ddc-4ec1-b166-be3e3d0bb02c.png', '123.png', 67203, 'png', 0, '2026-01-12 16:38:51', 0);
INSERT INTO `biz_image` VALUES (5, 1, NULL, '/uploads/44df31d3-22c4-4e1c-b70c-06706689ec5d.png', '123.png', 67203, 'png', 0, '2026-01-12 16:40:49', 0);
INSERT INTO `biz_image` VALUES (6, 1, NULL, '/uploads/14a5f9d5-59c6-419e-8212-2154a838c16e.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(1).jpg', 277037, 'jpg', 0, '2026-01-12 17:09:33', 0);
INSERT INTO `biz_image` VALUES (7, 1, NULL, '/uploads/7428ce03-5c32-436d-8d56-e7e34466c462.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(6).jpg', 195516, 'jpg', 0, '2026-01-12 17:09:33', 0);
INSERT INTO `biz_image` VALUES (8, 1, NULL, '/uploads/8d7a1f67-4064-4a67-90c3-bc00a51a821d.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(9).jpg', 200932, 'jpg', 0, '2026-01-12 17:09:33', 0);
INSERT INTO `biz_image` VALUES (9, 1, NULL, '/uploads/506bd083-993e-401e-8510-93f0a15b05b5.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(12).jpg', 181838, 'jpg', 0, '2026-01-12 17:09:33', 0);
INSERT INTO `biz_image` VALUES (10, 1, NULL, '/uploads/6d8a0984-76c6-4718-9894-0fbc49208afc.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(13).jpg', 290503, 'jpg', 0, '2026-01-12 17:09:33', 0);
INSERT INTO `biz_image` VALUES (11, 1, NULL, '/uploads/9753b6e0-c6a1-41bf-9a1d-6458888fbac5.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(16).jpg', 332003, 'jpg', 0, '2026-01-12 17:09:33', 0);
INSERT INTO `biz_image` VALUES (12, 1, NULL, '/uploads/e62cbcc8-73ce-48fd-b0d2-0741dab87025.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(17).jpg', 334758, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (13, 1, NULL, '/uploads/39027e72-fc9c-4afd-bae0-9195bd18778c.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(20).jpg', 172844, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (14, 1, NULL, '/uploads/75706536-6424-4a59-b528-988795f98a4b.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(23).jpg', 346649, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (15, 1, NULL, '/uploads/0339bc4e-53c0-411e-bcec-a6aca406246c.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(24).jpg', 491019, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (16, 1, NULL, '/uploads/2a26b7f7-e058-4abf-bdea-e1d5202fa206.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(25).jpg', 239514, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (17, 1, NULL, '/uploads/e644d568-f995-4a39-9b83-606dd613fd44.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(26).jpg', 100208, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (18, 1, NULL, '/uploads/0c604a36-c377-456a-a7f7-306d937716a7.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(27).jpg', 540272, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (19, 1, NULL, '/uploads/301d759d-5a5d-482f-815f-119619567fc8.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(28).jpg', 591604, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (20, 1, NULL, '/uploads/d55be32e-21fc-45f8-85ef-f6d7bda6291c.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(29).jpg', 153534, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (21, 1, NULL, '/uploads/ce82e2a5-2023-4667-958a-dda73c1e33c9.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(31).jpg', 402427, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (22, 1, NULL, '/uploads/b729fa67-f8e2-4c14-bec7-b6a811d15911.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(33).jpg', 188512, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (23, 1, NULL, '/uploads/0c489f3e-d769-4931-ace4-d504d70ebdd5.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(39).jpg', 75668, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (24, 1, NULL, '/uploads/f49988db-5907-4512-a876-108f3bf6e725.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(41).jpg', 556368, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (25, 1, NULL, '/uploads/ec244524-9752-46ee-bd3b-8972614c510a.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(44).jpg', 376493, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (26, 1, NULL, '/uploads/04338672-6b5f-43c5-a1f9-3dc22e3a2690.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(46).jpg', 698871, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (27, 1, NULL, '/uploads/11eb8694-0084-4113-b7db-5e91f4c72adb.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(49).jpg', 368258, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (28, 1, NULL, '/uploads/7b996fbf-eeff-4183-a5dc-b2d5345510c0.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(51).jpg', 369067, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (29, 1, NULL, '/uploads/14ecc61b-f8f6-4741-9589-68fcbea85640.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(52).jpg', 509592, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (30, 1, NULL, '/uploads/85c6b2b2-b75c-4a5a-80f6-ada82893dc98.jpg', '#锁屏壁纸 #该换壁纸了 #全屏高清壁纸(53).jpg', 726189, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (31, 1, NULL, '/uploads/d173c62b-6d18-40c3-bf70-7613604672da.jpg', '“把生活的温柔藏进壁纸里✨”@神图壁纸 口令取图g6689 #该换壁纸了 #全屏高清壁纸 #美女壁纸 #手机壁纸@DOU+小助手(2).jpg', 252480, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (32, 1, NULL, '/uploads/75229744-d840-4d0c-934f-481c33f4b20f.jpeg', '“把生活的温柔藏进壁纸里✨”@神图壁纸 口令取图g6689 #该换壁纸了 #全屏高清壁纸 #美女壁纸 #手机壁纸@DOU+小助手(3).jpeg', 454041, 'jpeg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (33, 1, NULL, '/uploads/3a172428-7956-432b-8343-a0c8724bb681.jpg', '“把生活的温柔藏进壁纸里✨”@神图壁纸 口令取图g6689 #该换壁纸了 #全屏高清壁纸 #美女壁纸 #手机壁纸@DOU+小助手(4).jpg', 205342, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (34, 1, NULL, '/uploads/e894b1f7-c854-4229-b48e-817044d56332.jpeg', '“把生活的温柔藏进壁纸里✨”@神图壁纸 口令取图g6689 #该换壁纸了 #全屏高清壁纸 #美女壁纸 #手机壁纸@DOU+小助手(5).jpeg', 294267, 'jpeg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (35, 1, NULL, '/uploads/eeb9eba2-fd0e-4a12-b786-d03ba6f7bd04.jpg', '“把生活的温柔藏进壁纸里✨”@神图壁纸 口令取图g6689 #该换壁纸了 #全屏高清壁纸 #美女壁纸 #手机壁纸@DOU+小助手(9).jpg', 487525, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (36, 1, NULL, '/uploads/46e3dfa9-d7c0-4f20-9ec2-75b529c8c109.jpg', '“把生活的温柔藏进壁纸里✨”@神图壁纸 口令取图g6689 #该换壁纸了 #全屏高清壁纸 #美女壁纸 #手机壁纸@DOU+小助手(10).jpg', 372471, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (37, 1, NULL, '/uploads/3707eac2-6725-4e05-b604-dedad34d30b3.jpg', '“把生活的温柔藏进壁纸里✨”@神图壁纸 口令取图g6689 #该换壁纸了 #全屏高清壁纸 #美女壁纸 #手机壁纸@DOU+小助手(12).jpg', 236768, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (38, 1, NULL, '/uploads/474db307-2aeb-4373-ba2d-9de25fd47638.jpg', '“把生活的温柔藏进壁纸里✨”@神图壁纸 口令取图g6689 #该换壁纸了 #全屏高清壁纸 #美女壁纸 #手机壁纸@DOU+小助手(13).jpg', 393487, 'jpg', 0, '2026-01-12 17:09:34', 0);
INSERT INTO `biz_image` VALUES (39, 1, NULL, '/uploads/72fbbf8b-1037-459e-948b-3c79fafe350c.jpg', '65e82b2b00fa4.jpg', 159724, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (40, 1, NULL, '/uploads/465d804c-d6b5-4500-991f-73d28210262e.jpg', '65e82b2b0a0e9.jpg', 163622, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (41, 1, NULL, '/uploads/05738873-7f54-4c2a-b0e8-2e99f5969988.jpg', '65e82b2b0c16b.jpg', 70611, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (42, 1, NULL, '/uploads/853ca734-8cb4-4e81-a6da-21138b104c90.jpg', '65e82b2b07e78.jpg', 124541, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (43, 1, NULL, '/uploads/a9e35d22-c692-423b-b1b8-b75b3862900e.jpg', '65e82b2b7ff20.jpg', 122747, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (44, 1, NULL, '/uploads/4b5b12a5-dbde-4b20-8211-b3d7ede82a73.jpg', '65e82b2b9d329.jpg', 214220, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (45, 1, NULL, '/uploads/67db5e6d-b1e8-45f3-b229-975fb0db0e12.jpg', '65e82b2b0052b.jpg', 62806, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (46, 1, NULL, '/uploads/efc29a7b-b5ce-4f9d-a7c1-425a91104fb7.jpg', '65e82b2b06671.jpg', 53494, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (47, 1, NULL, '/uploads/3f210523-cf43-44e3-bbc0-2b94b776d409.jpg', '65e82b2ba0af6.jpg', 292867, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (48, 1, NULL, '/uploads/c35eab69-dd1b-4dac-ae3a-1a99bef59ea5.jpg', '65e82b2bd0e06.jpg', 215164, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (49, 1, NULL, '/uploads/31e327aa-be83-437f-8733-c363cfe7b76d.jpg', '65e82b2bd6b05.jpg', 361742, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (50, 1, NULL, '/uploads/638faa82-2cb6-4ee1-83d6-d8926c468f44.jpg', '65e82b2bd26bd.jpg', 225232, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (51, 1, NULL, '/uploads/d7a85cc4-0c4f-4f6f-b401-508718c16737.jpg', '65e82b2bd44b6.jpg', 193335, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (52, 1, NULL, '/uploads/963afbb7-d660-49b7-a0b3-9e9d3ff20350.jpg', '65e82b2c6d174.jpg', 287182, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (53, 1, NULL, '/uploads/0fa2f0c8-5bbe-473c-8661-1c16107f183a.jpg', '65e82b2c681d7.jpg', 381696, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (54, 1, NULL, '/uploads/574e6efe-5539-4c2a-8fe0-984d7123cbeb.jpg', '65e82b2c62998.jpg', 466850, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (55, 1, NULL, '/uploads/5be2de53-3d7f-4e3e-a063-ec0d1d3a3788.jpg', '65e82b2ca37d9.jpg', 301511, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (56, 1, NULL, '/uploads/3ce07012-30bd-49a6-a61e-0cf04c5c3030.jpg', '65e82b2cb3dea.jpg', 113909, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (57, 1, NULL, '/uploads/f97c7e1b-8dcf-4b2b-879a-2bf598c393bb.jpg', '65e82b2cb164b.jpg', 151977, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (58, 1, NULL, '/uploads/3cc13db2-ea27-4d71-8c63-bd1716365a3b.jpg', '65e82b2cc9532.jpg', 105126, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (59, 1, NULL, '/uploads/513bc421-c3b1-46e5-be63-ad8f8b9cd08c.jpg', '65e82b2ccb26e.jpg', 242254, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (60, 1, NULL, '/uploads/8ea173d4-f47f-4f33-9473-56592565485e.jpg', '65e82b2d5cbfe.jpg', 172774, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (61, 1, NULL, '/uploads/ab54e284-5aa4-4aa0-8e64-5e8179b32ffd.jpg', '65e82b2d6b596.jpg', 190801, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (62, 1, NULL, '/uploads/dc90c122-8943-434b-9948-b4ea652cf5ab.jpg', '65e82b2d59d9e.jpg', 243010, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (63, 1, NULL, '/uploads/63f975f9-4843-4491-982c-72a4b48e34a3.jpg', '65e82b2d663c9.jpg', 282397, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (64, 1, NULL, '/uploads/a4c3d9da-9bd6-44c2-a8a8-a6b4d79780d8.jpg', '65e82b2d38809.jpg', 205829, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (65, 1, NULL, '/uploads/838f8dc4-4fcc-481e-ada6-e8103042ff74.jpg', '65e82b2d67513.jpg', 246717, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (66, 1, NULL, '/uploads/1e5a714b-5589-48cc-941f-11795704529b.jpg', '65e82b2df12da.jpg', 312149, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (67, 1, NULL, '/uploads/d8806a29-8d36-4f48-ba20-afd672e796e7.jpg', '65e82b2e1afa1.jpg', 190238, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (68, 1, NULL, '/uploads/8c477477-fb9f-4394-8791-573ba6422d24.jpg', '65e82b2e09fb2.jpg', 322968, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (69, 1, NULL, '/uploads/d06ab3cd-b9e0-4fff-870c-1385993c2a39.jpg', '65e82b2e195f3.jpg', 328773, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (70, 1, NULL, '/uploads/6ed12a46-5d8b-4e36-bb29-e60466ee28bc.jpg', '65e82b2e26493.jpg', 221803, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (71, 1, NULL, '/uploads/5c15e291-b04e-4ea7-9b5f-a2493c67b0f4.jpg', '65e82e62e5ace.jpg', 150841, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (72, 1, NULL, '/uploads/b9b1887b-43e1-41c0-898c-a58e726c154f.jpg', '65e82e62e679e.jpg', 135444, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (73, 1, NULL, '/uploads/4702a420-5fd6-4f16-b04f-2be3a4bb4416.jpg', '65e82e632fa5b.jpg', 135747, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (74, 1, NULL, '/uploads/272ecd2e-c64e-44de-80bd-86610d552eac.jpg', '65e82e6311a2a.jpg', 170349, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (75, 1, NULL, '/uploads/7d5b5546-bd21-4656-9019-77b114b98450.jpg', '65e82e6331fd2.jpg', 153778, 'jpg', 0, '2026-01-12 17:44:24', 0);
INSERT INTO `biz_image` VALUES (76, 1, NULL, '/uploads/958e4534-b7ac-43c2-80fa-f59621b92ec9.jpg', '65e82e63288ef.jpg', 135636, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (77, 1, NULL, '/uploads/52114754-6bdb-4d16-9407-3cfad707f11d.jpg', '65e828edbe521.jpg', 132569, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (78, 1, NULL, '/uploads/1ae5b0ec-05fa-4e43-b96f-283506cae9aa.jpg', '65e828edbeead.jpg', 103069, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (79, 1, NULL, '/uploads/2313860d-4da4-458b-adb7-6d3a90cdd8ab.jpg', '65e828edd25c7.jpg', 189351, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (80, 1, NULL, '/uploads/4f3cf7c6-ec41-4e3e-a590-98e233c2eb2f.jpg', '65e828edda13a.jpg', 133060, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (81, 1, NULL, '/uploads/aa918f00-bb05-4344-b744-9627cd31b100.jpg', '65e828ee6a65d.jpg', 157042, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (82, 1, NULL, '/uploads/1aa6ac6f-927e-4ee0-b8e5-b4b1f6823d05.jpg', '65e828ee8e768.jpg', 157215, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (83, 1, NULL, '/uploads/81e1be07-963d-4430-a78c-4236cf828b32.jpg', '65e828ee9d1dd.jpg', 256561, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (84, 1, NULL, '/uploads/47333b97-1405-424c-afe7-5c946000ca07.jpg', '65e828ee9fec4.jpg', 279075, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (85, 1, NULL, '/uploads/133f2edf-130b-4613-b98f-5263adfe2d5b.jpg', '65e828ee2707d.jpg', 188028, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (86, 1, NULL, '/uploads/a5216df7-86af-46e0-8fa3-d1d93177e55b.jpg', '65e828ee03980.jpg', 166212, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (87, 1, NULL, '/uploads/fb6754f9-e7b1-4f43-91fd-a454a772a634.jpg', '65e828eead8fd.jpg', 74454, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (88, 1, NULL, '/uploads/9d1b8ce4-321e-4d42-8532-62024b75345e.jpg', '65e828eec7292.jpg', 103320, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (89, 1, NULL, '/uploads/07a49d72-ec07-47e3-aff0-7c8dbc2f9fb1.jpg', '65e828eee468d.jpg', 149295, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (90, 1, NULL, '/uploads/ec467066-e881-4722-97ad-554b2e631397.jpg', '65e828ef7dcda.jpg', 158374, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (91, 1, NULL, '/uploads/7e3df5b2-c36b-4a12-b6e3-ac10c1e7e162.jpg', '65e828ef9a6ad.jpg', 207967, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (92, 1, NULL, '/uploads/75a9fb30-7168-4512-9574-442b4099ce9a.jpg', '65e828ef93c6c.jpg', 200431, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (93, 1, NULL, '/uploads/7630ae2f-8a24-4e91-a447-f67c803bf0b7.jpg', '65e828ef40536.jpg', 101685, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (94, 1, NULL, '/uploads/1ac7eee9-a7fe-4c2f-a2fb-34943d2b55e4.jpg', '65e828ef48863.jpg', 114775, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (95, 1, NULL, '/uploads/e00d3761-6599-410b-88ff-4cda26d25ecc.jpg', '65e828efb6983.jpg', 238387, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (96, 1, NULL, '/uploads/b39f9873-0259-4ca5-9e8e-58f12b09382b.jpg', '65e828efcf02e.jpg', 237977, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (97, 1, NULL, '/uploads/f9c672e0-a273-46ad-ab8a-cd556ed7e007.jpg', '65e828f000c27.jpg', 239160, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (98, 1, NULL, '/uploads/20b8fd06-cf4d-419b-822a-b83987e01fb2.jpg', '65e828f003d8e.jpg', 267605, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (99, 1, NULL, '/uploads/f04609f3-153d-4db8-a896-7909151d009a.jpg', '65e8222fd561a.jpg', 184912, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (100, 1, NULL, '/uploads/eb0b9ce7-4f72-46a4-ae81-5c246851ce64.jpg', '65e9169e4ed06.jpg', 317732, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (101, 1, NULL, '/uploads/3a16f797-56d3-480a-bf69-209ebf0beb41.jpg', '65e9169e50285.jpg', 74468, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (102, 1, NULL, '/uploads/9abb1198-3a41-4378-8790-2f280e0ed2c4.jpg', '65e9169e52690.jpg', 249703, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (103, 1, NULL, '/uploads/f524f8ff-5182-40e6-a6b1-9d91c130ad28.jpg', '65e9169e55379.jpg', 66304, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (104, 1, NULL, '/uploads/bbcd0d4d-fde5-4b7a-91e4-fb721363d724.jpg', '65e82230c0234.jpg', 179469, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (105, 1, NULL, '/uploads/27fab2b0-793e-48da-a07d-4b595d885462.jpg', '65e82230d0469.jpg', 243595, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (106, 1, NULL, '/uploads/ab8a8147-2c0a-42e4-9913-8c2255179764.jpg', '65e82231b3d91.jpg', 326855, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (107, 1, NULL, '/uploads/61343736-9dfe-431e-aa47-e20665763f20.jpg', '65e82231bf1b6.jpg', 201780, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (108, 1, NULL, '/uploads/6e4aa65c-c0ba-4d69-8fd4-9607e0d68215.jpg', '65e82231f3c57.jpg', 176519, 'jpg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (109, 1, NULL, '/uploads/eb6eed2d-6e93-40b8-8182-c8250135356e.jpeg', '65e82232b4a3f.jpeg', 61927, 'jpeg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (110, 1, NULL, '/uploads/bee6b6a7-315f-47e0-a0ad-88eea51043b5.jpeg', '65e82232b7b24.jpeg', 100791, 'jpeg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (111, 1, NULL, '/uploads/984e0cd1-e869-40bc-9175-366b5df6f120.jpeg', '65e82232b6108.jpeg', 120303, 'jpeg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (112, 1, NULL, '/uploads/475ab244-7ba9-450c-8bcc-5c6d846ff196.jpeg', '65e82232dc3f0.jpeg', 122565, 'jpeg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (113, 1, NULL, '/uploads/e1122861-98f2-4bdc-9ba0-4e764d8764d1.jpeg', '65e82232e1db5.jpeg', 122480, 'jpeg', 0, '2026-01-12 17:44:25', 0);
INSERT INTO `biz_image` VALUES (114, 1, NULL, '/uploads/98ece4b7-8a8e-4392-9b25-96d088843763.jpeg', '65e82233bd3b6.jpeg', 215789, 'jpeg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (115, 1, NULL, '/uploads/88b8c273-cf81-494f-9be6-cbac2aef2560.jpg', '65e82233def6f.jpg', 346834, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (116, 1, NULL, '/uploads/d8795ef4-b859-43f9-bb37-59ac57fca553.jpg', '65e82234ba33c.jpg', 167048, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (117, 1, NULL, '/uploads/0d75ca75-96b6-472c-8cdd-1f9a09113877.jpg', '65e82234e8fa9.jpg', 362672, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (118, 1, NULL, '/uploads/3c6f4972-16e0-432c-952b-60cc1a154d7a.jpg', '65e82236a14c9.jpg', 147651, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (119, 1, NULL, '/uploads/b56e460e-dfc5-4195-870d-85abd207da7e.jpg', '65e82236acdfb.jpg', 151469, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (120, 1, NULL, '/uploads/75dc4be5-c240-4b57-aac3-78d441fe6a46.jpg', '65e82236bf580.jpg', 148430, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (121, 1, NULL, '/uploads/5d6b9be4-648c-4831-8d7c-230313ea68bd.jpg', '65e82251e0be9.jpg', 111922, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (122, 1, NULL, '/uploads/dba74a6a-aa81-4ddb-b604-ca319aced67f.jpg', '65e82252a8264.jpg', 159809, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (123, 1, NULL, '/uploads/217c6f4b-c5ef-44c7-8e3e-32fd5842b7cc.jpg', '65e82252aa74c.jpg', 156671, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (124, 1, NULL, '/uploads/1fa32dbd-134a-4e5e-bc28-8063e28efaa9.jpg', '65e82252cd420.jpg', 403799, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (125, 1, NULL, '/uploads/3a514cd7-430f-4ecd-86f1-c41582e1c051.jpg', '65e82253b9e1f.jpg', 150818, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (126, 1, NULL, '/uploads/2a15f518-bee7-455b-a0a3-4d810bc22db6.jpg', '65e82254dac05.jpg', 248433, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (127, 1, NULL, '/uploads/3e0b72d5-2dbc-4a1b-b986-dfe3fb401bf3.jpg', '65e82254e1f9d.jpg', 168156, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (128, 1, NULL, '/uploads/32e2e546-ec5f-4876-ac77-d937be54755b.jpg', '65e82254e03ae.jpg', 243338, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (129, 1, NULL, '/uploads/23133724-cde7-4b29-b5c6-be1ed799b88d.jpg', '65e82255b3b19.jpg', 239925, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (130, 1, NULL, '/uploads/1112601e-6244-4043-bb40-e089f96e4c35.jpg', '65e82255b226d.jpg', 182824, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (131, 1, NULL, '/uploads/b06fd966-f1f1-4898-9160-0895d34f35d5.jpg', '65e82255c8016.jpg', 107150, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (132, 1, NULL, '/uploads/370e73c8-de22-4b46-90a3-cfb30acd7be1.jpg', '65e82256aa786.jpg', 276919, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (133, 1, NULL, '/uploads/e104ff78-71f5-4be4-be26-b2a6bcaae0ad.jpg', '65e82256acf59.jpg', 393656, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (134, 1, NULL, '/uploads/2105968b-e0c7-43dd-8c0e-df12a1466144.jpg', '65e82256ae45b.jpg', 277050, 'jpg', 0, '2026-01-12 17:44:26', 0);
INSERT INTO `biz_image` VALUES (135, 1, NULL, '/uploads/31282922-a9b9-419b-8a2e-16a16876fb6f.jpg', '65e822307b5c2.jpg', 245975, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (136, 1, NULL, '/uploads/89ca0d3e-6972-4821-80dc-a2478b2a2c78.jpg', '65e822317b700.jpg', 308773, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (137, 1, NULL, '/uploads/a245ae8f-489e-411d-877f-3c7e8d54abd5.jpg', '65e822317dd85.jpg', 233071, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (138, 1, NULL, '/uploads/0827f9ee-800c-4255-a1ff-b6d880c84838.jpg', '65e822322d992.jpg', 283776, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (139, 1, NULL, '/uploads/05436a2e-d46c-42e2-ac63-a49b699791b0.jpeg', '65e822335ecb1.jpeg', 290643, 'jpeg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (140, 1, NULL, '/uploads/e5a3fa2a-85c4-4242-aeda-1e41100cef93.jpeg', '65e822336a7ee.jpeg', 76825, 'jpeg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (141, 1, NULL, '/uploads/f8a254be-205c-452c-968b-aa9e5555fe60.jpeg', '65e822336f800.jpeg', 241219, 'jpeg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (142, 1, NULL, '/uploads/9629e89f-ed0e-4151-954c-81b44d560930.jpeg', '65e822338c94b.jpeg', 220428, 'jpeg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (143, 1, NULL, '/uploads/22c03238-ab9a-4fae-9751-2efc547ec222.jpg', '65e822341bfa3.jpg', 86183, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (144, 1, NULL, '/uploads/7a436997-8883-4ffb-9cb5-4dc29af01f8b.jpg', '65e822341f57a.jpg', 224494, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (145, 1, NULL, '/uploads/63528232-e9fa-4327-af7e-09c423424ec3.jpg', '65e822352b53f.jpg', 357533, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (146, 1, NULL, '/uploads/e2753814-24a6-4efa-8068-2be29a616857.jpg', '65e822352c5c1.jpg', 196257, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (147, 1, NULL, '/uploads/ab2c6356-da5d-4f9c-a8d3-631fbe4092b3.jpg', '65e822353bab5.jpg', 186818, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (148, 1, NULL, '/uploads/d292d4e8-1a5c-4491-aaf4-9c0853ffa8d5.jpg', '65e822360c61b.jpg', 273683, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (149, 1, NULL, '/uploads/9793e787-482d-43dd-95e1-ee045eab6488.jpg', '65e822360e566.jpg', 336927, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (150, 1, NULL, '/uploads/8aa01352-35d6-49fc-83e0-300cb3a86c68.jpg', '65e822515ab1e.jpg', 113443, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (151, 1, NULL, '/uploads/967d834c-2785-49da-82f8-8194ec43f6c7.jpg', '65e822515c75a.jpg', 206655, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (152, 1, NULL, '/uploads/6466110e-5c03-49db-86a6-75fe4b95e466.jpg', '65e822518adc0.jpg', 112216, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (153, 1, NULL, '/uploads/e8b220d9-67bd-4cc4-982b-fb74c8f5115c.jpg', '65e822518ccef.jpg', 239064, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (154, 1, NULL, '/uploads/d054e0d5-1b27-4968-a83f-d2aa1f74a475.jpg', '65e822518e155.jpg', 211755, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (155, 1, NULL, '/uploads/4f67d044-9040-4132-b244-f4f4f5c168f3.jpg', '65e822525b59f.jpg', 189920, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (156, 1, NULL, '/uploads/67ee221a-bade-4f9e-a72b-1b1cbdb69bd0.jpg', '65e822525c353.jpg', 167814, 'jpg', 0, '2026-01-12 17:44:28', 0);
INSERT INTO `biz_image` VALUES (157, 1, NULL, '/uploads/9d3977ac-8187-4c70-940d-8d25bd407633.jpg', '65e822525e229.jpg', 195782, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (158, 1, NULL, '/uploads/9386240a-a62b-41f9-a2ad-5098fc931ef2.jpg', '65e822540a31b.jpg', 327658, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (159, 1, NULL, '/uploads/75ee6054-3a4a-466e-80e5-95b1525bdde9.jpg', '65e822541e78c.jpg', 241189, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (160, 1, NULL, '/uploads/f7789a33-8358-459b-8258-5def60360602.jpg', '65e822541fafa.jpg', 124839, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (161, 1, NULL, '/uploads/ecd64edb-e958-4575-a95f-d831a7367aab.jpg', '65e822549c0d5.jpg', 455427, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (162, 1, NULL, '/uploads/b61ba56f-b9ea-4fe8-acfa-83357790bcac.jpg', '65e822551b2e4.jpg', 194550, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (163, 1, NULL, '/uploads/9c2669c1-55d3-4c87-9ae5-e39a1af7a013.jpg', '65e822554a9cc.jpg', 239409, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (164, 1, NULL, '/uploads/98932374-3685-44a9-a7dd-e23f56fcb11f.jpg', '65e822560c4a6.jpg', 198826, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (165, 1, NULL, '/uploads/12f629bb-5e19-4687-8d5b-0fe0648157f5.jpg', '65e822565b211.jpg', 273868, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (166, 1, NULL, '/uploads/79c0730b-5833-485f-a4ae-31f30a868289.jpg', '65e822570b6b1.jpg', 290262, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (167, 1, NULL, '/uploads/de331ded-3812-4004-8a7e-46b07ceac709.jpg', '65e822570db78.jpg', 409178, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (168, 1, NULL, '/uploads/1bfa847e-a24b-49fd-b093-78a128e5601a.jpg', '65e822575b68c.jpg', 327172, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (169, 1, NULL, '/uploads/fbe98700-49f9-43ec-b33a-bde33d4ab68f.jpg', '65e822576bff7.jpg', 470335, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (170, 1, NULL, '/uploads/b83e098d-abf5-4886-9565-b96d3fcd9afe.jpg', '65e822579f3b9.jpg', 397068, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (171, 1, NULL, '/uploads/dc06c738-4fb0-44da-a15b-ca805236431b.jpeg', '65e8223308a41.jpeg', 69415, 'jpeg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (172, 1, NULL, '/uploads/c951e4be-af35-4cae-9044-8b1bbdfa14f0.jpeg', '65e8223342bcb.jpeg', 76234, 'jpeg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (173, 1, NULL, '/uploads/244aeaaa-7236-4f99-bb60-2997911a16a9.jpg', '65e8223420db6.jpg', 373843, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (174, 1, NULL, '/uploads/76121d01-20d9-4944-8136-90ad7319c437.jpg', '65e8223529d97.jpg', 183696, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (175, 1, NULL, '/uploads/7967d12f-e1fd-4f0d-bfd3-2a02e1270081.jpg', '65e8225223c8d.jpg', 243500, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (176, 1, NULL, '/uploads/2fae2800-9948-4833-9077-585cac3ba99d.jpg', '65e8225343c24.jpg', 117484, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (177, 1, NULL, '/uploads/8600a883-0198-457b-bd8f-f3a6f0d762ed.jpg', '65e8225347e74.jpg', 234543, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (178, 1, NULL, '/uploads/c02b6b22-1128-4952-be09-2f8ac1ac1329.jpg', '65e8225408d0e.jpg', 94839, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (179, 1, NULL, '/uploads/c5838fbb-d0c2-421f-95be-5f0678600715.jpg', '65e8225617cc7.jpg', 231382, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (180, 1, NULL, '/uploads/65dae5b4-d19c-4560-9b20-06e2a622af40.jpg', '65e8225644ab5.jpg', 198832, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (181, 1, NULL, '/uploads/bca83c0f-7e4d-436c-b54c-984f4dbe6184.jpg', '65e8225680e7a.jpg', 157207, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (182, 1, NULL, '/uploads/20ebad18-91a1-4a0a-920f-a3657bfc5e42.jpg', '65e8225733bc0.jpg', 311256, 'jpg', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (183, 1, NULL, '/uploads/2c10803f-dcd7-4cde-a47b-2ffeda3dae7c.png', '65e82230470b1.png', 720801, 'png', 0, '2026-01-12 17:44:29', 0);
INSERT INTO `biz_image` VALUES (184, 1, NULL, '/uploads/60e6ad75-920d-47e6-bd8f-67995931c7dd.jpg', '6620cabab90b5.jpg', 77508, 'jpg', 0, '2026-01-13 11:46:57', 0);
INSERT INTO `biz_image` VALUES (185, 1, NULL, '/uploads/56937199-bf59-4a93-b5a5-4bcfcaee2c64.jpg', '65e82b2d6b596.jpg', 190801, 'jpg', 0, '2026-01-13 11:49:29', 0);
INSERT INTO `biz_image` VALUES (186, 1, NULL, '/uploads/6a2e6edf-f948-4d92-9568-32f5240580ac.jpg', '65e82b2b00fa4.jpg', 159724, 'jpg', 0, '2026-01-13 11:51:32', 0);
INSERT INTO `biz_image` VALUES (187, 1, 1, '/uploads/9aac23ef-2093-487d-a855-7fda0cc0f757.jpg', '“把生活的温柔藏进壁纸里✨”@神图壁纸 口令取图g6689 #该换壁纸了 #全屏高清壁纸 #美女壁纸 #手机壁纸@DOU+小助手(9).jpg', 487525, 'jpg', 0, '2026-01-13 15:47:15', 0);
INSERT INTO `biz_image` VALUES (188, 1, 1, '/uploads/83cf7e02-c2bf-4f75-91d6-273646bb6e10.jpg', '65e82253447a9.jpg', 163360, 'jpg', 0, '2026-01-13 16:02:01', 0);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联用户ID (NULL表示系统默认配置)',
  `config_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键 (如: max_file_size, upload_path, allowed_extensions)',
  `config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_key`(`user_id` ASC, `config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统/用户配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, NULL, 'max_file_size', '10485760', '默认最大上传大小', '单位: Byte, 默认10MB', '2026-01-12 11:01:43', '2026-01-12 11:01:43');
INSERT INTO `sys_config` VALUES (2, NULL, 'upload_path', 'd:/lewis/uploads/default/', '默认上传路径', '文件存储的物理路径', '2026-01-12 11:01:43', '2026-01-12 15:00:25');
INSERT INTO `sys_config` VALUES (3, NULL, 'allowed_extensions', 'jpg,jpeg,png,gif', '允许上传的后缀', '逗号分隔', '2026-01-12 11:01:43', '2026-01-12 11:01:43');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色标识',
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '管理员', 'ADMIN', '2026-01-12 15:28:42', NULL);
INSERT INTO `sys_role` VALUES (2, '普通用户', 'USER', '2026-01-12 15:28:42', NULL);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除 1:已删 0:未删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', 'e6be6b483e7e05279d248898d32babd4', '管理员', '/uploads/44df31d3-22c4-4e1c-b70c-06706689ec5d.png', '123@qq.com', '13800138000', 1, '2026-01-11 17:42:55', '2026-01-11 17:42:55', 0);
INSERT INTO `sys_user` VALUES (2, 'test', 'e6be6b483e7e05279d248898d32babd4', '用户4635', NULL, NULL, NULL, 1, '2026-01-12 15:37:05', '2026-01-12 15:37:05', 0);
INSERT INTO `sys_user` VALUES (3, 'test1', 'e6be6b483e7e05279d248898d32babd4', '用户1547', NULL, NULL, NULL, 1, '2026-01-12 15:40:45', '2026-01-12 15:40:45', 0);
INSERT INTO `sys_user` VALUES (4, 'test3', 'e6be6b483e7e05279d248898d32babd4', '用户2470', NULL, NULL, NULL, 1, '2026-01-13 11:31:54', '2026-01-13 11:31:54', 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);
INSERT INTO `sys_user_role` VALUES (3, 2);
INSERT INTO `sys_user_role` VALUES (4, 2);

SET FOREIGN_KEY_CHECKS = 1;
