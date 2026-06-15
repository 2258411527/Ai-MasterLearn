package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.user_questionnaire.QuestionnaireSubmitForm;
import cn.org.alan.exam.model.vo.user_questionnaire.UserStudyDashboardVO;

/**
 * 用户问卷服务接口
 */
public interface IUserQuestionnaireService {
    
    /**
     * 提交用户问卷
     */
    Result<String> submitQuestionnaire(QuestionnaireSubmitForm form);
    
    /**
     * 获取用户学习仪表盘数据
     */
    Result<UserStudyDashboardVO> getStudyDashboard(Integer userId);
    
    /**
     * 检查用户是否已完成问卷
     */
    Result<Boolean> checkQuestionnaireCompleted(Integer userId);
    
    /**
     * 生成AI个性化推荐
     */
    Result<String> generateAiRecommendations(Integer userId);
}