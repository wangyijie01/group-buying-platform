package cn.bugstack.trigger.listener;

import cn.bugstack.api.dto.NotifyRequestDTO;
import cn.bugstack.api.dto.TeamRefundSuccessRequestDTO;
import cn.bugstack.domain.order.service.IOrderService;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 营销退单成功消息
 *
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 2025/8/1 09:52
 */
@Slf4j
@Component
public class RefundSuccessTopicListener {

    @Resource
    private IOrderService orderService;

    /**
     * 指定消费队列
     */
    @RabbitListener(queues = "${spring.rabbitmq.config.consumer.topic_team_refund.queue}")
    public void listener(String message) {
        try {
            log.info("退单回调，发起退款 {}", message);
            TeamRefundSuccessRequestDTO requestDTO = JSON.parseObject(message, TeamRefundSuccessRequestDTO.class);
            String type = requestDTO.getType();
            if ("paid_unformed".equals(type) || "paid_formed".equals(type)) {
                orderService.refundPayOrder(requestDTO.getUserId(), requestDTO.getOutTradeNo());
            }
        } catch (AlipayApiException ex) {
            throw new RuntimeException(ex);
        } catch (Exception e) {
            log.error("拼团回调，退单完成，退款失败 {}", message, e);
            throw e;
        }
    }

}
