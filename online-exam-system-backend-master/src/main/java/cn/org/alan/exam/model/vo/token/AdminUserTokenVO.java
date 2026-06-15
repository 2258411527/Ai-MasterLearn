package cn.org.alan.exam.model.vo.token;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("管理员-用户Token余额VO")
public class AdminUserTokenVO {

    @ApiModelProperty("记录ID")
    private Integer id;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("真实姓名")
    private String realName;

    @ApiModelProperty("当前余额")
    private Integer balance;

    @ApiModelProperty("累计充值")
    private Integer totalPurchased;

    @ApiModelProperty("累计消耗")
    private Integer totalConsumed;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
