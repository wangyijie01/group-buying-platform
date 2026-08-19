package cn.bugstack.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 声明营销域交换机、业务队列和死信队列。
 * 消费失败超过重试上限后由 RabbitMQ 路由到对应 .dlq 队列，避免消息被静默丢弃。
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public Declarables groupBuyMarketDeclarables(
            @Value("${spring.rabbitmq.config.producer.exchange}") String exchangeName,
            @Value("${spring.rabbitmq.config.producer.topic_team_success.routing_key}") String successRoutingKey,
            @Value("${spring.rabbitmq.config.producer.topic_team_success.queue}") String successQueueName,
            @Value("${spring.rabbitmq.config.producer.topic_team_refund.routing_key}") String refundRoutingKey,
            @Value("${spring.rabbitmq.config.producer.topic_team_refund.queue}") String refundQueueName,
            @Value("${spring.rabbitmq.config.dead-letter.exchange}") String deadLetterExchangeName) {

        TopicExchange exchange = ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
        TopicExchange deadLetterExchange = ExchangeBuilder.topicExchange(deadLetterExchangeName).durable(true).build();

        Queue successQueue = businessQueue(successQueueName, deadLetterExchangeName, deadRoutingKey(successRoutingKey));
        Queue refundQueue = businessQueue(refundQueueName, deadLetterExchangeName, deadRoutingKey(refundRoutingKey));
        Queue successDeadQueue = QueueBuilder.durable(successQueueName + ".dlq").build();
        Queue refundDeadQueue = QueueBuilder.durable(refundQueueName + ".dlq").build();

        return new Declarables(
                exchange,
                deadLetterExchange,
                successQueue,
                refundQueue,
                successDeadQueue,
                refundDeadQueue,
                BindingBuilder.bind(successQueue).to(exchange).with(successRoutingKey),
                BindingBuilder.bind(refundQueue).to(exchange).with(refundRoutingKey),
                BindingBuilder.bind(successDeadQueue).to(deadLetterExchange).with(deadRoutingKey(successRoutingKey)),
                BindingBuilder.bind(refundDeadQueue).to(deadLetterExchange).with(deadRoutingKey(refundRoutingKey))
        );
    }

    private Queue businessQueue(String queueName, String deadLetterExchange, String deadLetterRoutingKey) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(deadLetterExchange)
                .deadLetterRoutingKey(deadLetterRoutingKey)
                .build();
    }

    private String deadRoutingKey(String routingKey) {
        return "dead." + routingKey;
    }
}
