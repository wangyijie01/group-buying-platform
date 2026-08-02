package cn.bugstack.api;

import cn.bugstack.api.response.Response;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * DCC 动态配置中心
 * @since 2025-01-03
 */
public interface IDCCService {

    Response<Boolean> updateConfig(String key, String value);

}
