-- ============================================
-- 备忘录系统数据库初始化脚本
-- 版本: V1.1 2026-06-07
-- 说明: 按照需求规格说明书创建表结构
-- ============================================

CREATE DATABASE IF NOT EXISTS memo_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE memo_db;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_username` (`tenant_id`, `username`),
    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 备忘录表
DROP TABLE IF EXISTS `memo`;
CREATE TABLE `memo` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '备忘录主键',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `title` VARCHAR(100) NOT NULL COMMENT '备忘录标题',
    `content` VARCHAR(500) COMMENT '备忘录内容',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `is_completed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已完成',
    `is_notified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已发送提醒',
    `priority` INT NOT NULL DEFAULT 2 COMMENT '优先级：1-高，2-中（默认），3-低',
    `due_date` DATETIME COMMENT '截止日期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_tenant_user_id` (`tenant_id`, `user_id`),
    INDEX `idx_tenant_completed` (`tenant_id`, `is_completed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备忘录表';

-- 提醒记录表（可选）
DROP TABLE IF EXISTS `reminder_log`;
CREATE TABLE `reminder_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `memo_id` BIGINT NOT NULL COMMENT '备忘录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `remind_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提醒时间',
    `remind_type` VARCHAR(20) DEFAULT 'DUE' COMMENT '提醒类型',
    PRIMARY KEY (`id`),
    INDEX `idx_memo_id` (`memo_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒记录表';