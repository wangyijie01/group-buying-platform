package cn.bugstack.domain.goods.service;

import cn.bugstack.domain.goods.adapter.repository.IGoodsRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 结算服务
 * @since 2025-02-15
 */
@Service
public class GoodsService implements IGoodsService {

    @Resource
    private IGoodsRepository repository;


    @Override
    public void changeOrderDealDone(String orderId) {
        repository.changeOrderDealDone(orderId);
    }

}
