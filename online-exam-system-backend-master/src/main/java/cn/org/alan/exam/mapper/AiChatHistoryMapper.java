package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.AiChatHistory;
import cn.org.alan.exam.model.vo.ai.AiChatHistoryVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI聊天记录Mapper接口
 *
 * @author AI Assistant
 * @since 2024
 */
public interface AiChatHistoryMapper extends BaseMapper<AiChatHistory> {
    
    /**
     * 根据用户ID查询聊天记录
     *
     * @param userId 用户ID
     * @return 聊天记录列表
     */
    List<AiChatHistoryVO> selectByUserId(@Param("userId") Integer userId);
    
    /**
     * 分页查询用户聊天记录
     *
     * @param page 分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<AiChatHistoryVO> selectChatHistoryPage(Page<AiChatHistoryVO> page, @Param("userId") Integer userId);
    
    /**
     * 逻辑删除用户的所有聊天记录
     *
     * @param userId 用户ID
     * @return 删除记录数
     */
    int deleteByUserId(@Param("userId") Integer userId);
}