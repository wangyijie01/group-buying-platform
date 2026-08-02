package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.Sku;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 商品查询
 * @since 2024-12-21
 */
@Mapper
public interface ISkuDao {

    Sku querySkuByGoodsId(String goodsId);

}
