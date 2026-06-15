package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.AiGradingDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * AI阅卷结果详情服务接口
 *
 * @author AI Assistant
 * @since 2025-05-04
 */
public interface IAiGradingDetailService extends IService<AiGradingDetail> {
    
    /**
     * 保存AI阅卷详情
     * @param examId 考试ID
     * @param userId 用户ID
     * @param questionId 题目ID
     * @param aiScore AI评分
     * @param detailedAnalysis 详细分析
     * @param improvementSuggestions 改进建议
     * @param knowledgePoints 知识点分析
     * @param optionAnalysis 选项分析
     * @return 保存结果
     */
    Result<Object> saveAiGradingDetail(Integer examId, Integer userId, Integer questionId, 
                                      Integer aiScore, String detailedAnalysis, 
                                      String improvementSuggestions, String knowledgePoints, 
                                      String optionAnalysis);
    
    /**
     * 获取AI阅卷详情
     * @param examId 考试ID
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return AI阅卷详情
     */
    Result<AiGradingDetail> getAiGradingDetail(Integer examId, Integer userId, Integer questionId);
    
    /**
     * 获取用户的AI阅卷历史
     * @param userId 用户ID
     * @return AI阅卷历史
     */
    Result<Object> getAiGradingHistory(Integer userId);
    
    /**
     * 获取考试的所有AI阅卷结果
     * @param examId 考试ID
     * @param userId 用户ID
     * @return AI阅卷结果列表
     */
    Result<Object> getExamAiGradingResults(Integer examId, Integer userId);
}