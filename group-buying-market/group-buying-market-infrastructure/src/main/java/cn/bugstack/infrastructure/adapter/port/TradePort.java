package cn.bugstack.infrastructure.adapter.port;

import cn.bugstack.domain.trade.adapter.port.ITradePort;
import cn.bugstack.domain.trade.model.entity.NotifyTaskEntity;
import cn.bugstack.domain.trade.model.valobj.NotifyTypeEnumVO;
import cn.bugstack.infrastructure.event.EventPublisher;
import cn.bugstack.infrastructure.gateway.GroupBuyNotifyService;
import cn.bugstack.infrastructure.redis.IRedisService;
import cn.bugstack.types.enums.NotifyTaskHTTPEnumVO;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 交易接口服务
 * @since 2025-01-31
 */
@Service
public class TradePort implements ITradePort {

    @Resource
    private GroupBuyNotifyService groupBuyNotifyService;
    @Resource
    private IRedisService redisService;
    @Resource
    private EventPublisher publisher;

    @Override
    public String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception {
        RLock lock = redisService.getLock(notifyTask.lockKey());
        try {
            // 同一通知任务按业务键串行执行；有限租约可在进程异常时自动释放，避免永久占锁。
            if (lock.tryLock(3, 2, TimeUnit.MINUTES)) {
                try {
                    // 回调方式 HTTP
                    if (NotifyTypeEnumVO.HTTP.getCode().equals(notifyTask.getNotifyType())) {
                        // 无效的 notifyUrl 则直接返回成功
                        if (StringUtils.isBlank(notifyTask.getNotifyUrl()) || "暂无".equals(notifyTask.getNotifyUrl())) {
                            return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
                        }
                        groupBuyNotifyService.groupBuyNotify(notifyTask.getNotifyUrl(), notifyTask.getParameterJson());
                        return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
                    }

                    // 回调方式 MQ
                    if (NotifyTypeEnumVO.MQ.getCode().equals(notifyTask.getNotifyType())) {
                        publisher.publish(notifyTask.getNotifyMQ(), notifyTask.getParameterJson());
                        return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
                    }
                } finally {
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
            return NotifyTaskHTTPEnumVO.NULL.getCode();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return NotifyTaskHTTPEnumVO.ERROR.getCode();
        } catch (Exception e) {
            return NotifyTaskHTTPEnumVO.ERROR.getCode();
        }
    }

}
