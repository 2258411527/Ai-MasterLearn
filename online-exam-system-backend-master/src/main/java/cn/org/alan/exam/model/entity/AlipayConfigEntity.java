package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_alipay_config")
public class AlipayConfigEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("config_name")
    private String configName;

    @TableField("app_id")
    private String appId;

    @TableField("private_key")
    private String privateKey;

    @TableField("alipay_public_key")
    private String alipayPublicKey;

    @TableField("server_url")
    private String serverUrl;

    @TableField("notify_url")
    private String notifyUrl;

    @TableField("return_url")
    private String returnUrl;

    @TableField("sign_type")
    private String signType;

    @TableField("test_mode")
    private Boolean testMode;

    @TableField("is_active")
    private Boolean isActive;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;
}
