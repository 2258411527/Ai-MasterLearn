package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.vo.ai.AiGradingResultVO;
import cn.org.alan.exam.service.IExamQuAnswerService;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.service.RealAiGradingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI阅卷结果控制器
 * 学生可以在这里查看AI阅卷结果
 *
 * @author AI Assistant
 * @since 2025-05-04
 */
@RestController
@RequestMapping("/ai-grading-result")
@Api(tags = "AI阅卷结果管理")
@Slf4j
public class AiGradingResultController {
    
    @Autowired
    private IExamQuAnswerService examQuAnswerService;
    
    @Autowired
    private IQuestionService questionService;
    
    @Autowired
    private RealAiGradingService realAiGradingService;
    
    @GetMapping("/exam/{examId}/user/{userId}")
    @ApiOperation("获取考试的所有AI阅卷结果")
    public Result<Object> getExamAiGradingResults(
            @PathVariable Integer examId,
            @PathVariable Integer userId) {
        log.info("获取考试AI阅卷结果 - 考试ID: {}, 用户ID: {}", examId, userId);
        
        try {
            // 获取该考试用户的所有答题记录（包括客观题和简答题）
            LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamQuAnswer::getExamId, examId)
                   .eq(ExamQuAnswer::getUserId, userId);
            
            List<ExamQuAnswer> allAnswers = examQuAnswerService.list(wrapper);
            
            List<AiGradingResultVO> results = allAnswers.stream().map(answer -> {
                AiGradingResultVO result = new AiGradingResultVO();
                result.setQuestionId(answer.getQuestionId());
                result.setUserAnswer(answer.getAnswerContent());
                result.setAiScore(answer.getAiScore());
                result.setDetailedAnalysis(answer.getAiReason());
                
                // 获取题目类型信息
                Question question = questionService.getById(answer.getQuestionId());
                if (question != null) {
                    result.setQuestionType(question.getQuType());
                    result.setQuestionContent(question.getContent());
                }
                
                // 使用现有字段，改进建议和知识点暂时设置为null或使用aiReason
                result.setImprovementSuggestions(null);
                result.setKnowledgePoints(null);
                
                // 设置是否合格（简答题60分以上为合格，客观题根据isRight判断）
                if (answer.getAiScore() != null) {
                    result.setIsCorrect(answer.getAiScore() >= 60);
                } else if (answer.getIsRight() != null) {
                    result.setIsCorrect(answer.getIsRight() == 1);
                }
                
                return result;
            }).collect(Collectors.toList());
            
            return Result.success(results);
            
        } catch (Exception e) {
            log.error("获取考试AI阅卷结果异常", e);
            return Result.failed("获取AI阅卷结果失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/question/{questionId}/exam/{examId}/user/{userId}")
    @ApiOperation("获取单个题目的AI阅卷结果")
    public Result<Object> getQuestionAiGradingResult(
            @PathVariable Integer questionId,
            @PathVariable Integer examId,
            @PathVariable Integer userId) {
        log.info("获取题目AI阅卷结果 - 题目ID: {}, 考试ID: {}, 用户ID: {}", questionId, examId, userId);
        
        try {
            LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamQuAnswer::getExamId, examId)
                   .eq(ExamQuAnswer::getUserId, userId)
                   .eq(ExamQuAnswer::getQuestionId, questionId);
            
            ExamQuAnswer answer = examQuAnswerService.getOne(wrapper);
            
            if (answer == null) {
                return Result.failed("未找到答题记录");
            }
            
            AiGradingResultVO result = new AiGradingResultVO();
            result.setQuestionId(answer.getQuestionId());
            result.setUserAnswer(answer.getAnswerContent());
            result.setAiScore(answer.getAiScore());
            result.setDetailedAnalysis(answer.getAiReason());
            // 使用现有字段，改进建议和知识点暂时设置为null
            result.setImprovementSuggestions(null);
            result.setKnowledgePoints(null);
            
            // 设置是否合格（简答题60分以上为合格）
            if (answer.getAiScore() != null) {
                result.setIsCorrect(answer.getAiScore() >= 60);
            }
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("获取题目AI阅卷结果异常", e);
            return Result.failed("获取AI阅卷结果失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}/history")
    @ApiOperation("获取用户的AI阅卷历史")
    public Result<Object> getUserAiGradingHistory(@PathVariable Integer userId) {
        log.info("获取用户AI阅卷历史 - 用户ID: {}", userId);
        
        try {
            LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamQuAnswer::getUserId, userId)
                   .and(wq -> wq.isNotNull(ExamQuAnswer::getAiScore)
                           .or().isNotNull(ExamQuAnswer::getAiReason))
                   .orderByDesc(ExamQuAnswer::getId); // 使用ID排序（最新插入的记录）
            
            List<ExamQuAnswer> aiGradedAnswers = examQuAnswerService.list(wrapper);
            
            List<AiGradingResultVO> history = aiGradedAnswers.stream().map(answer -> {
                AiGradingResultVO result = new AiGradingResultVO();
                result.setQuestionId(answer.getQuestionId());
                result.setUserAnswer(answer.getAnswerContent());
                result.setAiScore(answer.getAiScore());
                result.setDetailedAnalysis(answer.getAiReason());
                // 使用现有字段，改进建议和知识点暂时设置为null
                result.setImprovementSuggestions(null);
                result.setKnowledgePoints(null);
                
                // 设置是否合格（简答题60分以上为合格）
                if (answer.getAiScore() != null) {
                    result.setIsCorrect(answer.getAiScore() >= 60);
                }
                
                return result;
            }).collect(Collectors.toList());
            
            return Result.success(history);
            
        } catch (Exception e) {
            log.error("获取用户AI阅卷历史异常", e);
            return Result.failed("获取AI阅卷历史失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/status/exam/{examId}/user/{userId}")
    @ApiOperation("获取考试AI阅卷状态")
    public Result<Object> getExamAiGradingStatus(
            @PathVariable Integer examId,
            @PathVariable Integer userId) {
        log.info("获取考试AI阅卷状态 - 考试ID: {}, 用户ID: {}", examId, userId);
        
        try {
            // 获取需要AI阅卷的题目数量
            LambdaQueryWrapper<ExamQuAnswer> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(ExamQuAnswer::getExamId, examId)
                       .eq(ExamQuAnswer::getUserId, userId)
                       .in(ExamQuAnswer::getQuestionType, 4, 5, 6); // 主观题类型
            
            long totalQuestions = examQuAnswerService.count(totalWrapper);
            
            // 获取已AI阅卷的题目数量
            LambdaQueryWrapper<ExamQuAnswer> gradedWrapper = new LambdaQueryWrapper<>();
            gradedWrapper.eq(ExamQuAnswer::getExamId, examId)
                        .eq(ExamQuAnswer::getUserId, userId)
                        .and(wq -> wq.isNotNull(ExamQuAnswer::getAiScore)
                                .or().isNotNull(ExamQuAnswer::getAiReason));
            
            long gradedQuestions = examQuAnswerService.count(gradedWrapper);
            
            // 构建状态响应
            AiGradingStatusVO status = new AiGradingStatusVO();
            status.setExamId(examId);
            status.setUserId(userId);
            status.setTotalQuestions((int) totalQuestions);
            status.setGradedQuestions((int) gradedQuestions);
            status.setProgress(totalQuestions > 0 ? (double) gradedQuestions / totalQuestions * 100 : 0);
            status.setCompleted(gradedQuestions == totalQuestions && totalQuestions > 0);
            
            return Result.success(status);
            
        } catch (Exception e) {
            log.error("获取考试AI阅卷状态异常", e);
            return Result.failed("获取AI阅卷状态失败: " + e.getMessage());
        }
    }
    
    /**
     * AI阅卷状态VO
     */
    public static class AiGradingStatusVO {
        private Integer examId;
        private Integer userId;
        private Integer totalQuestions;
        private Integer gradedQuestions;
        private Double progress;
        private Boolean completed;
        
        // getter和setter方法
        public Integer getExamId() { return examId; }
        public void setExamId(Integer examId) { this.examId = examId; }
        
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        
        public Integer getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
        
        public Integer getGradedQuestions() { return gradedQuestions; }
        public void setGradedQuestions(Integer gradedQuestions) { this.gradedQuestions = gradedQuestions; }
        
        public Double getProgress() { return progress; }
        public void setProgress(Double progress) { this.progress = progress; }
        
        public Boolean getCompleted() { return completed; }
        public void setCompleted(Boolean completed) { this.completed = completed; }
    }
}