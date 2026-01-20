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

 Date: 20/01/2026 15:37:09
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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '相册表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '图片表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统/用户配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, NULL, 'max_file_size', '10485760', '默认最大上传大小', '单位: Byte, 默认10MB', '2026-01-12 11:01:43', '2026-01-12 11:01:43');
INSERT INTO `sys_config` VALUES (2, NULL, 'upload_path', '/data/uploads/', '默认上传路径', '文件存储的物理路径', '2026-01-12 11:01:43', '2026-01-16 15:19:04');
INSERT INTO `sys_config` VALUES (3, NULL, 'allowed_extensions', 'jpg,jpeg,png,gif', '允许上传的后缀', '逗号分隔', '2026-01-12 11:01:43', '2026-01-12 11:01:43');
INSERT INTO `sys_config` VALUES (4, NULL, 'user_upload_dir', NULL, '用户上传子目录', '每个用户独立的存储文件夹名称，留空则存储在根目录', '2026-01-18 10:32:07', '2026-01-20 10:10:03');
INSERT INTO `sys_config` VALUES (5, NULL, 'max_storage_size', '104857600', '用户默认存储配额(100MB)', '用户默认存储配额(100MB)', '2026-01-20 10:08:08', '2026-01-20 10:10:12');

-- ----------------------------
-- Table structure for sys_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'info' COMMENT '类型: info, success, warning, error',
  `read_flag` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读',
  `user_id` bigint NULL DEFAULT NULL COMMENT '所属用户ID (NULL表示全体消息)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_message
