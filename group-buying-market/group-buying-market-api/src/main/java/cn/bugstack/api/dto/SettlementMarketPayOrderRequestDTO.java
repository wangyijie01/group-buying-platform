package cn.bugstack.api.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 结算请求对象
 * @since 2025-02-02
 */
@Data
public class SettlementMarketPayOrderRequestDTO {

    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
    /** 用户ID */
    private String userId;
    /** 外部交易单号 */
    private String outTradeNo;
    /** 外部交易时间 */
    private Date outTradeTime;

}
