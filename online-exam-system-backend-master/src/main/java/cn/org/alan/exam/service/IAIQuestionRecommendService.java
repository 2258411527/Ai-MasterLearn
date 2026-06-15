package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.home.AIRecommendQuestionVO;

import java.util.List;

public interface IAIQuestionRecommendService {
    
    Result<List<AIRecommendQuestionVO>> getRecommendQuestions(Integer userId, Integer limit);
    
    Result<List<AIRecommendQuestionVO>> getQuestionsByKnowledgePoint(String knowledgePoint, Integer limit);
    
    Result<List<AIRecommendQuestionVO>> getQuestionsForWeakPoints(Integer userId, Integer limit);
    
    Result<String> generateAIRecommendationReason(Integer userId, Integer questionId);
}
