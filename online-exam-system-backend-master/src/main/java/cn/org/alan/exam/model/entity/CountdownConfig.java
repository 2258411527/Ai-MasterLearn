package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI定制学习系统 - 倒计时配置表实体类
 */
@Data
@TableName("t_countdown_config")
public class CountdownConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private String eventName;
    private LocalDate targetDate;
    private String milestones; // JSON数组
    private String aiAnalysis;
    private Integer isActive; // 0-停用 1-激活

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
