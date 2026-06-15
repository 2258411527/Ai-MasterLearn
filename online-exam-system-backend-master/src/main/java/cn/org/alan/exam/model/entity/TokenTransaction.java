package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_token_transaction")
public class TokenTransaction {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    @TableField("type")
    private Integer type;

    @TableField("amount")
    private Integer amount;

    @TableField("balance_after")
    private Integer balanceAfter;

    @TableField("description")
    private String description;

    @TableField("ai_service_type")
    private String aiServiceType;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public static final int TYPE_PURCHASE = 1;
    public static final int TYPE_CONSUME = 2;
    public static final int TYPE_ADMIN_ADJUST = 3;
}
