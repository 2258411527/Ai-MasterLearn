package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.AiConfigMapper;
import cn.org.alan.exam.model.entity.AiConfig;
import cn.org.alan.exam.model.form.ai.AiConfigForm;
import cn.org.alan.exam.model.vo.ai.AiConfigVO;
import cn.org.alan.exam.service.IAiConfigService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI配置服务实现类
 *
 * @author AI Assistant
 * @since 2024
 */
@Service
@Slf4j
public class AiConfigServiceImpl extends ServiceImpl<AiConfigMapper, AiConfig> implements IAiConfigService {

    @Resource
    private AiConfigMapper aiConfigMapper;

    @Override
    public Result<IPage<AiConfigVO>> getAiConfigPage(Integer pageNum, Integer pageSize, String configName) {
        try {
            Page<AiConfigVO> page = new Page<>(pageNum, pageSize);
            IPage<AiConfigVO> result = aiConfigMapper.selectAiConfigPage(page, configName);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询AI配置列表失败", e);
            return Result.failed("查询失败");
        }
    }

    @Override
    @Transactional
    public Result<String> addAiConfig(AiConfigForm aiConfigForm) {
        try {
            String configName = aiConfigForm.getConfigName();

            // 用原生SQL检查是否存在未删除的同名配置（绕过@TableLogic）
            int activeCount = aiConfigMapper.countActiveByName(configName);
            if (activeCount > 0) {
                return Result.failed("配置名称已存在");
            }

            // 物理删除所有同名配置（包括已软删除的），释放唯一约束
            int deleted = aiConfigMapper.physicalDeleteByName(configName);
            if (deleted > 0) {
                log.info("物理删除同名AI配置：name={}, 删除{}条", configName, deleted);
            }

            AiConfig aiConfig = new AiConfig();
            aiConfig.setConfigName(configName);
            aiConfig.setApiKey(aiConfigForm.getApiKey());
            aiConfig.setBaseUrl(aiConfigForm.getBaseUrl());
            aiConfig.setModel(aiConfigForm.getModel());
            aiConfig.setIsActive(Boolean.TRUE.equals(aiConfigForm.getIsActive()));

            try {
                aiConfig.setCreateUserId(SecurityUtil.getUserId());
            } catch (Exception e) {
                log.warn("获取当前用户ID失败，使用默认值: {}", e.getMessage());
            }

            boolean success = save(aiConfig);
            if (success) {
                log.info("添加AI配置成功：{}", configName);
                return Result.success("添加成功");
            } else {
                return Result.failed("添加失败");
            }
        } catch (Exception e) {
            log.error("添加AI配置异常: {}", e.getMessage(), e);
            return Result.failed("添加失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> updateAiConfig(Integer id, AiConfigForm aiConfigForm) {
        try {
            AiConfig existingConfig = getById(id);
            if (existingConfig == null || Boolean.TRUE.equals(existingConfig.getIsDeleted())) {
                return Result.failed("配置不存在");
            }

            Long count = lambdaQuery()
                    .eq(AiConfig::getConfigName, aiConfigForm.getConfigName())
                    .ne(AiConfig::getId, id)
                    .eq(AiConfig::getIsDeleted, false)
                    .count();
            
            if (count > 0) {
                return Result.failed("配置名称已存在");
            }

            AiConfig aiConfig = new AiConfig();
            aiConfig.setId(id);
            aiConfig.setConfigName(aiConfigForm.getConfigName());
            aiConfig.setApiKey(aiConfigForm.getApiKey());
            aiConfig.setBaseUrl(aiConfigForm.getBaseUrl());
            aiConfig.setModel(aiConfigForm.getModel());

            try {
                aiConfig.setUpdateUserId(SecurityUtil.getUserId());
            } catch (Exception e) {
                log.warn("获取当前用户ID失败: {}", e.getMessage());
            }

            aiConfig.setIsActive(aiConfigForm.getIsActive());

            boolean success = updateById(aiConfig);
            if (success) {
                log.info("修改AI配置成功：{}", aiConfigForm.getConfigName());
                return Result.success("修改成功");
            } else {
                return Result.failed("修改失败");
            }
        } catch (Exception e) {
            log.error("修改AI配置异常: {}", e.getMessage(), e);
            return Result.failed("修改失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> deleteAiConfig(Integer id) {
        try {
            AiConfig existingConfig = getById(id);
            if (existingConfig == null || Boolean.TRUE.equals(existingConfig.getIsDeleted())) {
                return Result.failed("配置不存在");
            }

            boolean success = removeById(id);
            if (success) {
                log.info("删除AI配置成功，ID：{}", id);
                return Result.success("删除成功");
            } else {
                return Result.failed("删除失败");
            }
        } catch (Exception e) {
            log.error("删除AI配置失败", e);
            return Result.failed("删除失败");
        }
    }

    @Override
    @Transactional
    public Result<String> activateAiConfig(Integer id) {
        try {
            AiConfig existingConfig = getById(id);
            if (existingConfig == null || Boolean.TRUE.equals(existingConfig.getIsDeleted())) {
                return Result.failed("配置不存在");
            }

            AiConfig aiConfig = new AiConfig();
            aiConfig.setId(id);

            if (Boolean.TRUE.equals(existingConfig.getIsActive())) {
                aiConfig.setIsActive(false);
                try {
                    aiConfig.setUpdateUserId(SecurityUtil.getUserId());
                } catch (Exception e) {
                    log.warn("获取当前用户ID失败: {}", e.getMessage());
                }
                updateById(aiConfig);
                log.info("取消激活AI配置，ID：{}", id);
                return Result.success("已取消激活");
            } else {
                aiConfig.setIsActive(true);
                try {
                    aiConfig.setUpdateUserId(SecurityUtil.getUserId());
                } catch (Exception e) {
                    log.warn("获取当前用户ID失败: {}", e.getMessage());
                }
                updateById(aiConfig);
                log.info("激活AI配置成功，ID：{}", id);
                return Result.success("激活成功");
            }
        } catch (Exception e) {
            log.error("切换AI配置激活状态失败", e);
            return Result.failed("操作失败");
        }
    }

    @Override
    public Result<AiConfig> getActiveConfig() {
        try {
            List<AiConfig> activeConfigs = lambdaQuery()
                    .eq(AiConfig::getIsDeleted, false)
                    .eq(AiConfig::getIsActive, true)
                    .orderByDesc(AiConfig::getUpdateTime)
                    .list();
            if (activeConfigs == null || activeConfigs.isEmpty()) {
                return Result.failed("没有激活的AI配置");
            }
            return Result.success("查询成功", activeConfigs.get(0));
        } catch (Exception e) {
            log.error("获取激活的AI配置失败", e);
            return Result.failed("获取配置失败");
        }
    }

    @Override
    public Result<List<AiConfig>> getActiveConfigs() {
        try {
            List<AiConfig> activeConfigs = lambdaQuery()
                    .eq(AiConfig::getIsDeleted, false)
                    .eq(AiConfig::getIsActive, true)
                    .orderByDesc(AiConfig::getUpdateTime)
                    .list();
            return Result.success("查询成功", activeConfigs);
        } catch (Exception e) {
            log.error("获取激活的AI配置列表失败", e);
            return Result.failed("获取配置失败");
        }
    }

    @Override
    public Result<List<Map<String, Object>>> getAvailableModels() {
        try {
            List<AiConfig> configs = lambdaQuery()
                    .eq(AiConfig::getIsDeleted, false)
                    .eq(AiConfig::getIsActive, true)
                    .orderByDesc(AiConfig::getUpdateTime)
                    .list();
            List<Map<String, Object>> models = new ArrayList<>();
            for (AiConfig config : configs) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", config.getId());
                item.put("configName", config.getConfigName());
                item.put("model", config.getModel());
                item.put("isActive", config.getIsActive());
                models.add(item);
            }
            return Result.success("查询成功", models);
        } catch (Exception e) {
            log.error("获取可用模型列表失败", e);
            return Result.failed("获取模型列表失败");
        }
    }

    @Override
    public Result<AiConfig> getConfigById(Integer id) {
        try {
            AiConfig config = getById(id);
            if (config == null || Boolean.TRUE.equals(config.getIsDeleted())) {
                return Result.failed("配置不存在");
            }
            return Result.success("查询成功", config);
        } catch (Exception e) {
            log.error("根据ID获取AI配置失败", e);
            return Result.failed("获取配置失败");
        }
    }

    @Override
    public Result<AiConfig> resolveConfig(Integer configId) {
        log.info("resolveConfig 被调用, configId={}", configId);
        if (configId != null && configId > 0) {
            Result<AiConfig> result = getConfigById(configId);
            if (result.getCode() == 1 && result.getData() != null) {
                AiConfig config = result.getData();
                if (Boolean.TRUE.equals(config.getIsActive())) {
                    log.info("resolveConfig 使用指定配置: id={}, name={}, model={}, isActive={}",
                            config.getId(), config.getConfigName(), config.getModel(), config.getIsActive());
                    return result;
                }
                log.warn("指定的AI配置ID={}存在但未激活(isActive={})，回退到激活配置", configId, config.getIsActive());
            } else {
                log.warn("指定的AI配置ID={}不存在或已删除，回退到激活配置", configId);
            }
        }
        Result<AiConfig> activeResult = getActiveConfig();
        if (activeResult.getCode() == 1 && activeResult.getData() != null) {
            log.info("resolveConfig 使用激活配置(回退): id={}, name={}, model={}",
                    activeResult.getData().getId(), activeResult.getData().getConfigName(), activeResult.getData().getModel());
        } else {
            log.warn("resolveConfig 没有找到任何激活的AI配置");
        }
        return activeResult;
    }
}