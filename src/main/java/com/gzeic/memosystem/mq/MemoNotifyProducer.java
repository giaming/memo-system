package com.gzeic.memosystem.mq;

import com.gzeic.memosystem.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 备忘录提醒消息生产者
 *
 * 职责：
 * - 把“需要提醒”的事件封装为消息
 * - 发送到 RabbitMQ 的交换机，由队列异步消费
 *
 * 多租户注意：
 * - 发送前必须把 tenantId 写进消息体
 */
@Component
@RequiredArgsConstructor
public class MemoNotifyProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送到期提醒消息
     *
     * @param message 消息体（必须包含 tenantId/userId/memoId）
     */
    public void sendMemoDueMessage(MemoDueMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NOTIFY,
                RabbitMQConfig.ROUTING_KEY_NOTIFY,
                message
        );
    }
}

