package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_token_package")
public class TokenPackage {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("tokens")
    private Integer tokens;

    @TableField("price")
    private BigDecimal price;

    @TableField("discount")
    private BigDecimal discount;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("description")
    private String description;

    @TableField("is_active")
    private Boolean isActive;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
