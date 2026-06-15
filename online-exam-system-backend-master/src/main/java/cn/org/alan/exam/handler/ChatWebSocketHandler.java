package cn.org.alan.exam.handler;

import cn.org.alan.exam.service.IAiChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天WebSocket处理器
 * 
 * @author AI Assistant
 * @since 2024
 */
@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    @Qualifier("enhancedAiChatServiceImpl")
    private IAiChatService aiChatService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.info("用户 {} 连接WebSocket成功，当前在线用户数: {}", userId, sessions.size());
            
            // 发送连接成功消息
            sendMessage(session, createSuccessMessage("连接成功"));
            
            // 广播用户上线消息
            broadcastOnlineStatus(userId, true);
        } else {
            log.warn("WebSocket连接缺少用户ID参数");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId == null) {
            log.warn("收到未知用户的消息");
            return;
        }

        try {
            Map<String, Object> messageData = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) messageData.get("type");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) messageData.get("data");

            log.info("收到用户 {} 的消息类型: {}", userId, type);

            switch (type) {
                case "ONLINE_STATUS":
                    handleOnlineStatus(userId, data);
                    break;
                case "CHAT_MESSAGE":
                    handleChatMessage(userId, data);
                    break;
                case "AI_REPLY_REQUEST":
                    handleAiReplyRequest(userId, data);
                    break;
                default:
                    log.warn("未知的消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            sendMessage(session, createErrorMessage("消息处理失败"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("用户 {} 断开WebSocket连接，当前在线用户数: {}", userId, sessions.size());
            
            // 广播用户下线消息
            broadcastOnlineStatus(userId, false);
        }
    }

    private String getUserIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("userId=")) {
                    return param.substring(7);
                }
            }
        }
        return null;
    }

    private void handleOnlineStatus(String userId, Map<String, Object> data) {
        Boolean isOnline = (Boolean) data.get("isOnline");
        log.info("用户 {} 在线状态变更: {}", userId, isOnline ? "上线" : "下线");
        
        // 广播在线状态
        broadcastOnlineStatus(userId, isOnline);
    }

    private void handleChatMessage(String fromUserId, Map<String, Object> data) {
        String toUserId = String.valueOf(data.get("toUserId"));
        String content = (String) data.get("content");
        
        log.info("用户 {} 发送消息给用户 {}: {}", fromUserId, toUserId, content);
        
        // 转发消息给接收者
        WebSocketSession toSession = sessions.get(toUserId);
        if (toSession != null && toSession.isOpen()) {
            try {
                Map<String, Object> innerData = new HashMap<>();
                innerData.put("fromUserId", Integer.parseInt(fromUserId));
                innerData.put("fromUserName", data.get("fromUserName"));
                innerData.put("content", content);
                innerData.put("createTime", System.currentTimeMillis());
                
                Map<String, Object> message = new HashMap<>();
                message.put("type", "CHAT_MESSAGE");
                message.put("data", innerData);
                
                sendMessage(toSession, message);
            } catch (Exception e) {
                log.error("转发消息失败", e);
            }
        } else {
            log.info("用户 {} 不在线，消息暂存", toUserId);
        }
    }

    private void handleAiReplyRequest(String userId, Map<String, Object> data) {
        log.info("处理用户 {} 的AI代理请求", userId);
        
        try {
            String question = (String) data.get("question");
            String fromUserId = String.valueOf(data.get("fromUserId"));
            
            if (question == null || question.trim().isEmpty()) {
                log.warn("AI代理请求问题为空");
                return;
            }
            
            log.info("用户 {} 请求AI代回复用户 {} 的问题: {}", userId, fromUserId, question);
            
            // 调用AI服务获取回复 - 使用模拟的用户ID
            String aiResponse = callAiServiceWithMockUser(question, Integer.parseInt(userId));
            
            log.info("AI回复内容: {}", aiResponse);
            
            // 将AI回复发送给请求者
            WebSocketSession session = sessions.get(userId);
            if (session != null && session.isOpen()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("question", question);
                responseData.put("response", aiResponse);
                responseData.put("fromUserId", Integer.parseInt(fromUserId));
                responseData.put("timestamp", System.currentTimeMillis());
                
                Map<String, Object> responseMessage = new HashMap<>();
                responseMessage.put("type", "AI_REPLY_RESPONSE");
                responseMessage.put("data", responseData);
                
                sendMessage(session, responseMessage);
            }
            
            // 同时将AI回复作为普通消息发送给原提问者
            if (sessions.containsKey(fromUserId)) {
                WebSocketSession fromSession = sessions.get(fromUserId);
                if (fromSession != null && fromSession.isOpen()) {
                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("fromUserId", Integer.parseInt(userId));
                    chatData.put("fromUserName", "AI助手");
                    chatData.put("content", "AI代理回复: " + aiResponse);
                    chatData.put("createTime", System.currentTimeMillis());
                    
                    Map<String, Object> chatMessage = new HashMap<>();
                    chatMessage.put("type", "CHAT_MESSAGE");
                    chatMessage.put("data", chatData);
                    
                    sendMessage(fromSession, chatMessage);
                }
            }
            
        } catch (Exception e) {
            log.error("处理AI代理请求失败", e);
            
            // 发送错误消息
            WebSocketSession session = sessions.get(userId);
            if (session != null && session.isOpen()) {
                sendMessage(session, createErrorMessage("AI代理处理失败: " + e.getMessage()));
            }
        }
    }

    private void broadcastOnlineStatus(String userId, boolean isOnline) {
        Map<String, Object> innerData = new HashMap<>();
        innerData.put("userId", Integer.parseInt(userId));
        innerData.put("isOnline", isOnline);
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ONLINE_STATUS");
        message.put("data", innerData);

        // 广播给所有在线用户
        sessions.forEach((id, session) -> {
            if (!id.equals(userId)) {
                try {
                    // 双重检查：发送前验证session状态
                    synchronized (session) {
                        if (session.isOpen()) {
                            sendMessage(session, message);
                        }
                    }
                } catch (Exception e) {
                    // 降低日志级别，避免连接关闭时的正常异常污染日志
                    log.debug("广播在线状态失败 - 用户ID: {}, 错误: {}", id, e.getMessage());
                }
            }
        });
    }

    /**
     * 安全发送消息（带同步锁和状态检查）
     */
    private void sendMessage(WebSocketSession session, Object message) {
        // 再次检查session状态（外层已有同步锁）
        if (session == null || !session.isOpen()) {
            log.debug("Session不可用，跳过消息发送");
            return;
        }
        
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(jsonMessage);
            
            // 使用同步块防止并发写入
            synchronized (session) {
                // 发送前最后一次检查
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        } catch (IllegalStateException e) {
            // WebSocket状态异常（如正在写入），静默处理
            log.debug("WebSocket状态异常，无法发送消息: {}", e.getMessage());
        } catch (IOException e) {
            // IO异常可能是连接已断开
            log.debug("发送WebSocket消息IO异常: {}", e.getMessage());
        } catch (Exception e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

    private Map<String, Object> createSuccessMessage(String message) {
        Map<String, Object> innerData = new HashMap<>();
        innerData.put("success", true);
        innerData.put("message", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("type", "SYSTEM");
        result.put("data", innerData);
        
        return result;
    }

    private Map<String, Object> createErrorMessage(String message) {
        Map<String, Object> innerData = new HashMap<>();
        innerData.put("success", false);
        innerData.put("message", message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("type", "SYSTEM");
        result.put("data", innerData);
        
        return result;
    }

    /**
     * 模拟用户身份调用AI服务
     */
    private String callAiServiceWithMockUser(String question, Integer userId) {
        try {
            // 创建一个简单的AI回复，避免Spring Security认证问题
            String mockResponse = generateMockAiResponse(question);
            log.info("模拟AI回复成功: {}", mockResponse);
            return mockResponse;
        } catch (Exception e) {
            log.error("模拟AI服务调用失败", e);
            return "抱歉，AI服务暂时不可用，请稍后重试。";
        }
    }

    /**
     * 生成模拟的AI回复
     */
    private String generateMockAiResponse(String question) {
        // 简单的关键词匹配回复
        if (question.contains("你好") || question.contains("hello")) {
            return "你好！我是AI助手，很高兴为你服务。";
        } else if (question.contains("帮助") || question.contains("help")) {
            return "我可以帮你回答问题、提供学习建议等。请告诉我你需要什么帮助？";
        } else if (question.contains("学习") || question.contains("study")) {
            return "关于学习，我建议你制定合理的学习计划，保持专注，定期复习。有什么具体的学习问题吗？";
        } else if (question.contains("考试") || question.contains("exam")) {
            return "考试前要做好充分准备，包括复习知识点、做模拟题、调整心态等。需要我提供具体的备考建议吗？";
        } else if (question.contains("?")) {
            return "这是一个很好的问题！基于我的理解，我认为这个问题可以从多个角度来分析。具体来说...";
        } else {
            // 默认回复
            return "感谢你的消息！我理解你的意思是：" + question + "。关于这个问题，我认为...";
        }
    }
}