package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.AiConfig;
import cn.org.alan.exam.service.IAiChatHistoryService;
import cn.org.alan.exam.service.IAiChatService;
import cn.org.alan.exam.service.IAiConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class AiChatServiceImpl implements IAiChatService {

    @Resource
    private IAiConfigService aiConfigService;

    @Resource
    private IAiChatHistoryService aiChatHistoryService;

    @Override
    public String chat(String question) {
        return chat(question, null);
    }

    @Override
    public String chat(String question, Integer configId) {
        try {
            Result<AiConfig> configResult = aiConfigService.resolveConfig(configId);
            if (configResult.getCode() != 1 || configResult.getData() == null) {
                return "AI服务未配置，请联系管理员。";
            }
            AiConfig activeConfig = configResult.getData();
            log.info("AI对话使用配置: id={}, name={}, model={}, configId参数={}",
                    activeConfig.getId(), activeConfig.getConfigName(), activeConfig.getModel(), configId);

            String apiUrl = activeConfig.getBaseUrl() + "/chat/completions";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + activeConfig.getApiKey());

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个专业的学习助手，请用简洁清晰的语言回答用户的问题。");
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", question);
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", activeConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("stream", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String answer = parseAiResponse(responseBody);
                return answer;
            } else {
                return "AI服务异常，状态码：" + response.getStatusCode();
            }
        } catch (Exception e) {
            log.error("AI对话异常", e);
            return "AI服务出现异常：" + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
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