package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.TokenConsumeConfig;

import java.util.Map;

public interface ITokenConsumeConfigService {

    Result<Map<String, Integer>> getAllConfigs();

    Result<Integer> getTokenCost(String configKey);

    Result<TokenConsumeConfig> getConfigByKey(String configKey);

    Result<String> updateConfig(String configKey, Integer tokenCost);

    Result<String> updateConfig(TokenConsumeConfig config);

    Result<String> initDefaultConfigs();
}
