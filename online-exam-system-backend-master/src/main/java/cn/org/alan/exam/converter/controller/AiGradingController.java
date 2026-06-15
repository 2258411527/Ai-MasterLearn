package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.annotation.RequireToken;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.ai.AiGradingForm;
import cn.org.alan.exam.model.vo.ai.AiGradingResultVO;
import cn.org.alan.exam.service.IAiGradingService;
import cn.org.alan.exam.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * AI阅卷控制器
 */
@RestController
@RequestMapping("/ai-grading")
@Api(tags = "AI阅卷管理")
@Slf4j
public class AiGradingController {

    @Autowired
    private IAiGradingService aiGradingService;

    @PostMapping("/grade-single")
    @ApiOperation("AI阅卷单个题目（教师/管理员）")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<AiGradingResultVO> gradeSingleQuestion(@Validated @RequestBody AiGradingForm form) {
        if (form.getUserId() == null) {
            form.setUserId(SecurityUtil.getUserId());
        }
        log.info("AI阅卷单题 - examId:{}, userId:{}, questionId:{}, configId:{}",
                form.getExamId(), form.getUserId(), form.getQuestionId(), form.getConfigId());
        return aiGradingService.gradeSingleQuestion(form);
    }

    @PostMapping("/analyze-question")
    @ApiOperation("AI解析题目（学生端，仅分析不保存分数）")
    @RequireToken(value = 2, serviceType = "analyze")
    public Result<AiGradingResultVO> analyzeQuestion(@Validated @RequestBody AiGradingForm form) {
        if (form.getUserId() == null) {
            form.setUserId(SecurityUtil.getUserId());
        }
        log.info("AI解析题目 - examId:{}, userId:{}, questionId:{}, configId:{}",
                form.getExamId(), form.getUserId(), form.getQuestionId(), form.getConfigId());
        return aiGradingService.analyzeQuestion(form);
    }

    @GetMapping("/grade-exam/{examId}")
    @ApiOperation("AI阅卷整张试卷（教师/管理员）")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<Object> gradeEntireExam(@PathVariable Integer examId) {
        return aiGradingService.gradeEntireExam(examId, SecurityUtil.getUserId());
    }

    @GetMapping("/history/{examId}")
    @ApiOperation("获取AI阅卷历史记录")
    public Result<Object> getAiGradingHistory(@PathVariable Integer examId) {
        return aiGradingService.getAiGradingHistory(examId, SecurityUtil.getUserId());
    }
}