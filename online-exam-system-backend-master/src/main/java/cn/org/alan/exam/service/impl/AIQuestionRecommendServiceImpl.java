package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.vo.home.AIRecommendQuestionVO;
import cn.org.alan.exam.service.IAIQuestionRecommendService;
import cn.org.alan.exam.service.IAiConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIQuestionRecommendServiceImpl implements IAIQuestionRecommendService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private OptionMapper optionMapper;

    @Autowired
    private UserKaoyanInfoMapper userKaoyanInfoMapper;

    @Autowired
    private UserStudyGoalMapper userStudyGoalMapper;

    // TODO: ExamRecordMapper 对应的实体类和Mapper尚未实现
    // @Autowired
    // private ExamRecordMapper examRecordMapper;

    // TODO: UserAnswerMapper 对应的实体类和Mapper尚未实现
    // @Autowired
    // private UserAnswerMapper userAnswerMapper;

    @Autowired
    private IAiConfigService aiConfigService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Result<List<AIRecommendQuestionVO>> getRecommendQuestions(Integer userId, Integer limit) {
        try {
            // 收集学生信息
            UserKaoyanInfo kaoyan = userKaoyanInfoMapper.selectOne(
                new LambdaQueryWrapper<UserKaoyanInfo>().eq(UserKaoyanInfo::getUserId, userId)
            );
            UserStudyGoal goal = userStudyGoalMapper.selectOne(
                new LambdaQueryWrapper<UserStudyGoal>().eq(UserStudyGoal::getUserId, userId)
            );

            // 从数据库获取可用知识点
            List<String> knowledgePoints = getKnowledgePointsFromDB();

            // 构建学生画像
            String studentProfile = buildStudentProfile(kaoyan, goal);

            // 随机选择题型（1单选 2多选 3判断 4填空 5简答）
            int quType = new Random().nextInt(5) + 1;

            // 随机选择一个知识点
            String targetKnowledge = knowledgePoints.isEmpty() ? "综合" :
                knowledgePoints.get(new Random().nextInt(knowledgePoints.size()));

            // 调用AI生成题目
            AIRecommendQuestionVO question = generateQuestionByAI(studentProfile, targetKnowledge, quType);

            if (question == null) {
                return Result.failed("AI生成题目失败，请稍后重试");
            }

            question.setRecommendReason("根据您的学习情况，AI实时生成「" + targetKnowledge + "」专项练习");

            return Result.success(Collections.singletonList(question));
        } catch (Exception e) {
            log.error("获取AI推荐习题失败", e);
            return Result.failed("获取推荐习题失败");
        }
    }

    @Override
    public Result<List<AIRecommendQuestionVO>> getQuestionsByKnowledgePoint(String knowledgePoint, Integer limit) {
        try {
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(Question::getKnowledgePoint, knowledgePoint)
                   .eq(Question::getIsDeleted, 0)
                   .last("LIMIT " + limit);
            
            List<Question> questions = questionMapper.selectList(wrapper);
            List<AIRecommendQuestionVO> result = convertToVO(questions);
            
            for (AIRecommendQuestionVO q : result) {
                q.setRecommendReason("针对知识点「" + knowledgePoint + "」专项练习");
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取知识点习题失败", e);
            return Result.failed("获取习题失败");
        }
    }

    @Override
    public Result<List<AIRecommendQuestionVO>> getQuestionsForWeakPoints(Integer userId, Integer limit) {
        try {
            List<AIRecommendQuestionVO> questions = getWeakPointQuestionsInternal(userId, limit);
            return Result.success(questions);
        } catch (Exception e) {
            log.error("获取薄弱点习题失败", e);
            return Result.failed("获取习题失败");
        }
    }

    @Override
    public Result<String> generateAIRecommendationReason(Integer userId, Integer questionId) {
        try {
            Question question = questionMapper.selectById(questionId);
            if (question == null) {
                return Result.failed("题目不存在");
            }

            UserKaoyanInfo kaoyan = userKaoyanInfoMapper.selectOne(
                new LambdaQueryWrapper<UserKaoyanInfo>().eq(UserKaoyanInfo::getUserId, userId)
            );

            String prompt = "请分析为什么这道题目适合这位学生练习：\n" +
                "题目内容：" + (question.getContent() != null ? question.getContent().substring(0, Math.min(100, question.getContent().length())) : "") + "\n" +
                "知识点：" + question.getKnowledgePoint() + "\n" +
                "学生薄弱模块：" + (kaoyan != null && kaoyan.getWeakModules() != null ? kaoyan.getWeakModules() : "未设置") + "\n" +
                "请用一句话说明推荐原因，不超过50字。";

            String aiResponse = callAI(prompt);
            return Result.success(aiResponse);
        } catch (Exception e) {
            log.error("生成推荐原因失败", e);
            return Result.success("根据您的学习情况智能推荐");
        }
    }

    private List<String> getKnowledgePointsFromDB() {
        List<String> points = new ArrayList<>();
        try {
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(Question::getKnowledgePoint)
                   .eq(Question::getIsDeleted, 0)
                   .isNotNull(Question::getKnowledgePoint)
                   .groupBy(Question::getKnowledgePoint)
                   .last("LIMIT 20");
            List<Question> list = questionMapper.selectList(wrapper);
            for (Question q : list) {
                if (q.getKnowledgePoint() != null && !q.getKnowledgePoint().trim().isEmpty()) {
                    points.add(q.getKnowledgePoint().trim());
                }
            }
        } catch (Exception e) {
            log.warn("获取知识点列表失败", e);
        }
        return points;
    }

    private String buildStudentProfile(UserKaoyanInfo kaoyan, UserStudyGoal goal) {
        StringBuilder sb = new StringBuilder();
        if (goal != null) {
            sb.append("考试类型：").append(goal.getExamType() != null ? goal.getExamType() : "未设置").append("；");
        }
        if (kaoyan != null) {
            if (kaoyan.getWeakModules() != null && !kaoyan.getWeakModules().isEmpty()) {
                sb.append("薄弱模块：").append(kaoyan.getWeakModules()).append("；");
            }
            if (kaoyan.getMathSubject() != null) {
                sb.append("数学科目：").append(kaoyan.getMathSubject()).append("；");
            }
            if (kaoyan.getEnglishSubject() != null) {
                sb.append("英语科目：").append(kaoyan.getEnglishSubject()).append("；");
            }
            if (kaoyan.getTargetUniversityLevel() != null) {
                sb.append("目标院校层次：").append(kaoyan.getTargetUniversityLevel()).append("；");
            }
        }
        return sb.length() > 0 ? sb.toString() : "暂无详细学习信息";
    }

    private AIRecommendQuestionVO generateQuestionByAI(String studentProfile, String knowledgePoint, int quType) {
        String typeName = getQuTypeName(quType);

        String prompt = "你是一位专业的考试出题专家。请根据以下信息生成一道" + typeName + "：\n\n" +
            "学生画像：" + studentProfile + "\n" +
            "知识点：" + knowledgePoint + "\n" +
            "题型：" + typeName + "\n\n" +
            "请严格按照以下JSON格式输出，不要输出任何其他内容：\n";

        if (quType == 1) {
            prompt += "{\"content\":\"题干内容\",\"options\":[{\"key\":\"A\",\"content\":\"选项A\",\"isCorrect\":false}," +
                "{\"key\":\"B\",\"content\":\"选项B\",\"isCorrect\":true}," +
                "{\"key\":\"C\",\"content\":\"选项C\",\"isCorrect\":false}," +
                "{\"key\":\"D\",\"content\":\"选项D\",\"isCorrect\":false}]," +
                "\"analysis\":\"解析内容\"}";
        } else if (quType == 2) {
            prompt += "{\"content\":\"题干内容\",\"options\":[{\"key\":\"A\",\"content\":\"选项A\",\"isCorrect\":true}," +
                "{\"key\":\"B\",\"content\":\"选项B\",\"isCorrect\":false}," +
                "{\"key\":\"C\",\"content\":\"选项C\",\"isCorrect\":true}," +
                "{\"key\":\"D\",\"content\":\"选项D\",\"isCorrect\":false}]," +
                "\"analysis\":\"解析内容\"}" +
                "\n注意：多选题至少有2个正确选项";
        } else if (quType == 3) {
            prompt += "{\"content\":\"题干内容（判断对错）\",\"options\":[{\"key\":\"A\",\"content\":\"正确\",\"isCorrect\":true}," +
                "{\"key\":\"B\",\"content\":\"错误\",\"isCorrect\":false}]," +
                "\"analysis\":\"解析内容\"}";
        } else if (quType == 4) {
            prompt += "{\"content\":\"题干内容（用___标记填空位置）\",\"answer\":\"标准答案\"," +
                "\"analysis\":\"解析内容\"}";
        } else {
            prompt += "{\"content\":\"题干内容\",\"answer\":\"参考答案\"," +
                "\"analysis\":\"解析内容\"}";
        }

        prompt += "\n\n要求：\n1. 题目必须与知识点「" + knowledgePoint + "」紧密相关\n" +
            "2. 题目难度适中，适合考研复习\n" +
            "3. 解析要详细说明解题思路\n" +
            "4. 只输出JSON，不要输出markdown代码块标记";

        try {
            String aiResponse = callAI(prompt);
            return parseAIQuestionResponse(aiResponse, quType, knowledgePoint);
        } catch (Exception e) {
            log.error("AI生成题目失败", e);
            return null;
        }
    }

    private String getQuTypeName(int quType) {
        switch (quType) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            case 4: return "填空题";
            case 5: return "简答题";
            default: return "单选题";
        }
    }

    private AIRecommendQuestionVO parseAIQuestionResponse(String aiResponse, int quType, String knowledgePoint) {
        try {
            // 清理可能的markdown代码块标记
            String json = aiResponse.trim();
            if (json.startsWith("```")) {
                json = json.substring(json.indexOf('\n') + 1);
            }
            if (json.endsWith("```")) {
                json = json.substring(0, json.lastIndexOf("```"));
            }
            json = json.trim();

            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            AIRecommendQuestionVO vo = new AIRecommendQuestionVO();
            vo.setId(Integer.valueOf((int)(System.currentTimeMillis() % Integer.MAX_VALUE))); // 生成临时ID
            vo.setContent((String) map.get("content"));
            vo.setQuType(quType);
            vo.setQuTypeId(quType);
            vo.setKnowledgePoint(knowledgePoint);
            vo.setAnalysis((String) map.get("analysis"));

            // 选择题和判断题处理选项
            if (quType <= 3 && map.containsKey("options")) {
                List<Map<String, Object>> optionsRaw = (List<Map<String, Object>>) map.get("options");
                List<AIRecommendQuestionVO.AnswerOption> options = new ArrayList<>();
                for (Map<String, Object> opt : optionsRaw) {
                    AIRecommendQuestionVO.AnswerOption ao = new AIRecommendQuestionVO.AnswerOption();
                    ao.setKey((String) opt.get("key"));
                    ao.setContent((String) opt.get("content"));
                    ao.setIsCorrect(Boolean.TRUE.equals(opt.get("isCorrect")));
                    options.add(ao);
                }
                vo.setOptions(options);
            }

            // 填空题和简答题处理答案
            if (quType >= 4 && map.containsKey("answer")) {
                vo.setAnswer((String) map.get("answer"));
            }

            return vo;
        } catch (Exception e) {
            log.error("解析AI题目响应失败: {}", aiResponse, e);
            return null;
        }
    }

    private String callAI(String prompt) {
        try {
            Result<AiConfig> configResult = aiConfigService.getActiveConfig();
            if (configResult.getCode() != 1 || configResult.getData() == null) {
                return "AI服务暂未配置";
            }

            AiConfig config = configResult.getData();
            String baseUrl = config.getBaseUrl();
            String apiKey = config.getApiKey();
            String model = config.getModel();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", Collections.singletonList(message));
            requestBody.put("max_tokens", 800);
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/chat/completions",
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageResp = (Map<String, Object>) choice.get("message");
                    return (String) messageResp.get("content");
                }
            }

            return "AI响应解析失败";
        } catch (Exception e) {
            log.error("调用AI失败", e);
            return "AI服务暂时不可用";
        }
    }

    /**
     * 将Question实体列表转换为AIRecommendQuestionVO列表
     */
    private List<AIRecommendQuestionVO> convertToVO(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return new ArrayList<>();
        }
        
        return questions.stream().map(question -> {
            AIRecommendQuestionVO vo = new AIRecommendQuestionVO();
            vo.setId(question.getId());
            vo.setContent(question.getContent());
            vo.setQuType(question.getQuType());
            vo.setKnowledgePoint(question.getKnowledgePoint());
            vo.setAnalysis(question.getAnalysis());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取薄弱点相关题目（内部方法）
     */
    private List<AIRecommendQuestionVO> getWeakPointQuestionsInternal(Integer userId, Integer limit) {
        // TODO: 由于ExamRecordMapper和UserAnswerMapper尚未实现，这里先提供一个简化版本
        // 实际应该根据用户的答题记录分析薄弱知识点
        
        log.warn("薄弱点分析功能暂未完全实现，返回推荐知识点题目");
        
        // 获取用户信息
        UserKaoyanInfo kaoyan = userKaoyanInfoMapper.selectOne(
            new LambdaQueryWrapper<UserKaoyanInfo>().eq(UserKaoyanInfo::getUserId, userId)
        );
        
        if (kaoyan != null && kaoyan.getWeakModules() != null && !kaoyan.getWeakModules().isEmpty()) {
            // 如果有薄弱模块记录，从该模块中选择题目的知识点
            String weakModule = kaoyan.getWeakModules();
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(Question::getKnowledgePoint, weakModule)
                   .eq(Question::getIsDeleted, 0)
                   .last("LIMIT " + limit);
            List<Question> questions = questionMapper.selectList(wrapper);
            return convertToVO(questions);
        }
        
        // 如果没有明确的薄弱点，随机返回一些题目
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getIsDeleted, 0)
               .orderByDesc(Question::getId)
               .last("LIMIT " + limit);
        List<Question> questions = questionMapper.selectList(wrapper);
        return convertToVO(questions);
    }
}
