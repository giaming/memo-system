package com.gzeic.memosystem.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置（Direct 模式 + 死信队列）
 *
 * 本章目标（教学版最小可用）：
 * 1) 建立“到期提醒”主队列 memo.notify.queue
 * 2) 建立死信队列 memo.notify.dlq，用来接收消费失败的消息
 * 3) 当消费者处理失败且不重回队列时，消息会进入死信队列，方便排查与补偿
 *
 * 多租户注意：
 * - 消息体必须包含 tenantId，消费者处理时也必须按 tenantId 维度记录/处理
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NOTIFY = "memo.notify.exchange";
    public static final String QUEUE_NOTIFY = "memo.notify.queue";
    public static final String ROUTING_KEY_NOTIFY = "memo.notify";

    public static final String EXCHANGE_DLX = "memo.notify.dlx";
    public static final String QUEUE_DLQ = "memo.notify.dlq";
    public static final String ROUTING_KEY_DLQ = "memo.notify.dlq";

    @Bean
    public DirectExchange notifyExchange() {
        return new DirectExchange(EXCHANGE_NOTIFY, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    @Bean
    public Queue notifyQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EXCHANGE_DLX);
        args.put("x-dead-letter-routing-key", ROUTING_KEY_DLQ);
        return new Queue(QUEUE_NOTIFY, true, false, false, args);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(QUEUE_DLQ, true);
    }

    @Bean
    public Binding notifyBinding(Queue notifyQueue, DirectExchange notifyExchange) {
        return BindingBuilder.bind(notifyQueue).to(notifyExchange).with(ROUTING_KEY_NOTIFY);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(ROUTING_KEY_DLQ);
    }

    /**
     * 消息转换器：把 Java 对象转换为 JSON（生产者发送、消费者接收都用它）
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
