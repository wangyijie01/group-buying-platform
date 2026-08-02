package cn.bugstack.domain.tag.service;

/**
 * @author Fuzhengwei
 * @author 王奕杰（业务注释与工程化维护）
 * 人群标签服务接口
 * @since 2024-12-28
 */
public interface ITagService {

    /**
     * 执行人群标签批次任务
     *
     * @param tagId   人群ID
     * @param batchId 批次ID
     */
    void execTagBatchJob(String tagId, String batchId);

}
