package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.ai.AiGradingForm;
import cn.org.alan.exam.model.vo.ai.AiGradingResultVO;
import cn.org.alan.exam.service.RealAiGradingService;
import cn.org.alan.exam.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 真实AI阅卷控制器
 * 提供独立的AI阅卷功能，学生可以选择任意试卷进行AI阅卷
 */
@RestController
@RequestMapping("/real-ai-grading")
@Slf4j
public class RealAiGradingController {
    
    @Autowired
    private RealAiGradingService realAiGradingService;
    
    /**
     * 真实AI阅卷单题
     */
    @PostMapping("/grade-single")
    public Result<AiGradingResultVO> gradeSingleQuestion(@RequestBody AiGradingForm form) {
        log.info("真实AI阅卷单题 - 考试ID: {}, 用户ID: {}, 题目ID: {}", 
                form.getExamId(), form.getUserId(), form.getQuestionId());
        return realAiGradingService.gradeWithRealAI(form);
    }
    
    /**
     * 批量真实AI阅卷
     */
    @PostMapping("/grade-batch/{examId}")
    public Result<Object> gradeBatchQuestions(@PathVariable Integer examId) {
        Integer userId = SecurityUtil.getUserId();
        log.info("批量真实AI阅卷 - 考试ID: {}, 用户ID: {}", examId, userId);
        return realAiGradingService.batchGradeWithRealAI(examId, userId);
    }
    
    /**
     * 获取阅卷进度
     */
    @GetMapping("/progress/{examId}")
    public Result<Object> getGradingProgress(@PathVariable Integer examId) {
        Integer userId = SecurityUtil.getUserId();
        log.info("获取阅卷进度 - 考试ID: {}, 用户ID: {}", examId, userId);
        return realAiGradingService.getGradingProgress(examId, userId);
    }
    
    /**
     * 获取可阅卷的试卷列表
     */
    @GetMapping("/gradable-exams")
    public Result<Object> getGradableExams() {
        Integer userId = SecurityUtil.getUserId();
        log.info("获取可阅卷试卷列表 - 用户ID: {}", userId);
        return realAiGradingService.getGradableExams(userId);
    }
    
    /**
     * 检查AI阅卷服务状态
     */
    @GetMapping("/status")
    public Result<Object> getServiceStatus() {
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("service", "Real AI Grading Service");
        status.put("status", "active");
        status.put("timestamp", System.currentTimeMillis());
        
        return Result.success(status);
    }
}