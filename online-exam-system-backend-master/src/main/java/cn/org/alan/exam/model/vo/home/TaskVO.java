package cn.org.alan.exam.model.vo.home;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务VO
 */
@Data
public class TaskVO {
    
    /**
     * 任务ID
     */
    private Integer id;
    
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
    
    /**
     * 优先级文本
     */
    private String priorityText;
    
    /**
     * 是否完成（0-未完成 1-已完成）
     */
    private Integer isCompleted;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
    
    /**
     * AI建议
     */
    private String aiSuggestion;
}