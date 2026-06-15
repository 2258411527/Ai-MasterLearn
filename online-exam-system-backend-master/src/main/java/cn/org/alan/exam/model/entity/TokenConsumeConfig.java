package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_token_consume_config")
public class TokenConsumeConfig {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("config_key")
    private String configKey;

    @TableField("config_name")
    private String configName;

    @TableField("token_cost")
    private Integer tokenCost;

    @TableField("description")
    private String description;

    @TableField("is_enabled")
    private Boolean isEnabled;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
