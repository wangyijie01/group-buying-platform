package cn.bugstack.api.dto;

import lombok.Data;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 商品营销请求对象
 * @since 2025-02-02
 */
@Data
public class GoodsMarketRequestDTO {

    // 用户ID
    private String userId;
    // 渠道
    private String source;
    // 来源
    private String channel;
    // 商品ID
    private String goodsId;

}
