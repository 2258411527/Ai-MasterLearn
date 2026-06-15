package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 聊天消息服务接口
 */
public interface IChatMessageService extends IService<ChatMessage> {

    /**
     * 发送消息
     */
    Result<ChatMessage> sendMessage(ChatMessage chatMessage);

    /**
     * 获取聊天记录
     */
    Result<List<ChatMessage>> getChatHistory(Integer userId1, Integer userId2, Integer limit);

    /**
     * 标记消息为已读
     */
    Result<String> markMessagesAsRead(Integer fromUserId, Integer toUserId);

    /**
     * 获取未读消息数量
     */
    Result<Integer> getUnreadMessageCount(Integer userId);

    /**
     * 删除聊天记录
     */
    Result<String> deleteChatHistory(Integer userId, Integer friendId);
}