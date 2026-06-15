package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.ChatMessageMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.ChatMessage;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.service.IChatMessageService;
import cn.org.alan.exam.service.IUserNotificationService;
import cn.org.alan.exam.utils.SecurityUtil;
import cn.org.alan.exam.websocket.WebsocketHandler;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息服务实现
 */
@Service
@Slf4j
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private IUserNotificationService notificationService;

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional
    public Result<ChatMessage> sendMessage(ChatMessage chatMessage) {
        try {
            // 设置消息属性
            chatMessage.setIsRead(0); // 未读
            chatMessage.setMessageType(1); // 文本消息
            chatMessage.setCreateTime(LocalDateTime.now());
            chatMessage.setUpdateTime(LocalDateTime.now());
            
            boolean success = save(chatMessage);
            if (success) {
                try {
                    User sender = userMapper.selectById(chatMessage.getFromUserId());
                    String senderName = sender != null ? sender.getRealName() : "用户";
                    String senderAvatar = sender != null ? sender.getAvatar() : null;
                    String previewContent = chatMessage.getContent();
                    if (previewContent != null && previewContent.length() > 50) {
                        previewContent = previewContent.substring(0, 50) + "...";
                    }
                    notificationService.createNotificationWithSender(
                            chatMessage.getToUserId(),
                            "friend_message",
                            chatMessage.getFromUserId(),
                            chatMessage.getFromUserId(),
                            senderName,
                            senderAvatar,
                            "新消息来自 " + senderName,
                            previewContent,
                            0
                    );
                } catch (Exception notifyEx) {
                    log.error("创建消息通知失败", notifyEx);
                }

                // 通过WebSocket推送消息给接收者
                try {
                    Map<String, Object> wsMessage = new HashMap<>();
                    wsMessage.put("type", "CHAT_MESSAGE");
                    wsMessage.put("data", chatMessage);
                    
                    String jsonMessage = objectMapper.writeValueAsString(wsMessage);
                    WebsocketHandler.sendMessageToUser(chatMessage.getToUserId(), jsonMessage);
                    
                    log.info("消息已发送并通过WebSocket推送给用户: {}", chatMessage.getToUserId());
                } catch (Exception e) {
                    log.error("WebSocket推送消息失败", e);
                    // WebSocket推送失败不影响消息保存
                }
                
                return Result.success("消息发送成功", chatMessage);
            } else {
                return Result.failed("消息发送失败");
            }
        } catch (Exception e) {
            log.error("发送消息异常", e);
            return Result.failed("消息发送失败");
        }
    }

    @Override
    public Result<List<ChatMessage>> getChatHistory(Integer userId1, Integer userId2, Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 50; // 默认加载50条消息
            }
            
            List<ChatMessage> chatHistory = baseMapper.selectChatHistory(userId1, userId2, limit);
            return Result.success("获取聊天记录成功", chatHistory);
        } catch (Exception e) {
            return Result.failed("获取聊天记录失败");
        }
    }

    @Override
    @Transactional
    public Result<String> markMessagesAsRead(Integer fromUserId, Integer toUserId) {
        try {
            baseMapper.markMessagesAsRead(fromUserId, toUserId);
            return Result.success("标记已读成功");
        } catch (Exception e) {
            return Result.failed("标记已读失败");
        }
    }

    @Override
    public Result<Integer> getUnreadMessageCount(Integer userId) {
        try {
            Integer count = baseMapper.countUnreadMessages(userId);
            return Result.success("获取未读消息数量成功", count);
        } catch (Exception e) {
            return Result.failed("获取未读消息数量失败");
        }
    }

    @Override
    @Transactional
    public Result<String> deleteChatHistory(Integer userId, Integer friendId) {
        try {
            LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.and(w -> w.and(w1 -> w1.eq(ChatMessage::getFromUserId, userId)
                                        .eq(ChatMessage::getToUserId, friendId))
                            .or(w2 -> w2.eq(ChatMessage::getFromUserId, friendId)
                                        .eq(ChatMessage::getToUserId, userId)))
                   .eq(ChatMessage::getIsDeleted, 0);
            
            List<ChatMessage> messages = baseMapper.selectList(wrapper);
            
            for (ChatMessage message : messages) {
                message.setIsDeleted(1);
                message.setUpdateTime(LocalDateTime.now());
                baseMapper.updateById(message);
            }
            
            return Result.success("删除聊天记录成功");
        } catch (Exception e) {
            return Result.failed("删除聊天记录失败");
        }
    }
}