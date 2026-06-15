package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.AiChatHistory;
import cn.org.alan.exam.model.vo.ai.AiChatHistoryVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * AI聊天记录服务接口
 *
 * @author AI Assistant
 * @since 2024
 */
public interface IAiChatHistoryService extends IService<AiChatHistory> {
    
    /**
     * 保存聊天记录
     *
     * @param userId 用户ID
     * @param role 角色
     * @param content 内容
     * @return 保存结果
     */
    Result<String> saveChatHistory(Integer userId, String role, String content);
    
    /**
     * 获取用户的聊天记录
     *
     * @param userId 用户ID
     * @return 聊天记录列表
     */
    Result<List<AiChatHistoryVO>> getChatHistory(Integer userId);
    
    /**
     * 分页查询用户聊天记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID
     * @return 分页结果
     */
    Result<IPage<AiChatHistoryVO>> getChatHistoryPage(Integer pageNum, Integer pageSize, Integer userId);
    
    /**
     * 清除用户的聊天记录
     *
     * @param userId 用户ID
     * @return 清除结果
     */
    Result<String> clearChatHistory(Integer userId);
}