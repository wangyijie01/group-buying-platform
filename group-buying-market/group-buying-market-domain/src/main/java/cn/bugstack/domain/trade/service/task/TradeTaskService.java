package cn.bugstack.domain.trade.service.task;

import cn.bugstack.domain.trade.adapter.port.ITradePort;
import cn.bugstack.domain.trade.adapter.repository.ITradeRepository;
import cn.bugstack.domain.trade.model.entity.NotifyTaskEntity;
import cn.bugstack.domain.trade.service.ITradeTaskService;
import cn.bugstack.types.enums.NotifyTaskHTTPEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易任务（MT/HTTP）服务
 *
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 2025/7/12 21:15
 */
@Slf4j
@Service
public class TradeTaskService implements ITradeTaskService {

    private static final int MAX_NOTIFY_ATTEMPTS = 5;

    private final ITradeRepository repository;
    private final ITradePort port;

    public TradeTaskService(ITradeRepository repository, ITradePort port) {
        this.repository = repository;
        this.port = port;
    }

    @Override
    public Map<String, Integer> execNotifyJob() throws Exception {
        log.info("拼团交易-执行回调通知任务");

        // 查询未执行任务
        List<NotifyTaskEntity> notifyTaskEntityList = repository.queryUnExecutedNotifyTaskList();

        return execNotifyJob(notifyTaskEntityList);
    }

    @Override
    public Map<String, Integer> execNotifyJob(String teamId) throws Exception {
        log.info("拼团交易-执行回调通知回调，指定 teamId:{}", teamId);
        List<NotifyTaskEntity> notifyTaskEntityList = repository.queryUnExecutedNotifyTaskList(teamId);
        return execNotifyJob(notifyTaskEntityList);
    }

    @Override
    public Map<String, Integer> execNotifyJob(NotifyTaskEntity notifyTaskEntity) throws Exception {
        log.info("拼团交易-执行指定回调任务 teamId:{} uuid:{}", notifyTaskEntity.getTeamId(), notifyTaskEntity.getUuid());
        return execNotifyJob(Collections.singletonList(notifyTaskEntity));
    }

    private Map<String, Integer> execNotifyJob(List<NotifyTaskEntity> notifyTaskEntityList) throws Exception {
        if (notifyTaskEntityList == null) {
            notifyTaskEntityList = Collections.emptyList();
        }

        int successCount = 0;
        int errorCount = 0;
        int retryCount = 0;
        int deferredCount = 0;
        int conflictCount = 0;

        for (NotifyTaskEntity notifyTask : notifyTaskEntityList) {
            String response;
            try {
                response = port.groupBuyNotify(notifyTask);
            } catch (Exception e) {
                // 单条任务失败不能中断整批扫描；失败任务转入重试状态，其余任务继续推进。
                log.warn("拼团通知执行异常，转入重试 teamId:{} uuid:{}", notifyTask.getTeamId(), notifyTask.getUuid(), e);
                response = NotifyTaskHTTPEnumVO.ERROR.getCode();
            }

            if (NotifyTaskHTTPEnumVO.SUCCESS.getCode().equals(response)) {
                int updateCount = repository.updateNotifyTaskStatusSuccess(notifyTask);
                if (1 == updateCount) {
                    successCount += 1;
                } else {
                    conflictCount += 1;
                }
            } else if (NotifyTaskHTTPEnumVO.ERROR.getCode().equals(response)) {
                int notifyCount = notifyTask.getNotifyCount() == null ? 0 : notifyTask.getNotifyCount();
                if (notifyCount + 1 >= MAX_NOTIFY_ATTEMPTS) {
                    int updateCount = repository.updateNotifyTaskStatusError(notifyTask);
                    if (1 == updateCount) {
                        errorCount += 1;
                    } else {
                        conflictCount += 1;
                    }
                } else {
                    int updateCount = repository.updateNotifyTaskStatusRetry(notifyTask);
                    if (1 == updateCount) {
                        retryCount += 1;
                    } else {
                        conflictCount += 1;
                    }
                }
            } else {
                // 未抢到单任务锁时不消耗重试次数，留给下一轮调度。
                deferredCount += 1;
            }
        }

        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("waitCount", notifyTaskEntityList.size());
        resultMap.put("successCount", successCount);
        resultMap.put("errorCount", errorCount);
        resultMap.put("retryCount", retryCount);
        resultMap.put("deferredCount", deferredCount);
        resultMap.put("conflictCount", conflictCount);

        return resultMap;
    }

}
