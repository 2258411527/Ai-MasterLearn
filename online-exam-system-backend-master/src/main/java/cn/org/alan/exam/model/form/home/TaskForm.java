package cn.org.alan.exam.model.form.home;

import lombok.Data;

/**
 * 添加任务表单
 */
@Data
public class TaskForm {
    
    /**
     * 科目
     */
    private String subject;
    
    /**
     * 任务内容
     */
    private String taskContent;
    
    /**
     * 预计时长（分钟）
     */
    private Integer estimatedMinutes;
    
    /**
     * 优先级（1-高 2-中 3-低）
     */
    private Integer priority;
}