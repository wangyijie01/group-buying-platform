package cn.bugstack.api.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 回调请求对象
 * @since 2025-01-31
 */
@Data
public class NotifyRequestDTO {

    /** 组队ID */
    private String teamId;
    /** 外部单号 */
    private List<String> outTradeNoList;

}
