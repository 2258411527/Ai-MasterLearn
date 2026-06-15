package cn.org.alan.exam.model.vo.token;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户Token余额VO")
public class UserTokenVO {

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("当前余额")
    private Integer balance;

    @ApiModelProperty("累计购买")
    private Integer totalPurchased;

    @ApiModelProperty("累计消耗")
    private Integer totalConsumed;
}
