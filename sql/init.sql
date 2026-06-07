-- ============================================
-- 备忘录系统数据库初始化脚本
-- 版本: V1.1 2026-06-07
-- 说明: 按照需求规格说明书创建表结构
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS memo_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE memo_db;

-- ============================================
-- 用户表
-- 对应需求规格说明书 UC-01（用户注册）和 UC-02（用户登录）
-- ============================================
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    -- 用户主键（自增）
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    -- 租户 ID（多租户隔离的核心字段）
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID，用于多租户隔离',
    -- 用户名（在同一个租户内唯一）
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    -- 密码（数据库中保存加密后的密文，如 BCrypt）
    `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    -- 邮箱（可选）
    `email` VARCHAR(100) COMMENT '邮箱',
    -- 手机号（可选）
    `phone` VARCHAR(20) COMMENT '手机号',
    -- 创建时间（由 MyBatis-Plus 自动填充）
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    -- 更新时间（由 MyBatis-Plus 自动填充）
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    -- 租户内用户名唯一约束
    UNIQUE KEY `uk_tenant_username` (`tenant_id`, `username`),
    -- 租户索引
    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 备忘录表
-- 对应需求规格说明书 UC-03 至 UC-10
-- ============================================
DROP TABLE IF EXISTS `memo`;

CREATE TABLE `memo` (
    -- 备忘录主键（自增）
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '备忘录主键',
    -- 租户 ID（多租户隔离的核心字段）
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID，用于多租户隔离',
    -- 标题（必填，最大100字符）
    `title` VARCHAR(100) NOT NULL COMMENT '备忘录标题',
    -- 内容（可选，最大500字符）
    `content` VARCHAR(500) COMMENT '备忘录内容',
    -- 所属用户 ID
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    -- 是否已完成（0-未完成，1-已完成）
    `is_completed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已完成',
    -- 是否已发送过"到期提醒"（用于幂等控制）
    `is_notified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已发送提醒',
    -- 优先级（1=HIGH, 2=MEDIUM, 3=LOW）
    `priority` INT NOT NULL DEFAULT 2 COMMENT '优先级：1-高，2-中（默认），3-低',
    -- 截止日期（可选）
    `due_date` DATETIME COMMENT '截止日期',
    -- 创建时间（由 MyBatis-Plus 自动填充）
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    -- 更新时间（由 MyBatis-Plus 自动填充）
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    -- 用户索引
    INDEX `idx_user_id` (`user_id`),
    -- 租户索引
    INDEX `idx_tenant_id` (`tenant_id`),
    -- 租户+用户联合索引
    INDEX `idx_tenant_user_id` (`tenant_id`, `user_id`),
    -- 租户+完成状态索引（用于每日汇总统计）
    INDEX `idx_tenant_completed` (`tenant_id`, `is_completed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备忘录表';

-- ============================================
-- 初始化测试数据（租户1001）
-- ============================================

-- 插入测试用户（密码为 BCrypt 加密的 "123456"）
INSERT INTO `user` (`tenant_id`, `username`, `password`, `email`) VALUES
(1001, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E.', 'admin@example.com'),
(1001, 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E.', 'test@example.com');

-- 插入测试备忘录
INSERT INTO `memo` (`tenant_id`, `title`, `content`, `user_id`, `priority`, `due_date`) VALUES
(1001, '完成项目报告', '需要在下周五前提交给客户', 1, 1, DATE_ADD(NOW(), INTERVAL 7 DAY)),
(1001, '团队周会', '讨论本周工作进展和下周计划', 1, 2, DATE_ADD(NOW(), INTERVAL 2 DAY)),
(1001, '代码评审', 'Review团队提交的PR', 1, 1, DATE_ADD(NOW(), INTERVAL 1 DAY)),
(1001, '阅读技术文档', '学习Spring Boot新特性', 1, 3, DATE_ADD(NOW(), INTERVAL 14 DAY)),
(1001, '逾期任务示例', '这是一个已过期的任务', 1, 2, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1001, '测试用户备忘录', '测试用户创建的备忘录', 2, 2, DATE_ADD(NOW(), INTERVAL 5 DAY));

-- ============================================
-- 创建提醒记录表（可选，用于去重和记录）
-- 对应需求规格说明书 UC-09（到期提醒）
-- ============================================
DROP TABLE IF EXISTS `reminder_log`;

CREATE TABLE `reminder_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `memo_id` BIGINT NOT NULL COMMENT '备忘录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `remind_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提醒时间',
    `remind_type` VARCHAR(20) DEFAULT 'DUE' COMMENT '提醒类型：DUE-到期提醒，EARLY-提前提醒',
    PRIMARY KEY (`id`),
    INDEX `idx_memo_id` (`memo_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒记录表';