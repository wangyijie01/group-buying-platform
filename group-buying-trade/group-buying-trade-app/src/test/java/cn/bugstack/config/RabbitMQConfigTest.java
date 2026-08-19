package cn.bugstack.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitMQConfigTest {

    @Test
    void shouldDeclareThreeRecoverableQueuesAndTheirDlqs() {
        Declarables declarables = new RabbitMQConfig().groupBuyingTradeDeclarables(
                "trade.exchange",
                "topic.pay_success",
                "trade.pay-success",
                "market.exchange",
                "topic.team_success",
                "trade.team-success",
                "topic.team_refund",
                "trade.team-refund",
                "trade.dlx"
        );

        List<Queue> queues = declarables.getDeclarables().stream()
                .filter(Queue.class::isInstance)
                .map(Queue.class::cast)
                .collect(Collectors.toList());

        assertEquals(6, queues.size());
        assertEquals(3, queues.stream().filter(queue -> queue.getName().endsWith(".dlq")).count());
        Queue payQueue = queues.stream()
                .filter(queue -> "trade.pay-success".equals(queue.getName()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertEquals("trade.dlx", payQueue.getArguments().get("x-dead-letter-exchange"));
        assertTrue(queues.stream().anyMatch(queue -> "trade.pay-success.dlq".equals(queue.getName())));
    }
}
