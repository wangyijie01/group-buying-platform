package cn.bugstack.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 声明商城域生产/消费队列及死信路由，确保超过消费重试上限的消息可追踪、可补偿。
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public Declarables groupBuyingTradeDeclarables(
            @Value("${spring.rabbitmq.config.producer.topic_order_pay_success.exchange}") String tradeExchangeName,
            @Value("${spring.rabbitmq.config.producer.topic_order_pay_success.routing_key}") String payRoutingKey,
            @Value("${spring.rabbitmq.config.producer.topic_order_pay_success.queue}") String payQueueName,
            @Value("${spring.rabbitmq.config.consumer.topic_team_success.exchange}") String marketExchangeName,
            @Value("${spring.rabbitmq.config.consumer.topic_team_success.routing_key}") String teamSuccessRoutingKey,
            @Value("${spring.rabbitmq.config.consumer.topic_team_success.queue}") String teamSuccessQueueName,
            @Value("${spring.rabbitmq.config.consumer.topic_team_refund.routing_key}") String teamRefundRoutingKey,
            @Value("${spring.rabbitmq.config.consumer.topic_team_refund.queue}") String teamRefundQueueName,
            @Value("${spring.rabbitmq.config.dead-letter.exchange}") String deadLetterExchangeName) {

        TopicExchange tradeExchange = ExchangeBuilder.topicExchange(tradeExchangeName).durable(true).build();
        TopicExchange marketExchange = ExchangeBuilder.topicExchange(marketExchangeName).durable(true).build();
        TopicExchange deadLetterExchange = ExchangeBuilder.topicExchange(deadLetterExchangeName).durable(true).build();

        Queue payQueue = businessQueue(payQueueName, deadLetterExchangeName, deadRoutingKey(payRoutingKey));
        Queue teamSuccessQueue = businessQueue(teamSuccessQueueName, deadLetterExchangeName, deadRoutingKey(teamSuccessRoutingKey));
        Queue teamRefundQueue = businessQueue(teamRefundQueueName, deadLetterExchangeName, deadRoutingKey(teamRefundRoutingKey));
        Queue payDeadQueue = QueueBuilder.durable(payQueueName + ".dlq").build();
        Queue teamSuccessDeadQueue = QueueBuilder.durable(teamSuccessQueueName + ".dlq").build();
        Queue teamRefundDeadQueue = QueueBuilder.durable(teamRefundQueueName + ".dlq").build();

        return new Declarables(
                tradeExchange,
                marketExchange,
                deadLetterExchange,
                payQueue,
                teamSuccessQueue,
                teamRefundQueue,
                payDeadQueue,
                teamSuccessDeadQueue,
                teamRefundDeadQueue,
                BindingBuilder.bind(payQueue).to(tradeExchange).with(payRoutingKey),
                BindingBuilder.bind(teamSuccessQueue).to(marketExchange).with(teamSuccessRoutingKey),
                BindingBuilder.bind(teamRefundQueue).to(marketExchange).with(teamRefundRoutingKey),
                BindingBuilder.bind(payDeadQueue).to(deadLetterExchange).with(deadRoutingKey(payRoutingKey)),
                BindingBuilder.bind(teamSuccessDeadQueue).to(deadLetterExchange).with(deadRoutingKey(teamSuccessRoutingKey)),
                BindingBuilder.bind(teamRefundDeadQueue).to(deadLetterExchange).with(deadRoutingKey(teamRefundRoutingKey))
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
