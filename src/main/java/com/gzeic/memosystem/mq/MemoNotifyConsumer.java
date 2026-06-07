package com.gzeic.memosystem.mq;

import com.gzeic.memosystem.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 备忘录提醒消息消费者
 *
 * 说明：
 * - 这是一个教学版消费者：收到消息后先打印日志
 * - 后续可以扩展为：发邮件/发短信/站内通知/推送 WebSocket 等
 *
 * 死信队列演示：
 * - 如果该方法抛出异常，并且配置了 default-requeue-rejected=false
 * - RabbitMQ 会把这条消息投递到死信交换机，再进入死信队列，便于排查
 */
@Slf4j
@Component
public class MemoNotifyConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFY)
    public void onMessage(MemoDueMessage message) {
        log.info("收到备忘录到期提醒消息 tenantId={}, userId={}, memoId={}, dueDate={}",
                message.getTenantId(),
                message.getUserId(),
                message.getMemoId(),
                message.getDueDate());

        if (message.getMemoId() != null && message.getMemoId() < 0) {
            throw new IllegalArgumentException("演示死信：memoId 为负数，模拟消费失败");
        }
    }
}

