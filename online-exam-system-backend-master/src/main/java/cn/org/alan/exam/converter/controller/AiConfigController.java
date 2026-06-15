package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.AiConfig;
import cn.org.alan.exam.model.form.ai.AiConfigForm;
import cn.org.alan.exam.model.vo.ai.AiConfigVO;
import cn.org.alan.exam.service.IAiConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * AI配置控制器
 *
 * @author AI Assistant
 * @since 2024
 */
@Api(tags = "AI配置管理相关接口")
@RestController
@RequestMapping("/ai-config")
public class AiConfigController {

    @Resource
    private IAiConfigService aiConfigService;

    /**
     * 分页查询AI配置
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param configName 配置名称
     * @return 分页结果
     */
    @ApiOperation("分页查询AI配置")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<IPage<AiConfigVO>> getAiConfigPage(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "configName", required = false) String configName) {
        return aiConfigService.getAiConfigPage(pageNum, pageSize, configName);
    }

    /**
     * 添加AI配置
     *
     * @param aiConfigForm AI配置表单
     * @return 添加结果
     */
    @ApiOperation("添加AI配置")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> addAiConfig(@RequestBody @Validated AiConfigForm aiConfigForm) {
        return aiConfigService.addAiConfig(aiConfigForm);
    }

    /**
     * 修改AI配置
     *
     * @param id AI配置ID
     * @param aiConfigForm AI配置表单
     * @return 修改结果
     */
    @ApiOperation("修改AI配置")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> updateAiConfig(@PathVariable("id") Integer id,
                                         @RequestBody @Validated AiConfigForm aiConfigForm) {
        return aiConfigService.updateAiConfig(id, aiConfigForm);
    }

    /**
     * 删除AI配置
     *
     * @param id AI配置ID
     * @return 删除结果
     */
    @ApiOperation("删除AI配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> deleteAiConfig(@PathVariable("id") Integer id) {
        return aiConfigService.deleteAiConfig(id);
    }

    /**
     * 激活AI配置
     *
     * @param id AI配置ID
     * @return 激活结果
     */
    @ApiOperation("激活AI配置")
    @PutMapping("/activate/{id}")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> activateAiConfig(@PathVariable("id") Integer id) {
        return aiConfigService.activateAiConfig(id);
    }

    /**
     * 获取当前激活的AI配置
     *
     * @return 激活的配置
     */
    @ApiOperation("获取当前激活的AI配置")
    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<List<AiConfig>> getActiveConfigs() {
        return aiConfigService.getActiveConfigs();
    }

    @ApiOperation("获取可用AI模型列表（所有角色可访问，不含API密钥）")
    @GetMapping("/models")
    public Result<List<Map<String, Object>>> getAvailableModels() {
        return aiConfigService.getAvailableModels();
    }

    @ApiOperation("根据ID获取AI配置")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<AiConfig> getConfigById(@PathVariable("id") Integer id) {
        return aiConfigService.getConfigById(id);
    }
}