package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.AiConfig;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.entity.ExamQuestion;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.entity.UserExamsScore;
import cn.org.alan.exam.model.form.ai.AiGradingForm;
import cn.org.alan.exam.model.vo.ai.AiGradingResultVO;
import cn.org.alan.exam.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RealAiGradingServiceImpl implements RealAiGradingService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IQuestionService questionService;

    @Autowired
    private IExamQuAnswerService examQuAnswerService;

    @Autowired
    private IUserExamsScoreService userExamsScoreService;

    @Autowired
    private IExamService examService;

    @Autowired
    private IAiConfigService aiConfigService;

    @Autowired
    private IExamQuestionService examQuestionService;

    @Autowired
    private IRagKnowledgeService ragKnowledgeService;

    private final Map<String, GradingProgress> gradingProgressMap = new ConcurrentHashMap<>();

    @Override
    public Result<AiGradingResultVO> gradeWithRealAI(AiGradingForm form) {
        try {
            if (form.getQuestionId() == null || form.getQuestionId() <= 0) {
                return Result.failed("题目ID不能为空且必须大于0");
            }

            Question question = questionService.getById(form.getQuestionId());
            if (question == null) {
                return Result.failed("题目不存在");
            }

            Result<AiConfig> configResult = aiConfigService.resolveConfig(form.getConfigId());
            if (configResult.getCode() != 1 || configResult.getData() == null) {
                return Result.failed("AI服务未配置，请联系管理员");
            }
            AiConfig activeConfig = configResult.getData();
            log.info("AI阅卷使用配置: id={}, name={}, model={}, configId参数={}",
                    activeConfig.getId(), activeConfig.getConfigName(), activeConfig.getModel(), form.getConfigId());

            boolean isGrading = form.getGradingMode() != null && form.getGradingMode();

            String gradingPrompt = buildGradingPrompt(form, question, isGrading);

            String aiResponse = callRealAiGradingAPI(activeConfig, gradingPrompt);

            AiGradingResultVO result = parseAiGradingResponse(aiResponse, form, question);

            if (isGrading) {
                saveAiGradingResult(form, result);
                log.info("AI阅卷完成 - 题目ID: {}, 用户ID: {}, AI评分: {}",
                        form.getQuestionId(), form.getUserId(), result.getAiScore());
            } else {
                log.info("AI解析完成 - 题目ID: {}, 用户ID: {}",
                        form.getQuestionId(), form.getUserId());
            }

            return Result.success(result);

        } catch (Exception e) {
            log.error("AI阅卷异常", e);
            return Result.failed("AI阅卷失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Object> batchGradeWithRealAI(Integer examId, Integer userId) {
        try {
            LambdaQueryWrapper<UserExamsScore> examWrapper = new LambdaQueryWrapper<>();
            examWrapper.eq(UserExamsScore::getExamId, examId)
                    .eq(UserExamsScore::getUserId, userId);
            UserExamsScore userExam = userExamsScoreService.getOne(examWrapper);
            if (userExam == null) {
                return Result.failed("考试记录不存在");
            }

            LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamQuAnswer::getExamId, examId)
                    .eq(ExamQuAnswer::getUserId, userId)
                    .eq(ExamQuAnswer::getQuestionType, 4)
                    .isNull(ExamQuAnswer::getAiScore);

            List<ExamQuAnswer> answersToGrade = examQuAnswerService.list(wrapper);

            if (answersToGrade.isEmpty()) {
                return Result.success("所有简答题已完成AI阅卷，系统将自动计算总分");
            }

            String progressKey = examId + "_" + userId;
            GradingProgress progress = new GradingProgress(answersToGrade.size());
            gradingProgressMap.put(progressKey, progress);

            final AiConfig activeConfig = aiConfigService.resolveConfig(null).getData();

            CompletableFuture.runAsync(() -> {
                try {
                    batchGradeAnswersConcurrently(examId, userId, answersToGrade, progress, activeConfig);
                    calculateTotalScore(examId, userId);
                    log.info("批量AI阅卷完成 - 考试ID: {}, 用户ID: {}, 题目数: {}",
                            examId, userId, answersToGrade.size());
                } catch (Exception e) {
                    log.error("批量AI阅卷异常", e);
                    progress.setError(e.getMessage());
                }
            });

            Map<String, Object> result = new HashMap<>();
            result.put("message", "简答题AI阅卷任务已提交，系统将并行处理各题目，评分完成后自动计算总分");
            result.put("totalQuestions", answersToGrade.size());
            result.put("progressKey", progressKey);

            return Result.success(result);

        } catch (Exception e) {
            log.error("批量AI阅卷异常", e);
            return Result.failed("批量AI阅卷失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Object> getGradingProgress(Integer examId, Integer userId) {
        String progressKey = examId + "_" + userId;
        GradingProgress progress = gradingProgressMap.get(progressKey);

        if (progress == null) {
            return Result.failed("未找到阅卷进度信息");
        }

        Map<String, Object> progressInfo = new HashMap<>();
        progressInfo.put("total", progress.getTotal());
        progressInfo.put("completed", progress.getCompleted());
        progressInfo.put("progress", progress.getProgress());
        progressInfo.put("status", progress.getStatus());
        progressInfo.put("error", progress.getError());

        return Result.success(progressInfo);
    }

    @Override
    public Result<Object> getGradableExams(Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return Result.failed("用户ID无效");
            }

            LambdaQueryWrapper<UserExamsScore> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserExamsScore::getUserId, userId)
                    .eq(UserExamsScore::getState, 1);

            List<UserExamsScore> exams = userExamsScoreService.list(wrapper);

            if (exams == null || exams.isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            List<Map<String, Object>> gradableExams = exams.stream()
                    .map(exam -> {
                        Map<String, Object> examInfo = new HashMap<>();
                        examInfo.put("examId", exam.getExamId());

                        Exam examDetail = examService.getById(exam.getExamId());
                        examInfo.put("examName", examDetail != null ? examDetail.getTitle() : "未知考试");
                        examInfo.put("examDate", exam.getCreateTime());

                        LambdaQueryWrapper<ExamQuAnswer> answerWrapper = new LambdaQueryWrapper<>();
                        answerWrapper.eq(ExamQuAnswer::getExamId, exam.getExamId())
                                .eq(ExamQuAnswer::getUserId, userId)
                                .isNull(ExamQuAnswer::getAiScore);

                        long ungradedCount = examQuAnswerService.count(answerWrapper);
                        examInfo.put("ungradedCount", ungradedCount);
                        examInfo.put("canGrade", ungradedCount > 0);

                        return examInfo;
                    })
                    .filter(exam -> exam.get("ungradedCount") != null && (Long) exam.get("ungradedCount") > 0)
                    .collect(Collectors.toList());

            return Result.success(gradableExams);

        } catch (Exception e) {
            log.error("获取可阅卷试卷列表异常 - 用户ID: {}", userId, e);
            return Result.failed("获取可阅卷试卷列表失败: " + e.getMessage());
        }
    }

    private String buildGradingPrompt(AiGradingForm form, Question question, boolean isGrading) {
        String standardAnswer = getStandardAnswer(question);

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位资深的阅卷专家和学科教师。请对以下题目进行全面、专业的评分和分析：\n\n");
        prompt.append("【题目信息】\n");
        prompt.append("题目内容：").append(question.getContent()).append("\n");
        prompt.append("题目类型：").append(getQuestionTypeName(question.getQuType())).append("\n\n");

        prompt.append("【评分标准】\n");
        prompt.append("标准答案：").append(standardAnswer).append("\n");
        prompt.append("学生答案：").append(form.getUserAnswer()).append("\n\n");

        if (isGrading) {
            try {
                Result<List<String>> ragResult = ragKnowledgeService.searchAdminGradingKnowledge(
                        question.getContent(), 3);
                if (ragResult.getCode() == 1 && ragResult.getData() != null && !ragResult.getData().isEmpty()) {
                    prompt.append("【阅卷参考资料】\n");
                    prompt.append("以下为管理员配置的阅卷评分标准与参考资料中与本题相关的内容，请严格参考这些资料辅助评分与矫正：\n");
                    for (int i = 0; i < ragResult.getData().size(); i++) {
                        String ref = ragResult.getData().get(i);
                        if (ref.length() > 500) {
                            ref = ref.substring(0, 500) + "...";
                        }
                        prompt.append("参考").append(i + 1).append("：").append(ref).append("\n");
                    }
                    prompt.append("\n");
                }
            } catch (Exception e) {
                log.warn("获取管理员RAG阅卷参考资料失败", e);
            }
        }

        if (isGrading && (question.getQuType() == 4)) {
            prompt.append("【评分维度】（每题0-100分）\n");
            prompt.append("- 内容完整度（40%）：是否覆盖标准答案的核心要点\n");
            prompt.append("- 准确性（30%）：知识点表述是否准确无误\n");
            prompt.append("- 逻辑性（15%）：答案结构是否清晰、论述是否合理\n");
            prompt.append("- 深度与拓展（15%）：是否有独到见解或知识拓展\n\n");
            prompt.append("请严格按照以下格式返回（每行一个字段，中文冒号）：\n");
            prompt.append("评分：{0-100的数字}分\n");
            prompt.append("是否合格：{是/否}\n");
            prompt.append("评分依据：{简述核心扣分/加分原因}\n");
            prompt.append("详细分析：{从以上四个维度进行详细分析}\n");
            prompt.append("改进建议：{针对薄弱点给出具体可行的学习建议}\n");
            prompt.append("知识点：{列出本题涉及的核心知识点}\n");
            prompt.append("学习路径：{推荐下一步的学习方向和资源}\n");
        } else if (!isGrading && (question.getQuType() == 4)) {
            prompt.append("【分析说明】\n");
            prompt.append("请对该学生答案进行全面分析（注意：不输出评分和是否合格），帮助其理解题目考察要点和优化方向：\n\n");
            prompt.append("请严格按照以下格式返回（每行一个字段，中文冒号）：\n");
            prompt.append("详细分析：{从内容完整度、准确性、逻辑性、深度四个维度进行详细分析，指出优点和不足}\n");
            prompt.append("改进建议：{针对薄弱点给出具体可行的学习建议}\n");
            prompt.append("知识点：{列出本题涉及的核心知识点}\n");
            prompt.append("学习路径：{推荐下一步的学习方向和资源}\n");
        } else if (isGrading) {
            prompt.append("请按照以下格式返回分析结果（中文冒号）：\n");
            prompt.append("详细分析：{分析答案的正确性和错误原因}\n");
            prompt.append("改进建议：{具体可行的学习建议}\n");
            prompt.append("知识点：{列出本题考察的知识点}\n");
            prompt.append("选项分析：{逐个选项分析对错原因}\n");
        } else {
            prompt.append("请按照以下格式返回分析结果（中文冒号，注意：仅做分析，不输出评分）：\n");
            prompt.append("详细分析：{分析答案的正确性和错误原因}\n");
            prompt.append("改进建议：{具体可行的学习建议}\n");
            prompt.append("知识点：{列出本题考察的知识点}\n");
            prompt.append("选项分析：{逐个选项分析对错原因}\n");
        }

        return prompt.toString();
    }

    private String callRealAiGradingAPI(AiConfig config, String prompt) {
        try {
            String apiUrl = config.getBaseUrl() + "/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一位资深阅卷专家和学科教师，请严格按照要求的格式返回评分和分析结果。评分必须客观公正，分析要具体有深度。");

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(systemMsg);
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 3000);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                return parseAiResponse(responseBody);
            } else {
                throw new RuntimeException("AI API调用失败,状态码: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("调用AI API异常 - baseUrl: {}, model: {}, 错误: {}",
                    config.getBaseUrl(), config.getModel(), e.getMessage());

            String errorMsg = "AI服务调用失败: ";
            if (e instanceof org.springframework.web.client.ResourceAccessException) {
                errorMsg += "网络连接失败,请检查AI服务地址是否正确";
            } else if (e instanceof org.springframework.web.client.HttpClientErrorException.Unauthorized) {
                errorMsg += "API密钥无效或已过期";
            } else if (e instanceof org.springframework.web.client.HttpServerErrorException) {
                errorMsg += "AI服务器内部错误,请稍后重试";
            } else {
                errorMsg += e.getMessage();
            }

            throw new RuntimeException(errorMsg, e);
        }
    }

    private String parseAiResponse(Map<String, Object> responseBody) {
        if (responseBody == null || !responseBody.containsKey("choices")) {
            throw new RuntimeException("AI响应格式错误");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("AI响应为空");
        }

        Map<String, Object> choice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) choice.get("message");

        return (String) message.get("content");
    }

    private AiGradingResultVO parseAiGradingResponse(String aiResponse,
                                                      AiGradingForm form, Question question) {
        AiGradingResultVO result = new AiGradingResultVO();
        result.setQuestionId(form.getQuestionId());
        result.setQuestionContent(question.getContent());
        result.setQuestionType(question.getQuType());
        result.setUserAnswer(form.getUserAnswer());
        result.setStandardAnswer(getStandardAnswer(question));

        boolean isGrading = form.getGradingMode() != null && form.getGradingMode();

        if (question.getQuType() == 4) {
            parseEssayQuestionResponse(aiResponse, result, isGrading);
        } else {
            parseObjectiveQuestionResponse(aiResponse, result, isGrading);
        }

        return result;
    }

    private void parseEssayQuestionResponse(String aiResponse, AiGradingResultVO result, boolean isGrading) {
        try {
            String[] lines = aiResponse.split("\n");
            StringBuilder analysisBuf = new StringBuilder();

            for (String line : lines) {
                String trimmed = line.trim();
                if (isGrading && (trimmed.startsWith("评分：") || trimmed.startsWith("评分:"))) {
                    String scoreStr = trimmed.replaceAll("^评分[：:]", "").replace("分", "").trim();
                    scoreStr = parseScoreFromString(scoreStr);
                    try {
                        int score = Math.max(0, Math.min(100, Integer.parseInt(scoreStr)));
                        result.setAiScore(score);
                    } catch (NumberFormatException e) {
                        result.setAiScore(60);
                    }
                } else if (isGrading && (trimmed.startsWith("是否合格：") || trimmed.startsWith("是否合格:"))) {
                    String qualified = trimmed.replaceAll("^是否合格[：:]", "").trim();
                    result.setIsCorrect("是".equals(qualified));
                } else if (isGrading && (trimmed.startsWith("评分依据：") || trimmed.startsWith("评分依据:"))) {
                    result.setScoringCriteria(trimmed.replaceAll("^评分依据[：:]", "").trim());
                } else if (trimmed.startsWith("详细分析：") || trimmed.startsWith("详细分析:")) {
                    analysisBuf.append(trimmed.replaceAll("^详细分析[：:]", "").trim());
                } else if (trimmed.startsWith("改进建议：") || trimmed.startsWith("改进建议:")) {
                    result.setImprovementSuggestions(trimmed.replaceAll("^改进建议[：:]", "").trim());
                } else if (trimmed.startsWith("知识点：") || trimmed.startsWith("知识点:")) {
                    result.setKnowledgePoints(trimmed.replaceAll("^知识点[：:]", "").trim());
                } else if (trimmed.startsWith("学习路径：") || trimmed.startsWith("学习路径:")) {
                    result.setLearningPath(trimmed.replaceAll("^学习路径[：:]", "").trim());
                } else if (!trimmed.isEmpty() && !trimmed.startsWith("是否合格")) {
                    analysisBuf.append("\n").append(trimmed);
                }
            }

            result.setDetailedAnalysis(analysisBuf.toString().trim());

            if (isGrading && result.getAiScore() == null) {
                result.setAiScore(60);
                result.setIsCorrect(false);
                result.setDetailedAnalysis("AI评分解析失败，请检查答案质量。");
                result.setImprovementSuggestions("建议重新作答并提交评分。");
            }

        } catch (Exception e) {
            log.warn("简答题AI" + (isGrading ? "评分" : "分析") + "解析失败", e);
            result.setDetailedAnalysis("AI" + (isGrading ? "评分" : "分析") + "解析异常：" + e.getMessage());
            if (isGrading) {
                result.setAiScore(60);
                result.setIsCorrect(false);
            }
        }
    }

    private void parseObjectiveQuestionResponse(String aiResponse, AiGradingResultVO result, boolean isGrading) {
        try {
            String[] lines = aiResponse.split("\n");

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("详细分析：") || trimmed.startsWith("详细分析:")) {
                    result.setDetailedAnalysis(trimmed.replaceAll("^详细分析[：:]", "").trim());
                } else if (trimmed.startsWith("改进建议：") || trimmed.startsWith("改进建议:")) {
                    result.setImprovementSuggestions(trimmed.replaceAll("^改进建议[：:]", "").trim());
                } else if (trimmed.startsWith("知识点：") || trimmed.startsWith("知识点:")) {
                    result.setKnowledgePoints(trimmed.replaceAll("^知识点[：:]", "").trim());
                } else if (trimmed.startsWith("选项分析：") || trimmed.startsWith("选项分析:")) {
                    result.setOptionAnalysis(trimmed.replaceAll("^选项分析[：:]", "").trim());
                }
            }

            result.setAiScore(null);
            result.setIsCorrect(null);

            if (result.getDetailedAnalysis() == null) {
                result.setDetailedAnalysis("AI分析解析失败，请检查答案质量。");
            }

        } catch (Exception e) {
            log.warn("客观题AI分析解析失败", e);
            result.setDetailedAnalysis("AI分析解析异常：" + e.getMessage());
        }
    }

    private String getQuestionTypeName(Integer quType) {
        switch (quType) {
            case 1:
                return "单选题";
            case 2:
                return "多选题";
            case 3:
                return "判断题";
            case 4:
                return "简答题";
            default:
                return "未知题型";
        }
    }

    private void saveAiGradingResult(AiGradingForm form, AiGradingResultVO result) {
        LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuAnswer::getExamId, form.getExamId())
                .eq(ExamQuAnswer::getUserId, form.getUserId())
                .eq(ExamQuAnswer::getQuestionId, form.getQuestionId());

        ExamQuAnswer answerRecord = examQuAnswerService.getOne(wrapper);
        if (answerRecord != null) {
            if (result.getAiScore() != null) {
                int convertedScore = convertScoreToQuestionScore(form.getExamId(), form.getQuestionId(), result.getAiScore());
                answerRecord.setAiScore(convertedScore);
                answerRecord.setAiReason(result.getDetailedAnalysis());
                result.setAiScore(convertedScore);
            } else {
                answerRecord.setAiReason(result.getDetailedAnalysis());
            }

            examQuAnswerService.updateById(answerRecord);

            updateUserExamAiScore(form.getExamId(), form.getUserId());
        }
    }

    private int convertScoreToQuestionScore(Integer examId, Integer questionId, int aiPercentScore) {
        try {
            LambdaQueryWrapper<ExamQuestion> qw = new LambdaQueryWrapper<>();
            qw.eq(ExamQuestion::getExamId, examId)
                    .eq(ExamQuestion::getQuestionId, questionId);
            ExamQuestion examQuestion = examQuestionService.getOne(qw);
            if (examQuestion != null && examQuestion.getScore() != null && examQuestion.getScore() > 0) {
                int converted = (int) Math.round(aiPercentScore * examQuestion.getScore() / 100.0);
                return Math.max(0, Math.min(converted, examQuestion.getScore()));
            }
        } catch (Exception e) {
            log.warn("获取题目分值失败，使用AI百分制原始评分: examId={}, questionId={}", examId, questionId);
        }
        return aiPercentScore;
    }

    private void batchGradeAnswersConcurrently(Integer examId, Integer userId,
                                                List<ExamQuAnswer> answers, GradingProgress progress,
                                                AiConfig activeConfig) {
        int threadCount = Math.min(answers.size(), 3);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Future<?>> futures = new ArrayList<>();
        for (ExamQuAnswer answer : answers) {
            futures.add(executor.submit(() -> {
                try {
                    AiGradingForm form = new AiGradingForm();
                    form.setExamId(examId);
                    form.setUserId(userId);
                    form.setQuestionId(answer.getQuestionId());
                    form.setUserAnswer(answer.getAnswerContent());
                    form.setGradingMode(true);

                    Question question = questionService.getById(answer.getQuestionId());
                    if (question != null) {
                        form.setQuestionType(String.valueOf(question.getQuType()));
                        form.setQuestionContent(question.getContent());
                    }

                    Result<AiGradingResultVO> gradingResult = gradeWithRealAI(form);

                    if (gradingResult.getCode() == 1) {
                        progress.incrementCompleted();
                    } else {
                        log.warn("题目AI阅卷失败 - 题目ID: {}, 错误: {}",
                                answer.getQuestionId(), gradingResult.getMsg());
                    }
                } catch (Exception e) {
                    log.error("批量阅卷单题异常 - 题目ID: {}", answer.getQuestionId(), e);
                }
            }));
        }

        for (int i = 0; i < futures.size(); i++) {
            try {
                futures.get(i).get(120, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.error("题目 {} 阅卷超时", answers.get(i).getQuestionId());
            } catch (Exception e) {
                log.error("题目 {} 阅卷异常", answers.get(i).getQuestionId(), e);
            }
        }

        executor.shutdown();
        progress.setStatus("completed");
    }

    private void calculateTotalScore(Integer examId, Integer userId) {
        try {
            Exam exam = examService.getById(examId);
            if (exam == null) {
                return;
            }

            LambdaQueryWrapper<ExamQuAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.eq(ExamQuAnswer::getExamId, examId)
                    .eq(ExamQuAnswer::getUserId, userId);

            List<ExamQuAnswer> allAnswers = examQuAnswerService.list(answerWrapper);

            int totalScore = 0;

            for (ExamQuAnswer answer : allAnswers) {
                switch (answer.getQuestionType()) {
                    case 1:
                    case 2:
                    case 3:
                        if (answer.getIsRight() != null && answer.getIsRight() == 1) {
                            Integer questionScore = getQuestionScore(exam, answer.getQuestionType());
                            totalScore += questionScore != null ? questionScore : 0;
                        }
                        break;
                    case 4:
                        if (answer.getAiScore() != null) {
                            totalScore += answer.getAiScore();
                        }
                        break;
                    default:
                        break;
                }
            }

            LambdaQueryWrapper<UserExamsScore> userExamWrapper = new LambdaQueryWrapper<>();
            userExamWrapper.eq(UserExamsScore::getExamId, examId)
                    .eq(UserExamsScore::getUserId, userId);

            UserExamsScore userExam = userExamsScoreService.getOne(userExamWrapper);
            if (userExam != null) {
                userExam.setUserScore(totalScore);
                userExam.setWhetherMark(1);
                userExamsScoreService.updateById(userExam);
                log.info("总分计算完成 - 考试ID: {}, 用户ID: {}, 总分: {}", examId, userId, totalScore);
            }

        } catch (Exception e) {
            log.error("总分计算异常", e);
        }
    }

    private Integer getQuestionScore(Exam exam, Integer questionType) {
        switch (questionType) {
            case 1:
                return exam.getRadioScore();
            case 2:
                return exam.getMultiScore();
            case 3:
                return exam.getJudgeScore();
            default:
                return 0;
        }
    }

    private String getStandardAnswer(Question question) {
        return question.getAnalysis() != null ? question.getAnalysis() : "暂无标准答案";
    }

    private void updateUserExamAiScore(Integer examId, Integer userId) {
        try {
            calculateTotalScore(examId, userId);
        } catch (Exception e) {
            log.error("更新用户AI考试成绩异常", e);
        }
    }

    private String parseScoreFromString(String scoreStr) {
        if (scoreStr == null || scoreStr.trim().isEmpty()) {
            return "0";
        }
        String cleaned = scoreStr.trim()
                .replaceAll("[（\\(].*[）\\)]", "")
                .replaceAll("[^0-9]", "");
        return cleaned.isEmpty() ? "0" : cleaned;
    }

    private static class GradingProgress {
        private final int total;
        private int completed;
        private String status;
        private String error;

        public GradingProgress(int total) {
            this.total = total;
            this.completed = 0;
            this.status = "processing";
        }

        public synchronized void incrementCompleted() {
            completed++;
        }

        public int getTotal() {
            return total;
        }

        public int getCompleted() {
            return completed;
        }

        public String getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public double getProgress() {
            return total > 0 ? (double) completed / total * 100 : 0;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}