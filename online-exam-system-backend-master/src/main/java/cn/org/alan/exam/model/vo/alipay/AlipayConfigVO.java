package cn.org.alan.exam.model.vo.alipay;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("支付宝配置VO")
public class AlipayConfigVO {

    @ApiModelProperty("配置ID")
    private Integer id;

    @ApiModelProperty("配置名称")
    private String configName;

    @ApiModelProperty("应用AppId")
    private String appId;

    @ApiModelProperty("应用私钥(脱敏)")
    private String privateKeyMasked;

    @ApiModelProperty("支付宝公钥(脱敏)")
    private String alipayPublicKeyMasked;

    @ApiModelProperty("支付宝网关地址")
    private String serverUrl;

    @ApiModelProperty("异步通知地址")
    private String notifyUrl;

    @ApiModelProperty("同步跳转地址")
    private String returnUrl;

    @ApiModelProperty("签名类型")
    private String signType;

    @ApiModelProperty("测试模式")
    private Boolean testMode;

    @ApiModelProperty("是否激活")
    private Boolean isActive;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
