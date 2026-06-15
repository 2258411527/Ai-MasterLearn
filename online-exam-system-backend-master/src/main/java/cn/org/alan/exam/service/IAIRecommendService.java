package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import java.util.List;
import java.util.Map;

public interface IAIRecommendService {
    
    Result<List<Map<String, Object>>> generatePersonalizedRecommendations(Integer userId);
    
    Result<String> generateDailyStudyPlan(Integer userId);
    
    Result<Map<String, Object>> analyzeWeakPoints(Integer userId);
    
    Result<String> getStudyAdvice(Integer userId, String subject);
}
