package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.RagKnowledge;
import cn.org.alan.exam.model.entity.StudyMaterial;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * RAG知识库服务接口
 *
 * @author AI Assistant
 * @since 2026
 */
public interface IRagKnowledgeService extends IService<RagKnowledge> {
    
    /**
     * 解析学习资料并存入知识库
     *
     * @param studyMaterial 学习资料
     * @return 处理结果
     */
    Result<String> parseAndStoreMaterial(StudyMaterial studyMaterial);
    
    /**
     * 解析学习资料并存入知识库(带进度跟踪)
     *
     * @param studyMaterial 学习资料
     * @param taskId 任务ID
     * @return 处理结果
     */
    Result<String> parseAndStoreMaterialWithProgress(StudyMaterial studyMaterial, Integer taskId);
    
    /**
     * 检索相关知识
     *
     * @param userId 用户ID
     * @param question 问题
     * @param topK 返回最相关的K个结果
     * @return 相关知识列表
     */
    Result<List<String>> searchKnowledge(Integer userId, String question, int topK);
    
    /**
     * 删除用户的知识库数据
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    Result<String> deleteUserKnowledge(Integer userId);
    
    /**
     * 删除特定学习资料的知识库数据
     *
     * @param studyMaterialId 学习资料ID
     * @return 删除结果
     */
    Result<String> deleteMaterialKnowledge(Integer studyMaterialId);
    
    /**
     * 获取用户知识库统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Result<Object> getKnowledgeStats(Integer userId);

    /**
     * 解析阅卷参考材料并存入阅卷知识库(knowledge_type='grading')
     */
    Result<String> parseAndStoreGradingMaterial(StudyMaterial studyMaterial, Integer taskId);

    /**
     * 检索阅卷相关知识（仅knowledge_type='grading'）
     */
    Result<List<String>> searchGradingKnowledge(Integer userId, String question, int topK);

    /**
     * 获取阅卷知识库统计信息
     */
    Result<Object> getGradingKnowledgeStats(Integer userId);

    /**
     * 删除用户的阅卷知识库数据
     */
    Result<String> deleteGradingKnowledge(Integer userId);

    Result<List<String>> searchAdminGradingKnowledge(String question, int topK);

    Result<Object> getAdminGradingKnowledgeStats();

    Result<String> deleteAdminGradingKnowledge();
}
