package cn.bugstack.trigger.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 结算完成消息监听
 * @since 2025-03-08
 */
@Slf4j
@Component
public class TeamSuccessTopicListener {

    @RabbitListener(queues = "${spring.rabbitmq.config.producer.topic_team_success.queue}")
    public void listener(String message) {
        log.info("接收消息（组队成功）:{}", message);
    }

}
