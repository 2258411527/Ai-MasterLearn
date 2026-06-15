package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.service.IAIRecommendService;
import cn.org.alan.exam.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "AI个性化推荐")
@RestController
@RequestMapping("/api/ai-recommend")
public class AIRecommendController {

    @Autowired
    private IAIRecommendService aiRecommendService;

    @ApiOperation("获取个性化推荐")
    @GetMapping("/personalized")
    public Result<List<Map<String, Object>>> getPersonalizedRecommendations() {
        Integer userId = SecurityUtil.getUserId();
        return aiRecommendService.generatePersonalizedRecommendations(userId);
    }

    @ApiOperation("生成每日学习计划")
    @GetMapping("/daily-plan")
    public Result<String> getDailyStudyPlan() {
        Integer userId = SecurityUtil.getUserId();
        return aiRecommendService.generateDailyStudyPlan(userId);
    }

    @ApiOperation("分析薄弱点")
    @GetMapping("/weak-analysis")
    public Result<Map<String, Object>> analyzeWeakPoints() {
        Integer userId = SecurityUtil.getUserId();
        return aiRecommendService.analyzeWeakPoints(userId);
    }

    @ApiOperation("获取科目学习建议")
    @GetMapping("/advice/{subject}")
    public Result<String> getStudyAdvice(@PathVariable String subject) {
        Integer userId = SecurityUtil.getUserId();
        return aiRecommendService.getStudyAdvice(userId, subject);
    }
}
