package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.TeacherGrading;
import cn.org.alan.exam.model.form.grading.TeacherGradingForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 教师阅卷服务接口
 *
 * @author AI Assistant
 * @since 2025-05-04
 */
public interface ITeacherGradingService extends IService<TeacherGrading> {
    
    /**
     * 教师阅卷
     * @param form 阅卷表单
     * @return 阅卷结果
     */
    Result<Object> gradeByTeacher(TeacherGradingForm form);
    
    /**
     * 获取教师阅卷记录
     * @param examId 考试ID
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 阅卷记录
     */
    Result<TeacherGrading> getTeacherGradingRecord(Integer examId, Integer userId, Integer questionId);
    
    /**
     * 批量教师阅卷
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 批量阅卷结果
     */
    Result<Object> batchGradeByTeacher(Integer examId, Integer userId);
    
    /**
     * 获取需要教师阅卷的试卷列表
     * @param teacherId 教师ID
     * @return 试卷列表
     */
    Result<Object> getPendingGradingExams(Integer teacherId);
}