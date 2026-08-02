package cn.bugstack.domain.goods.adapter.repository;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 结算仓储
 * @since 2025-02-15
 */
public interface IGoodsRepository {

    void changeOrderDealDone(String orderId);

}
