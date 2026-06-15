package cn.org.alan.exam.model.vo.token;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("Token订单VO")
public class TokenOrderVO {

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("套餐名称")
    private String packageName;

    @ApiModelProperty("Token数量")
    private Integer tokens;

    @ApiModelProperty("支付金额")
    private BigDecimal amount;

    @ApiModelProperty("订单状态: 0-待支付 1-已支付 2-已关闭 3-已退款")
    private Integer status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("支付宝交易号")
    private String tradeNo;

    @ApiModelProperty("支付时间")
    private LocalDateTime payTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
