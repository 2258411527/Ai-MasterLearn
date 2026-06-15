package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI定制学习系统 - 每日学习任务表实体类
 */
@Data
@TableName("t_daily_task")
public class DailyTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private LocalDate taskDate;
    private String subject;
    private String taskContent;
    private Integer estimatedMinutes;
    private Integer priority; // 1-高 2-中 3-低
    private Integer isCompleted; // 0-未完成 1-已完成
    private LocalDateTime completedTime;
    private String aiSuggestion;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
