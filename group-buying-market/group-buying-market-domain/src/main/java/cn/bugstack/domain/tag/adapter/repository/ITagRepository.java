package cn.bugstack.domain.tag.adapter.repository;

import cn.bugstack.domain.tag.model.entity.CrowdTagsJobEntity;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 人群标签仓储接口
 * @since 2024-12-28
 */
public interface ITagRepository {

    CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId);

    void addCrowdTagsUserId(String tagId, String userId);

    void updateCrowdTagsStatistics(String tagId, int count);

}
