package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.grading.TeacherGradingForm;
import cn.org.alan.exam.service.ITeacherGradingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 教师阅卷控制器
 *
 * @author AI Assistant
 * @since 2025-05-04
 */
@RestController
@RequestMapping("/teacher-grading")
@Api(tags = "教师阅卷管理")
@Slf4j
public class TeacherGradingController {
    
    @Autowired
    private ITeacherGradingService teacherGradingService;
    
    @PostMapping("/grade")
    @ApiOperation("教师阅卷")
    public Result<Object> gradeByTeacher(@Valid @RequestBody TeacherGradingForm form) {
        log.info("教师阅卷请求 - 考试ID: {}, 用户ID: {}, 题目ID: {}, 教师ID: {}", 
                form.getExamId(), form.getUserId(), form.getQuestionId(), form.getTeacherId());
        
        return teacherGradingService.gradeByTeacher(form);
    }
    
    @GetMapping("/record")
    @ApiOperation("获取教师阅卷记录")
    public Result<?> getTeacherGradingRecord(
            @RequestParam Integer examId,
            @RequestParam Integer userId,
            @RequestParam Integer questionId) {
        log.info("获取教师阅卷记录 - 考试ID: {}, 用户ID: {}, 题目ID: {}", examId, userId, questionId);
        
        return teacherGradingService.getTeacherGradingRecord(examId, userId, questionId);
    }
    
    @PostMapping("/batch-grade/{examId}/{userId}")
    @ApiOperation("批量教师阅卷")
    public Result<Object> batchGradeByTeacher(
            @PathVariable Integer examId,
            @PathVariable Integer userId) {
        log.info("批量教师阅卷 - 考试ID: {}, 用户ID: {}", examId, userId);
        
        return teacherGradingService.batchGradeByTeacher(examId, userId);
    }
    
    @GetMapping("/pending-exams/{teacherId}")
    @ApiOperation("获取待阅卷试卷列表")
    public Result<Object> getPendingGradingExams(@PathVariable Integer teacherId) {
        log.info("获取待阅卷试卷列表 - 教师ID: {}", teacherId);
        
        return teacherGradingService.getPendingGradingExams(teacherId);
    }
}