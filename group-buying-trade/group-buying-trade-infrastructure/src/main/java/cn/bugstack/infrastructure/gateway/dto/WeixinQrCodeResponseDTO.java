package cn.bugstack.infrastructure.gateway.dto;

import lombok.Data;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 获取微信登录二维码响应对象
 * @since 2024-02-25
 */
@Data
public class WeixinQrCodeResponseDTO {

    private String ticket;
    private Long expire_seconds;
    private String url;

}
