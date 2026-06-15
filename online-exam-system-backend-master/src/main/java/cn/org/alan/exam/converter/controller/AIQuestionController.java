package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.home.AIRecommendQuestionVO;
import cn.org.alan.exam.service.IAIQuestionRecommendService;
import cn.org.alan.exam.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "AI习题推荐")
@RestController
@RequestMapping("/api/ai-question")
public class AIQuestionController {

    @Autowired
    private IAIQuestionRecommendService questionRecommendService;

    @ApiOperation("获取AI推荐习题")
    @GetMapping("/recommend")
    public Result<List<AIRecommendQuestionVO>> getRecommendQuestions(
            @RequestParam(defaultValue = "4") Integer limit) {
        Integer userId = SecurityUtil.getUserId();
        return questionRecommendService.getRecommendQuestions(userId, limit);
    }

    @ApiOperation("根据知识点获取习题")
    @GetMapping("/by-knowledge")
    public Result<List<AIRecommendQuestionVO>> getQuestionsByKnowledgePoint(
            @RequestParam String knowledgePoint,
            @RequestParam(defaultValue = "4") Integer limit) {
        return questionRecommendService.getQuestionsByKnowledgePoint(knowledgePoint, limit);
    }

    @ApiOperation("获取薄弱点习题")
    @GetMapping("/weak-points")
    public Result<List<AIRecommendQuestionVO>> getQuestionsForWeakPoints(
            @RequestParam(defaultValue = "4") Integer limit) {
        Integer userId = SecurityUtil.getUserId();
        return questionRecommendService.getQuestionsForWeakPoints(userId, limit);
    }

    @ApiOperation("生成推荐原因")
    @GetMapping("/reason/{questionId}")
    public Result<String> generateRecommendReason(@PathVariable Integer questionId) {
        Integer userId = SecurityUtil.getUserId();
        return questionRecommendService.generateAIRecommendationReason(userId, questionId);
    }
}
