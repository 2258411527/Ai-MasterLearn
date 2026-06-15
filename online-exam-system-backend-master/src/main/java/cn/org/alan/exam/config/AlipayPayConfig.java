package cn.org.alan.exam.config;

import cn.org.alan.exam.model.entity.AlipayConfigEntity;
import cn.org.alan.exam.service.IAlipayConfigService;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayPayConfig {

    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String serverUrl;
    private String notifyUrl;
    private String returnUrl;
    private String signType;
    private String charset;
    private String format;

    @Autowired
    private IAlipayConfigService alipayConfigService;

    @Bean
    @Primary
    public AlipayClient alipayClient() {
        AlipayConfigEntity dbConfig = null;
        try {
            cn.org.alan.exam.common.result.Result<AlipayConfigEntity> result = alipayConfigService.getActiveConfig();
            if (result.getCode() == 1 && result.getData() != null) {
                dbConfig = result.getData();
            }
        } catch (Exception e) {
            log.warn("从数据库读取支付宝配置失败，使用yml配置: {}", e.getMessage());
        }

        String appId = dbConfig != null ? dbConfig.getAppId() : this.appId;
        String privateKey = dbConfig != null ? dbConfig.getPrivateKey() : this.privateKey;
        String alipayPublicKey = dbConfig != null ? dbConfig.getAlipayPublicKey() : this.alipayPublicKey;
        String serverUrl = dbConfig != null ? dbConfig.getServerUrl() : this.serverUrl;
        String signType = dbConfig != null ? dbConfig.getSignType() : this.signType;

        // 如果配置为空，返回null而不是抛出异常
        if (appId == null || appId.isEmpty() || 
            privateKey == null || privateKey.isEmpty() || 
            alipayPublicKey == null || alipayPublicKey.isEmpty()) {
            log.warn("支付宝配置未设置，支付宝功能将不可用。请在application.yml或数据库中配置支付宝参数。");
            return null;
        }

        com.alipay.api.AlipayConfig config = new com.alipay.api.AlipayConfig();
        config.setServerUrl(serverUrl);
        config.setAppId(appId);
        config.setPrivateKey(privateKey);
        config.setFormat(format);
        config.setAlipayPublicKey(alipayPublicKey);
        config.setCharset(charset);
        config.setSignType(signType);

        try {
            AlipayClient client = new DefaultAlipayClient(config);
            log.info("支付宝客户端初始化成功, appId: {}", appId);
            return client;
        } catch (Exception e) {
            log.error("初始化支付宝客户端失败: {}", e.getMessage());
            throw new RuntimeException("初始化支付宝客户端失败", e);
        }
    }

    public String getEffectiveNotifyUrl() {
        try {
            cn.org.alan.exam.common.result.Result<AlipayConfigEntity> result = alipayConfigService.getActiveConfig();
            if (result.getCode() == 1 && result.getData() != null && result.getData().getNotifyUrl() != null) {
                return result.getData().getNotifyUrl();
            }
        } catch (Exception ignored) {}
        return this.notifyUrl;
    }

    public String getEffectiveReturnUrl() {
        try {
            cn.org.alan.exam.common.result.Result<AlipayConfigEntity> result = alipayConfigService.getActiveConfig();
            if (result.getCode() == 1 && result.getData() != null && result.getData().getReturnUrl() != null) {
                return result.getData().getReturnUrl();
            }
        } catch (Exception ignored) {}
        return this.returnUrl;
    }

    public String getEffectiveSignType() {
        try {
            cn.org.alan.exam.common.result.Result<AlipayConfigEntity> result = alipayConfigService.getActiveConfig();
            if (result.getCode() == 1 && result.getData() != null && result.getData().getSignType() != null) {
                return result.getData().getSignType();
            }
        } catch (Exception ignored) {}
        return this.signType;
    }
}
