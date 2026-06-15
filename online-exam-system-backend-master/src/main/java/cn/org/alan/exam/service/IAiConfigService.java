package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.AiConfig;
import cn.org.alan.exam.model.form.ai.AiConfigForm;
import cn.org.alan.exam.model.vo.ai.AiConfigVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * AI配置服务接口
 *
 * @author AI Assistant
 * @since 2024
 */
public interface IAiConfigService extends IService<AiConfig> {
    
    /**
     * 分页查询AI配置
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param configName 配置名称
     * @return 分页结果
     */
    Result<IPage<AiConfigVO>> getAiConfigPage(Integer pageNum, Integer pageSize, String configName);
    
    /**
     * 添加AI配置
     *
     * @param aiConfigForm AI配置表单
     * @return 添加结果
     */
    Result<String> addAiConfig(AiConfigForm aiConfigForm);
    
    /**
     * 修改AI配置
     *
     * @param id AI配置ID
     * @param aiConfigForm AI配置表单
     * @return 修改结果
     */
    Result<String> updateAiConfig(Integer id, AiConfigForm aiConfigForm);
    
    /**
     * 删除AI配置
     *
     * @param id AI配置ID
     * @return 删除结果
     */
    Result<String> deleteAiConfig(Integer id);
    
    /**
     * 激活AI配置
     *
     * @param id AI配置ID
     * @return 激活结果
     */
    Result<String> activateAiConfig(Integer id);
    
    /**
     * 获取当前激活的AI配置
     *
     * @return 激活的配置
     */
    Result<AiConfig> getActiveConfig();

    Result<List<AiConfig>> getActiveConfigs();

    /**
     * 获取所有可用的AI配置列表（不含API密钥，供学生选择模型）
     */
    Result<List<Map<String, Object>>> getAvailableModels();

    /**
     * 根据ID获取配置
     */
    Result<AiConfig> getConfigById(Integer id);

    /**
     * 获取AI配置：优先使用指定configId，否则使用激活配置
     */
    Result<AiConfig> resolveConfig(Integer configId);
}