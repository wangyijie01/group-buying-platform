package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.goods.adapter.repository.IGoodsRepository;
import cn.bugstack.infrastructure.dao.IOrderDao;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 结算仓储服务
 * @since 2025-02-15
 */
@Repository
public class GoodsRepository implements IGoodsRepository {

    @Resource
    private IOrderDao orderDao;

    @Override
    public void changeOrderDealDone(String orderId) {
        orderDao.changeOrderDealDone(orderId);
    }

}
