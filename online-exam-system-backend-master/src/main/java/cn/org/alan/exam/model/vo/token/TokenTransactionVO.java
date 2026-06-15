package cn.org.alan.exam.model.vo.token;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("Token交易记录VO")
public class TokenTransactionVO {

    @ApiModelProperty("记录ID")
    private Integer id;

    @ApiModelProperty("交易类型: 1-充值 2-消费 3-管理员调整")
    private Integer type;

    @ApiModelProperty("交易类型名称")
    private String typeName;

    @ApiModelProperty("变动数量")
    private Integer amount;

    @ApiModelProperty("变动后余额")
    private Integer balanceAfter;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("AI服务类型")
    private String aiServiceType;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
