package cn.bugstack.domain.trade.service.task;

import cn.bugstack.domain.trade.adapter.port.ITradePort;
import cn.bugstack.domain.trade.adapter.repository.ITradeRepository;
import cn.bugstack.domain.trade.model.entity.NotifyTaskEntity;
import cn.bugstack.types.enums.NotifyTaskHTTPEnumVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeTaskServiceTest {

    @Mock
    private ITradeRepository repository;
    @Mock
    private ITradePort port;

    private TradeTaskService service;

    @BeforeEach
    void setUp() {
        service = new TradeTaskService(repository, port);
    }

    @Test
    void shouldMarkTaskSuccessful() throws Exception {
        NotifyTaskEntity task = task("task-success", 0);
        when(repository.queryUnExecutedNotifyTaskList()).thenReturn(Collections.singletonList(task));
        when(port.groupBuyNotify(task)).thenReturn(NotifyTaskHTTPEnumVO.SUCCESS.getCode());
        when(repository.updateNotifyTaskStatusSuccess(task)).thenReturn(1);

        Map<String, Integer> result = service.execNotifyJob();

        assertEquals(1, result.get("successCount"));
        assertEquals(0, result.get("retryCount"));
        assertEquals(0, result.get("conflictCount"));
        verify(repository).updateNotifyTaskStatusSuccess(task);
    }

    @Test
    void shouldRetryBeforeAttemptLimitAndStopAtLimit() throws Exception {
        NotifyTaskEntity retryTask = task("task-retry", 3);
        NotifyTaskEntity terminalTask = task("task-terminal", 4);
        when(repository.queryUnExecutedNotifyTaskList()).thenReturn(Arrays.asList(retryTask, terminalTask));
        when(port.groupBuyNotify(any(NotifyTaskEntity.class))).thenReturn(NotifyTaskHTTPEnumVO.ERROR.getCode());
        when(repository.updateNotifyTaskStatusRetry(retryTask)).thenReturn(1);
        when(repository.updateNotifyTaskStatusError(terminalTask)).thenReturn(1);

        Map<String, Integer> result = service.execNotifyJob();

        assertEquals(1, result.get("retryCount"));
        assertEquals(1, result.get("errorCount"));
        verify(repository).updateNotifyTaskStatusRetry(retryTask);
        verify(repository).updateNotifyTaskStatusError(terminalTask);
    }

    @Test
    void shouldContinueBatchWhenOnePortCallThrows() throws Exception {
        NotifyTaskEntity failedTask = task("task-failed", 0);
        NotifyTaskEntity successTask = task("task-next", 0);
        when(repository.queryUnExecutedNotifyTaskList()).thenReturn(Arrays.asList(failedTask, successTask));
        when(port.groupBuyNotify(failedTask)).thenThrow(new IllegalStateException("downstream unavailable"));
        when(port.groupBuyNotify(successTask)).thenReturn(NotifyTaskHTTPEnumVO.SUCCESS.getCode());
        when(repository.updateNotifyTaskStatusRetry(failedTask)).thenReturn(1);
        when(repository.updateNotifyTaskStatusSuccess(successTask)).thenReturn(1);

        Map<String, Integer> result = service.execNotifyJob();

        assertEquals(2, result.get("waitCount"));
        assertEquals(1, result.get("retryCount"));
        assertEquals(1, result.get("successCount"));
    }

    @Test
    void shouldDeferWithoutConsumingRetryWhenTaskLockIsBusy() throws Exception {
        NotifyTaskEntity task = task("task-busy", 2);
        when(repository.queryUnExecutedNotifyTaskList()).thenReturn(Collections.singletonList(task));
        when(port.groupBuyNotify(task)).thenReturn(NotifyTaskHTTPEnumVO.DEFERRED.getCode());

        Map<String, Integer> result = service.execNotifyJob();

        assertEquals(1, result.get("deferredCount"));
        verify(repository, never()).updateNotifyTaskStatusRetry(any());
        verify(repository, never()).updateNotifyTaskStatusError(any());
    }

    private NotifyTaskEntity task(String uuid, int notifyCount) {
        return NotifyTaskEntity.builder()
                .teamId("team-01")
                .uuid(uuid)
                .notifyCount(notifyCount)
                .build();
    }
}
