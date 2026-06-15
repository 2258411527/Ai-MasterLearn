package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.AiConfig;
import cn.org.alan.exam.service.IAiChatHistoryService;
import cn.org.alan.exam.service.IAiChatService;
import cn.org.alan.exam.service.IAiConfigService;
import cn.org.alan.exam.service.IRagKnowledgeService;
import cn.org.alan.exam.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class EnhancedAiChatServiceImpl implements IAiChatService {

    @Resource
    private IAiConfigService aiConfigService;
    @Resource
    private IRagKnowledgeService ragKnowledgeService;
    @Resource
    private IAiChatHistoryService aiChatHistoryService;

    @Override
    public String chat(String question) {
        return chatWithRag(question, true, 3);
    }

    @Override
    public String chat(String question, Integer configId) {
        return chatWithRag(question, true, 3, configId);
    }

    public String chatWithRag(String question, boolean enableRag, int topK) {
        return chatWithRag(question, enableRag, topK, null);
    }

    public String chatWithRag(String question, boolean enableRag, int topK, Integer configId) {
        try {
            Result<AiConfig> configResult = aiConfigService.resolveConfig(configId);
            if (configResult.getCode() != 1 || configResult.getData() == null) {
                return "AI服务未配置，请联系管理员。";
            }
            AiConfig activeConfig = configResult.getData();

            String apiUrl = activeConfig.getBaseUrl() + "/chat/completions";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + activeConfig.getApiKey());

            Integer userId = SecurityUtil.getUserId();
            String context = "";
            String noteRag = "";

            if (enableRag) {
                Result<List<String>> searchResult = ragKnowledgeService.searchKnowledge(userId, question, topK);
                if (searchResult.getCode() == 1 && searchResult.getData() != null && !searchResult.getData().isEmpty()) {
                    context = buildContextFromSearchResults(searchResult.getData());
                } else {
                    noteRag = "（注意：您当前的知识库中未找到与问题直接相关的资料，以下回答基于通用知识。）";
                }
            }

            // 构建消息列表（兼容 Java 8）
            List<Map<String, String>> messages = new ArrayList<>();

            // 系统消息
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", buildSystemPrompt(enableRag));
            messages.add(systemMsg);

            // 用户消息
            String userContent = buildUserMessage(question, context, enableRag);
            if (!noteRag.isEmpty()) {
                userContent = noteRag + "\n" + userContent;
            }
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userContent);
            messages.add(userMsg);

            // 请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", activeConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("stream", false);

            // 保存用户消息到历史
            aiChatHistoryService.saveChatHistory(userId, "user", question);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String answer = parseAiResponse(responseBody);
                aiChatHistoryService.saveChatHistory(userId, "assistant", answer);
                return answer;
            } else {
                String error = "AI服务异常，状态码：" + response.getStatusCode();
                aiChatHistoryService.saveChatHistory(userId, "assistant", error);
                return error;
            }
        } catch (Exception e) {
            log.error("增强AI对话异常", e);
            return "AI服务出现异常：" + e.getMessage();
        }
    }

    private String buildSystemPrompt(boolean enableRag) {
        return enableRag
                ? "你是一个专业的考研学习助手。回答时请优先参考提供的资料片段。"
                : "你是一个专业的考研学习助手，请用通用知识回答。";
    }

    private String buildContextFromSearchResults(List<String> results) {
        StringBuilder sb = new StringBuilder("以下是与问题相关的资料片段：\n");
        for (int i = 0; i < results.size(); i++) {
            sb.append("【片段").append(i + 1).append("】\n").append(results.get(i)).append("\n\n");
        }
        return sb.toString();
    }

    private String buildUserMessage(String question, String context, boolean enableRag) {
        if (enableRag && !context.isEmpty()) {
            return context + "\n请基于以上资料回答：\n" + question;
        }
        return question;
    }

    private String parseAiResponse(Map<String, Object> responseBody) {
        if (responseBody == null) return "AI返回空响应";
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null && message.containsKey("content")) {
                    return (String) message.get("content");
                }
            }
            if (responseBody.containsKey("content")) {
                return (String) responseBody.get("content");
            }
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
        }
        return responseBody.toString();
    }
}