package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.ai.AiGradingForm;
import cn.org.alan.exam.model.vo.ai.AiGradingResultVO;

/**
 * AI阅卷服务接口
 */
public interface IAiGradingService {
    
    /**
     * AI阅卷单个题目
     * @param form 阅卷表单
     * @return 阅卷结果
     */
    Result<AiGradingResultVO> gradeSingleQuestion(AiGradingForm form);
    
    /**
     * AI阅卷整张试卷
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 阅卷结果列表
     */
    Result<Object> gradeEntireExam(Integer examId, Integer userId);
    
    /**
     * AI题目解析（学生端使用，仅分析不保存分数）
     * @param form 阅卷表单
     * @return 分析结果
     */
    Result<AiGradingResultVO> analyzeQuestion(AiGradingForm form);

    /**
     * 获取AI阅卷历史记录
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 历史记录
     */
    Result<Object> getAiGradingHistory(Integer examId, Integer userId);
}