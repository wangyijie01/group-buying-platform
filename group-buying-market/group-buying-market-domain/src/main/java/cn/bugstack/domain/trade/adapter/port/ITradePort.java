package cn.bugstack.domain.trade.adapter.port;

import cn.bugstack.domain.trade.model.entity.NotifyTaskEntity;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 交易接口服务接口
 * @since 2025-01-31
 */
public interface ITradePort {

    String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception;

}
