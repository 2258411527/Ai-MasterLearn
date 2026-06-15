package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_token_order")
public class TokenOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Integer userId;

    @TableField("package_id")
    private Integer packageId;

    @TableField("package_name")
    private String packageName;

    @TableField("tokens")
    private Integer tokens;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("status")
    private Integer status;

    @TableField("trade_no")
    private String tradeNo;

    @TableField("pay_time")
    private LocalDateTime payTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_CLOSED = 2;
    public static final int STATUS_REFUND = 3;
}
