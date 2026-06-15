package cn.org.alan.exam.model.form.grading;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 教师阅卷表单
 *
 * @author AI Assistant
 * @since 2025-05-04
 */
@Data
public class TeacherGradingForm {
    
    @NotNull(message = "考试ID不能为空")
    private Integer examId;
    
    @NotNull(message = "用户ID不能为空")
    private Integer userId;
    
    @NotNull(message = "题目ID不能为空")
    private Integer questionId;
    
    @NotNull(message = "教师ID不能为空")
    private Integer teacherId;
    
    @NotNull(message = "教师评分不能为空")
    private Integer teacherScore;
    
    private String teacherComment;
}