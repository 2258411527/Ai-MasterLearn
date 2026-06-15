package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.TokenConsumeConfigMapper;
import cn.org.alan.exam.model.entity.TokenConsumeConfig;
import cn.org.alan.exam.service.ITokenConsumeConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TokenConsumeConfigServiceImpl implements ITokenConsumeConfigService {

    @Resource
    private TokenConsumeConfigMapper configMapper;

    @Override
    public Result<Map<String, Integer>> getAllConfigs() {
        LambdaQueryWrapper<TokenConsumeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TokenConsumeConfig::getIsEnabled, true);
        List<TokenConsumeConfig> configs = configMapper.selectList(wrapper);
        
        Map<String, Integer> configMap = new HashMap<>();
        for (TokenConsumeConfig config : configs) {
            configMap.put(config.getConfigKey(), config.getTokenCost());
        }
        return Result.success(configMap);
    }

    @Override
    public Result<Integer> getTokenCost(String configKey) {
        LambdaQueryWrapper<TokenConsumeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TokenConsumeConfig::getConfigKey, configKey)
               .eq(TokenConsumeConfig::getIsEnabled, true);
        TokenConsumeConfig config = configMapper.selectOne(wrapper);
        
        if (config == null) {
            return Result.failed("配置项不存在或已禁用");
        }
        return Result.success(config.getTokenCost());
    }

    @Override
    public Result<TokenConsumeConfig> getConfigByKey(String configKey) {
        LambdaQueryWrapper<TokenConsumeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TokenConsumeConfig::getConfigKey, configKey);
        TokenConsumeConfig config = configMapper.selectOne(wrapper);
        
        if (config == null) {
            return Result.failed("配置项不存在");
        }
        return Result.success(config);
    }

    @Override
    public Result<String> updateConfig(String configKey, Integer tokenCost) {
        if (tokenCost == null || tokenCost < 0) {
            return Result.failed("token消耗值必须大于等于0");
        }
        
        LambdaUpdateWrapper<TokenConsumeConfig> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TokenConsumeConfig::getConfigKey, configKey)
               .set(TokenConsumeConfig::getTokenCost, tokenCost);
        
        int rows = configMapper.update(null, wrapper);
        if (rows > 0) {
            return Result.success("更新成功");
        }
        return Result.failed("更新失败，配置项不存在");
    }

    @Override
    public Result<String> updateConfig(TokenConsumeConfig config) {
        if (config.getId() == null) {
            return Result.failed("配置ID不能为空");
        }
        
        int rows = configMapper.updateById(config);
        if (rows > 0) {
            return Result.success("更新成功");
        }
        return Result.failed("更新失败");
    }

    @Override
    public Result<String> initDefaultConfigs() {
        LambdaQueryWrapper<TokenConsumeConfig> wrapper = new LambdaQueryWrapper<>();
        Long count = configMapper.selectCount(wrapper);
        
        if (count > 0) {
            return Result.success("配置已存在，无需初始化");
        }
        
        TokenConsumeConfig[] defaultConfigs = {
            createConfig("ai_chat", "AI对话(每次)", 1, "每次AI对话消耗的token数"),
            createConfig("ai_chat_per_100_chars", "AI对话(每100字)", 1, "AI对话按输入字数计算，每100字消耗"),
            createConfig("ai_grading", "AI阅卷(每题)", 2, "AI自动阅卷每题消耗的token数"),
            createConfig("ai_analysis", "学习分析报告", 5, "生成AI学习分析报告消耗的token数"),
            createConfig("ai_recommend", "个性化推荐", 3, "生成AI个性化推荐消耗的token数"),
            createConfig("question_generate", "AI生成题目", 5, "AI生成练习题消耗的token数"),
            createConfig("knowledge_qa", "知识点问答", 2, "知识点智能问答消耗的token数")
        };
        
        for (TokenConsumeConfig config : defaultConfigs) {
            configMapper.insert(config);
        }
        
        return Result.success("初始化默认配置成功");
    }
    
    private TokenConsumeConfig createConfig(String key, String name, Integer cost, String desc) {
        TokenConsumeConfig config = new TokenConsumeConfig();
        config.setConfigKey(key);
        config.setConfigName(name);
        config.setTokenCost(cost);
        config.setDescription(desc);
        config.setIsEnabled(true);
        return config;
    }
}
