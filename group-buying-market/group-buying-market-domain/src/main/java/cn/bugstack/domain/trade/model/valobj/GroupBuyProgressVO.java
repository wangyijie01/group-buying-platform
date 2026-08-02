package cn.bugstack.domain.trade.model.valobj;

import lombok.*;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 拼团进度值对象
 * @since 2025-01-11
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyProgressVO {

    /** 目标数量 */
    private Integer targetCount;
    /** 完成数量 */
    private Integer completeCount;
    /** 锁单数量 */
    private Integer lockCount;

}