-- ----------------------------
INSERT INTO `sys_message` VALUES (1, '测试消息', '这是一条测试的消息', 'info', 1, NULL, '2026-01-16 14:41:26', 0);
INSERT INTO `sys_message` VALUES (2, '系统欢迎消息', '欢迎使用 EverKeep 笔记系统！', 'success', 1, NULL, '2026-01-16 09:00:00', 0);
INSERT INTO `sys_message` VALUES (3, '版本更新 1.0.1', '修复了已知的一些小 Bug，提升了稳定性。', 'info', 0, NULL, '2026-01-16 09:10:00', 0);
INSERT INTO `sys_message` VALUES (4, '空间不足提醒', '您的存储空间已使用 80%，请及时清理。', 'warning', 0, 1, '2026-01-16 09:20:00', 0);
INSERT INTO `sys_message` VALUES (5, '登录异常通知', '您的账号在上海登录，请确认是否本人操作。', 'error', 1, 1, '2026-01-16 09:30:00', 0);
INSERT INTO `sys_message` VALUES (6, '周报提醒', '您上周共记录了 15 篇笔记，继续保持！', 'success', 0, 1, '2026-01-16 09:40:00', 0);
INSERT INTO `sys_message` VALUES (7, '社区守则更新', '我们更新了社区使用守则，请知悉。', 'info', 1, NULL, '2026-01-16 09:50:00', 0);
INSERT INTO `sys_message` VALUES (8, '笔记备份成功', '您的所有数据已成功备份到云端。', 'success', 1, 1, '2026-01-16 10:00:00', 0);
INSERT INTO `sys_message` VALUES (9, '付费会员到期', '您的会员服务将于 3 天后到期。', 'warning', 0, 1, '2026-01-16 10:10:00', 0);
INSERT INTO `sys_message` VALUES (10, 'API 访问限制', '您的 API 调用频率已接近上限。', 'error', 1, 1, '2026-01-16 10:20:00', 0);
INSERT INTO `sys_message` VALUES (11, '好友互动消息', '张三 评论了您的笔记《2026规划》。', 'info', 0, 1, '2026-01-16 10:30:00', 0);
INSERT INTO `sys_message` VALUES (12, '系统性能优化', '底层架构已升级，响应速度提升 50%。', 'success', 1, NULL, '2026-01-16 10:40:00', 0);
INSERT INTO `sys_message` VALUES (13, '安全中心提醒', '请定期修改您的登录密码。', 'warning', 1, NULL, '2026-01-16 10:50:00', 0);
INSERT INTO `sys_message` VALUES (14, '导入失败', '由于格式错误，笔记导入失败。', 'error', 1, 1, '2026-01-16 11:00:00', 0);
INSERT INTO `sys_message` VALUES (15, '分享链接失效', '您分享的笔记链接已过期。', 'info', 0, 1, '2026-01-16 11:10:00', 0);
INSERT INTO `sys_message` VALUES (16, '活动邀请', '参加 EverKeep 创作者大赛，赢取大奖。', 'success', 1, NULL, '2026-01-16 11:20:00', 0);
INSERT INTO `sys_message` VALUES (17, '离线同步通知', '离线期间修改的 3 篇笔记已完成同步。', 'info', 0, 1, '2026-01-16 11:30:00', 0);
INSERT INTO `sys_message` VALUES (18, '敏感词检测', '您的某篇笔记包含敏感词，已被隐藏。', 'warning', 0, 1, '2026-01-16 11:40:00', 0);
INSERT INTO `sys_message` VALUES (19, '数据库负载高', '系统目前访问量较大，可能会有延迟。', 'error', 1, NULL, '2026-01-16 11:50:00', 0);
INSERT INTO `sys_message` VALUES (20, '新皮肤上线', '深色模式现已支持自定义配色。', 'info', 0, NULL, '2026-01-16 12:00:00', 0);
INSERT INTO `sys_message` VALUES (21, '意见反馈回复', '您提交的建议已被采纳，感谢支持！', 'success', 1, 1, '2026-01-16 12:10:00', 0);
INSERT INTO `sys_message` VALUES (22, '权限变更', '您已被管理员设置为高级用户。', 'info', 0, 1, '2026-01-16 12:20:00', 0);
INSERT INTO `sys_message` VALUES (23, '存储异常', '云端同步出现异常，请手动刷新。', 'error', 1, 1, '2026-01-16 12:30:00', 0);
INSERT INTO `sys_message` VALUES (24, '节日祝福', 'EverKeep 祝您元旦快乐！', 'success', 1, NULL, '2026-01-16 12:40:00', 0);
INSERT INTO `sys_message` VALUES (25, '流量预警', '本月下行流量已使用 90%。', 'warning', 0, 1, '2026-01-16 12:50:00', 0);
INSERT INTO `sys_message` VALUES (26, '回收站清理', '回收站中的笔记将在 24 小时后彻底删除。', 'info', 1, 1, '2026-01-16 13:00:00', 0);

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容',
  `tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `color` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '颜色',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, 'EverKeep v2.0 正式发布，全新架构更稳定', 'EverKeep v2.0 正式发布，全新架构更稳定', '最新', 'red', 1, '2026-01-16 09:46:44', 0);
INSERT INTO `sys_notice` VALUES (2, '关于系统存储扩容的通知', '关于系统存储扩容的通知', '通知', 'blue', 2, '2026-01-16 09:46:44', 0);
INSERT INTO `sys_notice` VALUES (3, '如何高效管理您的个人相册？', '如何高效管理您的个人相册？', '指南', 'green', 3, '2026-01-16 09:46:44', 0);
INSERT INTO `sys_notice` VALUES (4, '社区规则更新公告', '社区规则更新公告', '规范', 'orange', 4, '2026-01-16 09:46:44', 0);
INSERT INTO `sys_notice` VALUES (5, '春节期间服务在线支持说明', '春节期间服务在线支持说明', '服务', 'purple', 5, '2026-01-16 09:46:44', 0);
INSERT INTO `sys_notice` VALUES (6, 'EverKeep 存储系统架构深度解析', '本文将深入探讨 EverKeep 如何利用对象存储技术实现海量图片的安全存储与快速访问...', '技术', 'blue', 10, '2026-01-16 10:00:00', 0);
INSERT INTO `sys_notice` VALUES (7, '关于 1月20日 凌晨系统例行维护的通知', '为了提供更优质的服务，我们将于 2026年1月20日 02:00-04:00 进行机房网络升级...', '维护', 'orange', 20, '2026-01-15 14:30:00', 0);
INSERT INTO `sys_notice` VALUES (8, '新功能预告：支持图片 AI 自动标签分类', '下一版本我们将引入 AI 识别技术，为您上传的图片自动生成风景、人物、美食等标签...', '预告', 'purple', 30, '2026-01-14 09:15:00', 0);
INSERT INTO `sys_notice` VALUES (9, '移动端 App 1.5.0 版本灰度测试开启', '诚邀各位核心用户参与移动端新版本的灰度测试，提前体验流畅的滑动预览功能...', '活动', 'green', 40, '2026-01-13 16:45:00', 0);
INSERT INTO `sys_notice` VALUES (10, '社区版权保护指南：尊重原创，共建美好空间', '作为图片存储社区，我们始终致力于保护用户的原创版权，请在分享时务必遵守相关法律法规...', '规范', 'red', 50, '2026-01-12 11:20:00', 0);
INSERT INTO `sys_notice` VALUES (11, '新手必读：如何快速导入第三方云端相册', '如果您之前使用过其他云相册，可以参考本教程快速将您的图片一键搬家到 EverKeep...', '指南', 'cyan', 60, '2026-01-11 08:50:00', 0);
INSERT INTO `sys_notice` VALUES (12, 'EverKeep 荣获年度最具潜力开源项目提名', '感谢全球开发者的支持，我们在最新的开源社区评选中获得了多项核心技术指标的认可...', '荣誉', 'gold', 70, '2026-01-10 13:10:00', 0);
INSERT INTO `sys_notice` VALUES (13, '安全提醒：请及时开启二级支付密码', '为了保障您的账户资产安全，建议所有开启了扩容服务的用户立即在个人中心设置二级密码...', '安全', 'red', 80, '2026-01-09 17:35:00', 0);
INSERT INTO `sys_notice` VALUES (14, '冬季摄影大赛：记录你身边的银装素裹', '参与冬季摄影投稿，赢取最高 1TB 的永久存储空间奖励，优秀作品将展示在首页头条...', '活动', 'green', 90, '2026-01-08 10:00:00', 0);
INSERT INTO `sys_notice` VALUES (15, '系统底层性能优化报告 v2.1.0', '在最新的补丁中，我们将图片缩略图的生成速度提升了 45%，并降低了 30% 的内存占用...', '技术', 'blue', 100, '2026-01-07 15:25:00', 0);
INSERT INTO `sys_notice` VALUES (16, '关于相册共享功能暂时下线的说明', '由于底层 API 调整，相册共享功能将暂时关闭维护，预计在一周后重新开放...', '通知', 'orange', 110, '2026-01-06 09:40:00', 0);
INSERT INTO `sys_notice` VALUES (17, '全球 CDN 节点新增部署公告', '我们已在欧洲和北美新增了 12 个加速节点，大幅提升了海外用户的上传下载体验...', '网络', 'geekblue', 120, '2026-01-05 14:15:00', 0);
INSERT INTO `sys_notice` VALUES (18, 'EverKeep 开发者周刊：插件系统开发指南', '本期周刊将教您如何编写自定义存储插件，让 EverKeep 支持更多的后端存储服务...', '技术', 'blue', 130, '2026-01-04 11:05:00', 0);
INSERT INTO `sys_notice` VALUES (19, '用户反馈中心：您的建议我们都在听', '上周我们共收到了 156 条有效建议，其中关于 UI 适配的 12 项改动已进入排期...', '反馈', 'lime', 140, '2026-01-03 16:55:00', 0);
INSERT INTO `sys_notice` VALUES (20, '新年快乐！EverKeep 祝您 2026 万事如意', '新的一年，我们将继续陪伴您记录生活的点点滴滴，感谢一路有你...', '节日', 'red', 150, '2026-01-01 00:00:01', 0);

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
INSERT INTO `sys_user` VALUES (1, 'admin', 'dc2192a2a9f3c99db5e13bf52b795964', '管理员', '/uploads/avatar/2026/01/18/b3c54dab-bf11-40f4-bfaa-60c52cb8f972.png', '123@qq.com', '13800138000', 1, '2026-01-11 17:42:55', '2026-01-11 17:42:55', 0);

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

SET FOREIGN_KEY_CHECKS = 1;
