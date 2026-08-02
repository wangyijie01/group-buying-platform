package cn.bugstack.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 营销支付优惠
 * @since 2025-02-06
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketPayDiscountEntity {

    /** 原始价格 */
    private BigDecimal originalPrice;
    /** 折扣金额 */
    private BigDecimal deductionPrice;
    /** 支付金额 */
    private BigDecimal payPrice;

}
