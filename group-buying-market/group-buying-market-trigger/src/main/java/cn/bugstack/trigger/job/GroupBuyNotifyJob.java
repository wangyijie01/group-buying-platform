package cn.bugstack.trigger.job;

import cn.bugstack.domain.trade.service.ITradeSettlementOrderService;
import cn.bugstack.domain.trade.service.ITradeTaskService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 拼团完结回调通知任务；拼团回调任务表，实际公司场景会定时清理数据结转，不会有太多数据挤压
 * @since 2025-01-31
 */
@Slf4j
@Service
public class GroupBuyNotifyJob {

    @Resource
    private ITradeTaskService tradeTaskService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 默认每分钟扫描一次，可通过配置覆盖，缩短通知失败后的恢复窗口。
     */
    @Scheduled(cron = "${group-buying.notify.cron}")
    public void exec() {
        // 多实例只允许一个节点扫描任务；租约兜底进程异常，正常完成后主动释放。
        RLock lock = redissonClient.getLock("group_buy_market_notify_job_exec");
        try {
            boolean isLocked = lock.tryLock(3, 5, TimeUnit.MINUTES);
            if (!isLocked) return;

            Map<String, Integer> result = tradeTaskService.execNotifyJob();
            log.info("定时任务，回调通知完成 result:{}", JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("定时任务，回调通知完成失败", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
