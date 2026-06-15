package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.TokenConsumeConfigMapper;
import cn.org.alan.exam.model.entity.TokenConsumeConfig;
import cn.org.alan.exam.service.ITokenConsumeConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "Token消耗配置管理")
@RestController
@RequestMapping("/api/token-consume-config")
public class TokenConsumeConfigController {

    @Resource
    private ITokenConsumeConfigService configService;

    @Resource
    private TokenConsumeConfigMapper configMapper;

    @ApiOperation("获取所有配置")
    @GetMapping("/list")
    public Result<List<TokenConsumeConfig>> list() {
        List<TokenConsumeConfig> configs = configMapper.selectList(null);
        return Result.success(configs);
    }

    @ApiOperation("获取配置Map")
    @GetMapping("/map")
    public Result<Map<String, Integer>> getConfigMap() {
        return configService.getAllConfigs();
    }

    @ApiOperation("根据key获取配置")
    @GetMapping("/get/{key}")
    public Result<TokenConsumeConfig> getByKey(@PathVariable String key) {
        return configService.getConfigByKey(key);
    }

    @ApiOperation("更新配置")
    @PutMapping("/update")
    public Result<String> update(@RequestBody TokenConsumeConfig config) {
        return configService.updateConfig(config);
    }

    @ApiOperation("更新单个配置值")
    @PutMapping("/update/{key}/{cost}")
    public Result<String> updateCost(@PathVariable String key, @PathVariable Integer cost) {
        return configService.updateConfig(key, cost);
    }

    @ApiOperation("初始化默认配置")
    @PostMapping("/init")
    public Result<String> initDefaults() {
        return configService.initDefaultConfigs();
    }

    @ApiOperation("启用/禁用配置")
    @PutMapping("/toggle/{id}")
    public Result<String> toggle(@PathVariable Integer id) {
        TokenConsumeConfig config = configMapper.selectById(id);
        if (config == null) {
            return Result.failed("配置不存在");
        }
        config.setIsEnabled(!config.getIsEnabled());
        configMapper.updateById(config);
        return Result.success(config.getIsEnabled() ? "已启用" : "已禁用");
    }
}
