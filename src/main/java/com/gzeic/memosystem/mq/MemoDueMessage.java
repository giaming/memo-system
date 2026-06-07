package com.gzeic.memosystem.mq;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 备忘录到期提醒消息体
 *
 * 为什么消息体必须包含 tenantId：
 * - RabbitMQ 是"跨线程/跨进程"的异步通信
 * - ThreadLocal（TenantContext）无法自动传播到消费者
 * - 所以消息本身必须携带 tenantId，消费者才能按租户维度处理
 *
 * @version V1.1 2026-06-07 新增 actionType 字段用于区分操作类型
 */
@Data
public class MemoDueMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户 ID（多租户隔离关键字段）
     */
    private Long tenantId;

    /**
     * 用户 ID（提醒要发给谁）
     */
    private Long userId;

    /**
     * 备忘录 ID（提醒对应哪条备忘录）
     */
    private Long memoId;

    /**
     * 到期时间（用于展示/校验）
     */
    private LocalDateTime dueDate;

    /**
     * 操作类型
     * - created: 创建备忘录
     * - updated: 更新备忘录
     * - due_reminder: 到期提醒
     */
    private String actionType;

    /**
     * 时间戳（消息发送时间）
     */
    private LocalDateTime timestamp;

    /**
     * 设置默认时间戳
     */
    public void initTimestamp() {
        this.timestamp = LocalDateTime.now();
    }
}