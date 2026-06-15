package cn.org.alan.exam.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.ExamQuAnswerMapper;
import cn.org.alan.exam.model.entity.AiConfig;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.service.IAiConfigService;
import cn.org.alan.exam.service.IExamService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.org.alan.exam.model.vo.question.QuestionScoreVO;
import cn.org.alan.exam.service.IAutoScoringService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AutoScoringServiceImpl extends ServiceImpl<ExamQuAnswerMapper, ExamQuAnswer> implements IAutoScoringService {

    @Autowired
    private ExamQuAnswerMapper examQuAnswerMapper;

    @Autowired
    private IAiConfigService aiConfigService;

    @Autowired
    @Lazy
    private IExamService examService;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private RestTemplate restTemplate;

    private static final int MAX_RETRY = 3;
    private static final long BASE_DELAY_MS = 2000;
    private static final int BATCH_SIZE = 3;

    @Override
    @Async
    public void autoScoringExam(Integer examId, Integer userId) {
        log.info("========== AI智能评分开始 ==========");
        log.info("考试ID: {}, 用户ID: {}", examId, userId);

        Result<AiConfig> configResult = aiConfigService.resolveConfig(null);
        if (configResult.getCode() != 1 || configResult.getData() == null) {
            log.error("AI服务未配置，无法进行自动评分");
            return;
        }
        AiConfig activeConfig = configResult.getData();
        log.info("AI配置: {}, 模型: {}", activeConfig.getConfigName(), activeConfig.getModel());

        List<QuestionScoreVO> questions = examQuAnswerMapper.getQuestionsForGrading(examId, userId);
        if (questions == null || questions.isEmpty()) {
            log.info("没有待评分题目");
            return;
        }
        log.info("获取到 {} 道待评分题目", questions.size());

        List<List<QuestionScoreVO>> batches = partitionList(questions, BATCH_SIZE);
        log.info("分批处理: 共 {} 批，每批最多 {} 道", batches.size(), BATCH_SIZE);

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(batches.size(), 3));
        List<Future<int[]>> futures = new ArrayList<>();

        for (int i = 0; i < batches.size(); i++) {
            final List<QuestionScoreVO> batch = batches.get(i);
            final int batchIndex = i + 1;
            futures.add(executor.submit(() -> processBatch(activeConfig, examId, userId, batch, batchIndex)));
        }

        int totalUpdated = 0;
        int totalSkipped = 0;
        for (int i = 0; i < futures.size(); i++) {
            try {
                int[] result = futures.get(i).get(120, TimeUnit.SECONDS);
                totalUpdated += result[0];
                totalSkipped += result[1];
            } catch (TimeoutException e) {
                log.error("第 {} 批评分超时，跳过", (i + 1));
            } catch (Exception e) {
                log.error("第 {} 批评分异常: {}", (i + 1), e.getMessage());
            }
        }
        executor.shutdown();

        log.info("========== AI智能评分完成 ==========");
        log.info("总计: 成功更新 {} 条, 跳过 {} 条, 分批数: {}", totalUpdated, totalSkipped, batches.size());
    }

    private int[] processBatch(AiConfig config, Integer examId, Integer userId,
                               List<QuestionScoreVO> batch, int batchIndex) {
        int updated = 0;
        int skipped = 0;

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus status = platformTransactionManager.getTransaction(def);

            try {
                log.info("第 {} 批 第 {} 次尝试, 题目数: {}", batchIndex, attempt, batch.size());

                Exam exam = examService.getById(examId);
                String examTitle = exam != null ? exam.getTitle() : "未知考试";

                String prompt = buildBatchGradingPrompt(batch, examTitle);
                long startTime = System.currentTimeMillis();
                String response = callAiApi(config, prompt);
                long elapsed = System.currentTimeMillis() - startTime;
                log.info("第 {} 批 AI响应耗时: {} ms", batchIndex, elapsed);

                String scoringResult = extractJsonFromResponse(response);

                JSONObject resultObj = JSONUtil.parseObj(scoringResult);
                JSONArray scoreArray = resultObj.getJSONArray("评分结果");

                if (scoreArray == null || scoreArray.isEmpty()) {
                    log.warn("第 {} 批 AI返回评分结果为空", batchIndex);
                    throw new RuntimeException("AI返回评分结果为空");
                }

                Map<String, JSONObject> scoreMap = buildScoreMap(scoreArray);

                for (QuestionScoreVO q : batch) {
                    JSONObject item = scoreMap.get(q.getQuestionId());
                    if (item == null) {
                        skipped++;
                        continue;
                    }

                    String scoreStr = item.getStr("最终得分");
                    String reason = item.getStr("扣分明细");
                    String analysis = item.getStr("详细分析");
                    String suggestions = item.getStr("改进建议");
                    String knowledge = item.getStr("知识点");

                    Integer score = parseScore(scoreStr);
                    if (score == null) {
                        skipped++;
                        continue;
                    }
                    score = Math.max(0, Math.min(100, score));

                    String fullReason = buildFullReason(reason, analysis, suggestions, knowledge);

                    ExamQuAnswer answer = new ExamQuAnswer();
                    answer.setQuestionId(Integer.valueOf(q.getQuestionId()));
                    answer.setAiScore(score);
                    answer.setAiReason(fullReason);

                    LambdaQueryWrapper<ExamQuAnswer> qw = new LambdaQueryWrapper<>();
                    qw.eq(ExamQuAnswer::getExamId, examId)
                            .eq(ExamQuAnswer::getUserId, userId)
                            .eq(ExamQuAnswer::getQuestionId, answer.getQuestionId());

                    ExamQuAnswer existing = getOne(qw);
                    if (existing != null) {
                        answer.setId(existing.getId());
                        updateById(answer);
                        updated++;
                        log.info("题目ID {} 评分: {} 分", q.getQuestionId(), score);
                    } else {
                        skipped++;
                    }
                }

                platformTransactionManager.commit(status);
                log.info("第 {} 批完成: 更新 {} 条, 跳过 {} 条", batchIndex, updated, skipped);
                return new int[]{updated, skipped};

            } catch (Exception e) {
                platformTransactionManager.rollback(status);
                log.warn("第 {} 批 第 {} 次失败: {}", batchIndex, attempt, e.getMessage());

                if (attempt == MAX_RETRY) {
                    log.error("第 {} 批重试 {} 次后仍失败", batchIndex, MAX_RETRY);
                    return new int[]{updated, skipped};
                }

                long delay = BASE_DELAY_MS * (long) Math.pow(2, attempt - 1);
                log.info("第 {} 批 等待 {} ms 后重试...", batchIndex, delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return new int[]{updated, skipped};
                }
            }
        }
        return new int[]{updated, skipped};
    }

    private String buildBatchGradingPrompt(List<QuestionScoreVO> questions, String examTitle) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位资深的考试评卷专家。请对以下《").append(examTitle).append("》考试中的题目进行专业评分。\n\n");

        prompt.append("【评分规则】\n");
        prompt.append("1. 根据标准答案的完整度、准确度、逻辑性综合打分（0-100分）\n");
        prompt.append("2. 90-100分: 答案完整准确，逻辑清晰，有深度见解\n");
        prompt.append("3. 75-89分: 答案基本正确，部分细节缺失\n");
        prompt.append("4. 60-74分: 答案大体方向正确，但遗漏重要内容\n");
        prompt.append("5. 40-59分: 答案有部分正确内容，但存在明显错误\n");
        prompt.append("6. 0-39分: 答案基本不正确或完全偏离主题\n\n");

        prompt.append("【待评分题目列表】\n");
        for (int i = 0; i < questions.size(); i++) {
            QuestionScoreVO q = questions.get(i);
            prompt.append("--- 题目").append(i + 1).append(" ---\n");
            prompt.append("题目ID: ").append(q.getQuestionId()).append("\n");
            prompt.append("题目内容: ").append(q.getQuestionContent()).append("\n");
            prompt.append("满分: ").append(q.getTotalScore()).append("分\n");
            prompt.append("标准答案: ").append(q.getQusetionAnswer()).append("\n");
            prompt.append("学生答案: ").append(q.getUserAnswer()).append("\n\n");
        }

        prompt.append("请返回JSON格式的评分结果，格式严格如下：\n");
        prompt.append("{\n");
        prompt.append("  \"评分结果\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"题目ID\": \"题号\",\n");
        prompt.append("      \"最终得分\": \"得分数字\",\n");
        prompt.append("      \"扣分明细\": \"扣分原因\",\n");
        prompt.append("      \"详细分析\": \"答案优点和不足的详细分析\",\n");
        prompt.append("      \"改进建议\": \"具体可行的学习改进建议\",\n");
        prompt.append("      \"知识点\": \"涉及的考点和知识点\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    private String buildFullReason(String reason, String analysis, String suggestions, String knowledge) {
        StringBuilder sb = new StringBuilder();
        if (reason != null && !reason.isEmpty()) {
            sb.append("【扣分明细】").append(reason).append("\n\n");
        }
        if (analysis != null && !analysis.isEmpty()) {
            sb.append("【详细分析】").append(analysis).append("\n\n");
        }
        if (suggestions != null && !suggestions.isEmpty()) {
            sb.append("【改进建议】").append(suggestions).append("\n\n");
        }
        if (knowledge != null && !knowledge.isEmpty()) {
            sb.append("【知识点】").append(knowledge);
        }
        if (sb.length() == 0) {
            sb.append("无详细说明");
        }
        return sb.toString();
    }

    private Map<String, JSONObject> buildScoreMap(JSONArray scoreArray) {
        Map<String, JSONObject> map = new HashMap<>();
        for (int i = 0; i < scoreArray.size(); i++) {
            JSONObject item = scoreArray.getJSONObject(i);
            String id = item.getStr("题目ID");
            if (id != null) {
                map.put(id, item);
            }
        }
        return map;
    }

    private List<List<QuestionScoreVO>> partitionList(List<QuestionScoreVO> list, int size) {
        List<List<QuestionScoreVO>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    private String extractJsonFromResponse(String response) {
        Pattern codeBlockPattern = Pattern.compile("```(?:json)?\\s*\\r?\\n(.*?)\\s*```", Pattern.DOTALL);
        Matcher m = codeBlockPattern.matcher(response);
        if (m.find()) {
            return m.group(1).trim();
        }

        int braceStart = response.indexOf('{');
        int braceEnd = response.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            return response.substring(braceStart, braceEnd + 1);
        }

        return response;
    }

    private String callAiApi(AiConfig config, String prompt) {
        try {
            String url = config.getBaseUrl() + "/chat/completions";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一位资深的考试评卷专家，擅长客观公正地评估学生答案。请严格按照要求返回JSON格式的评分结果。");
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 4000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                return parseAiResponse(responseBody);
            }

            throw new RuntimeException("AI API返回异常状态码: " + response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("调用AI API失败: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String parseAiResponse(Map<String, Object> responseBody) {
        if (responseBody == null) return "";
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

    private Integer parseScore(String scoreStr) {
        if (scoreStr == null || scoreStr.trim().isEmpty()) {
            return null;
        }

        try {
            String cleaned = scoreStr.trim()
                .replaceAll("[分分数数得得:：\\s]+$", "")
                .replaceAll("[（\\(].*[）\\)]", "");
            return Integer.valueOf(cleaned.trim());
        } catch (NumberFormatException ignored) {
        }

        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(scoreStr);
        if (matcher.find()) {
            try {
                return (int) Math.round(Double.parseDouble(matcher.group(1)));
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
    }
}