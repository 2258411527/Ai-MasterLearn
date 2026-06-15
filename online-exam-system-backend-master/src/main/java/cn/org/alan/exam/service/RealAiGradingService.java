package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.ai.AiGradingForm;
import cn.org.alan.exam.model.vo.ai.AiGradingResultVO;

/**
 * 真实AI阅卷服务接口
 * 集成真实AI API进行智能评分
 */
public interface RealAiGradingService {
    
    /**
     * 调用真实AI API进行单题评分
     * @param form 阅卷表单
     * @return AI评分结果
     */
    Result<AiGradingResultVO> gradeWithRealAI(AiGradingForm form);
    
    /**
     * 批量AI阅卷
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 批量阅卷结果
     */
    Result<Object> batchGradeWithRealAI(Integer examId, Integer userId);
    
    /**
     * 获取阅卷进度
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 阅卷进度信息
     */
    Result<Object> getGradingProgress(Integer examId, Integer userId);
    
    /**
     * 获取可阅卷的试卷列表
     * @param userId 用户ID
     * @return 试卷列表
     */
    Result<Object> getGradableExams(Integer userId);
}