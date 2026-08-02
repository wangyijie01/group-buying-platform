package cn.bugstack.infrastructure.gateway.dto;

import lombok.Data;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 获取 Access token DTO 对象
 * @since 2024-02-25
 */
@Data
public class WeixinTokenResponseDTO {

    private String access_token;
    private int expires_in;
    private String errcode;
    private String errmsg;

}
