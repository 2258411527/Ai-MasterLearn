package cn.org.alan.exam.model.form.alipay;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("支付宝配置表单")
public class AlipayConfigForm {

    @NotBlank(message = "配置名称不能为空")
    @ApiModelProperty("配置名称")
    private String configName;

    @NotBlank(message = "AppId不能为空")
    @ApiModelProperty("应用AppId")
    private String appId;

    @NotBlank(message = "应用私钥不能为空")
    @ApiModelProperty("应用私钥")
    private String privateKey;

    @NotBlank(message = "支付宝公钥不能为空")
    @ApiModelProperty("支付宝公钥")
    private String alipayPublicKey;

    @ApiModelProperty("支付宝网关地址")
    private String serverUrl;

    @ApiModelProperty("异步通知地址")
    private String notifyUrl;

    @ApiModelProperty("同步跳转地址")
    private String returnUrl;

    @ApiModelProperty("签名类型")
    private String signType;

    @ApiModelProperty("测试模式: true-模拟支付 false-真实支付")
    private Boolean testMode;
}
