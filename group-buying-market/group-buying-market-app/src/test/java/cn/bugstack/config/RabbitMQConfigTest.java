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
    void shouldDeclareBusinessQueuesWithDeadLetterRouting() {
        Declarables declarables = new RabbitMQConfig().groupBuyMarketDeclarables(
                "market.exchange",
                "topic.team_success",
                "market.team-success",
                "topic.team_refund",
                "market.team-refund",
                "market.dlx"
        );

        List<Queue> queues = declarables.getDeclarables().stream()
                .filter(Queue.class::isInstance)
                .map(Queue.class::cast)
                .collect(Collectors.toList());

        assertEquals(4, queues.size());
        Queue refundQueue = queues.stream()
                .filter(queue -> "market.team-refund".equals(queue.getName()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertEquals("market.dlx", refundQueue.getArguments().get("x-dead-letter-exchange"));
        assertEquals("dead.topic.team_refund", refundQueue.getArguments().get("x-dead-letter-routing-key"));
        assertTrue(queues.stream().anyMatch(queue -> "market.team-refund.dlq".equals(queue.getName())));
    }
}
