package cn.org.alan.exam.model.vo.token;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("Token套餐VO")
public class TokenPackageVO {

    @ApiModelProperty("套餐ID")
    private Integer id;

    @ApiModelProperty("套餐名称")
    private String name;

    @ApiModelProperty("Token数量")
    private Integer tokens;

    @ApiModelProperty("价格(元)")
    private BigDecimal price;

    @ApiModelProperty("折扣比例，如0.80表示8折")
    private BigDecimal discount;

    @ApiModelProperty("原价")
    private BigDecimal originalPrice;

    @ApiModelProperty("单价(Token/元)")
    private BigDecimal unitPrice;

    @ApiModelProperty("套餐描述")
    private String description;
}
