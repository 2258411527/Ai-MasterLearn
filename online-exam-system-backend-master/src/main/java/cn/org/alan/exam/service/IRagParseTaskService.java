package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.RagParseTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * RAG解析任务服务接口
 *
 * @author AI Assistant
 * @since 2026
 */
public interface IRagParseTaskService extends IService<RagParseTask> {
    
    /**
     * 创建解析任务
     *
     * @param userId 用户ID
     * @param studyMaterialId 学习资料ID
     * @return 任务ID
     */
    Integer createTask(Integer userId, Integer studyMaterialId);
    
    /**
     * 更新任务进度
     *
     * @param taskId 任务ID
     * @param progress 进度百分比
     * @param processedPages 已处理页数
     */
    void updateProgress(Integer taskId, Integer progress, Integer processedPages);
    
    /**
     * 标记任务完成
     *
     * @param taskId 任务ID
     * @param totalChunks 总分块数
     */
    void markCompleted(Integer taskId, Integer totalChunks);
    
    /**
     * 标记任务失败
     *
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    void markFailed(Integer taskId, String errorMessage);
    
    /**
     * 获取用户的解析任务列表
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    Result<List<RagParseTask>> getUserTasks(Integer userId);
    
    /**
     * 获取特定资料的最新解析任务
     *
     * @param studyMaterialId 学习资料ID
     * @return 任务信息
     */
    Result<RagParseTask> getLatestTaskByMaterial(Integer studyMaterialId);
}
