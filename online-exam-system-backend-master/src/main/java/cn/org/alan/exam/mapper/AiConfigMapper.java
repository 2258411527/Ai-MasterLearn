package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.AiConfig;
import cn.org.alan.exam.model.vo.ai.AiConfigVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AI配置Mapper接口
 *
 * @author AI Assistant
 * @since 2024
 */
public interface AiConfigMapper extends BaseMapper<AiConfig> {
    
    /**
     * 分页查询AI配置
     *
     * @param page 分页对象
     * @param configName 配置名称
     * @return 分页结果
     */
    Page<AiConfigVO> selectAiConfigPage(Page<AiConfigVO> page, @Param("configName") String configName);
    
    /**
     * 获取激活的AI配置
     *
     * @return 激活的配置
     */
    AiConfig selectActiveConfig();
    
    /**
     * 取消所有配置的激活状态
     */
    void deactivateAllConfigs();

    /**
     * 物理删除AI配置（绕过@TableLogic逻辑删除）
     *
     * @param id 配置ID
     * @return 删除行数
     */
    @Delete("DELETE FROM t_ai_config WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Integer id);

    /**
     * 物理删除指定名称的所有配置（绕过@TableLogic，包括已软删除的）
     *
     * @param configName 配置名称
     * @return 删除行数
     */
    @Delete("DELETE FROM t_ai_config WHERE config_name = #{configName}")
    int physicalDeleteByName(@Param("configName") String configName);

    /**
     * 查询未删除的同名配置数量（绕过@TableLogic，用原生SQL）
     *
     * @param configName 配置名称
     * @return 未删除的同名配置数量
     */
    @Select("SELECT COUNT(*) FROM t_ai_config WHERE config_name = #{configName} AND is_deleted = 0")
    int countActiveByName(@Param("configName") String configName);
}