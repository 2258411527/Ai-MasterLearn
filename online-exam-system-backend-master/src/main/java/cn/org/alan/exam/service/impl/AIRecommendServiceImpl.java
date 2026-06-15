package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.service.IAIRecommendService;
import cn.org.alan.exam.service.IAiConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class AIRecommendServiceImpl implements IAIRecommendService {

    @Autowired
    private UserStudyGoalMapper userStudyGoalMapper;

    @Autowired
    private UserKaoyanInfoMapper userKaoyanInfoMapper;

    @Autowired
    private UserStudyAnalysisMapper userStudyAnalysisMapper;

    @Autowired
    private UserDailyLoginDurationMapper userDailyLoginDurationMapper;

    // TODO: RepWrongQuestionMapper 对应的实体类和Mapper尚未实现
    // @Autowired
    // private RepWrongQuestionMapper repWrongQuestionMapper;

    @Autowired
    private IAiConfigService aiConfigService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Result<List<Map<String, Object>>> generatePersonalizedRecommendations(Integer userId) {
        try {
            UserStudyGoal goal = userStudyGoalMapper.selectOne(
                new LambdaQueryWrapper<UserStudyGoal>().eq(UserStudyGoal::getUserId, userId)
            );

            UserKaoyanInfo kaoyan = userKaoyanInfoMapper.selectOne(
                new LambdaQueryWrapper<UserKaoyanInfo>().eq(UserKaoyanInfo::getUserId, userId)
            );

            UserStudyAnalysis analysis = userStudyAnalysisMapper.selectOne(
                new LambdaQueryWrapper<UserStudyAnalysis>().eq(UserStudyAnalysis::getUserId, userId)
            );

            String prompt = buildRecommendationPrompt(goal, kaoyan, analysis);
            String aiResponse = callAI(prompt);

            List<Map<String, Object>> recommendations = parseAIResponse(aiResponse);
            
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("生成AI推荐失败", e);
            return Result.success(getDefaultRecommendations());
        }
    }

    @Override
    public Result<String> generateDailyStudyPlan(Integer userId) {
        try {
            UserStudyGoal goal = userStudyGoalMapper.selectOne(
                new LambdaQueryWrapper<UserStudyGoal>().eq(UserStudyGoal::getUserId, userId)
            );

            UserKaoyanInfo kaoyan = userKaoyanInfoMapper.selectOne(
                new LambdaQueryWrapper<UserKaoyanInfo>().eq(UserKaoyanInfo::getUserId, userId)
            );

            int todayMinutes = getTodayStudyMinutes(userId);
            String dailyHours = goal != null ? goal.getDailyStudyHours() : "4";

            String prompt = "请为一位考研学生生成今日学习计划。\n" +
                "目标考试类型：" + (goal != null ? goal.getExamType() : "考研") + "\n" +
                "目标院校层次：" + (kaoyan != null && kaoyan.getTargetUniversityLevel() != null ? kaoyan.getTargetUniversityLevel() : "未设置") + "\n" +
                "每日计划学习时长：" + dailyHours + "小时\n" +
                "今日已学习：" + todayMinutes + "分钟\n" +
                "薄弱模块：" + (kaoyan != null && kaoyan.getWeakModules() != null ? kaoyan.getWeakModules() : "未设置") + "\n\n" +
                "请生成简洁的学习计划，包含具体的学习任务和时间分配，以列表形式输出。";

            String aiResponse = callAI(prompt);
            return Result.success(aiResponse);
        } catch (Exception e) {
            log.error("生成每日学习计划失败", e);
            return Result.success("建议今日完成：\n1. 数学基础练习 1小时\n2. 英语阅读训练 1小时\n3. 专业课复习 2小时");
        }
    }

    @Override
    public Result<Map<String, Object>> analyzeWeakPoints(Integer userId) {
        try {
            // TODO: RepWrongQuestionMapper 尚未实现
            // LambdaQueryWrapper<RepWrongQuestion> wrapper = new LambdaQueryWrapper<>();
            // wrapper.eq(RepWrongQuestion::getUserId, userId);
            // Long wrongCount = repWrongQuestionMapper.selectCount(wrapper);
            Long wrongCount = 0L;

            UserKaoyanInfo kaoyan = userKaoyanInfoMapper.selectOne(
                new LambdaQueryWrapper<UserKaoyanInfo>().eq(UserKaoyanInfo::getUserId, userId)
            );

            Map<String, Object> result = new HashMap<>();
            result.put("totalWrongCount", wrongCount);
            result.put("weakModules", kaoyan != null ? kaoyan.getWeakModules() : "");
            
            if (wrongCount > 0) {
                String prompt = "请分析以下学生的薄弱点并给出改进建议：\n" +
                    "错题数量：" + wrongCount + "\n" +
                    "标记的薄弱模块：" + (kaoyan != null && kaoyan.getWeakModules() != null ? kaoyan.getWeakModules() : "无") + "\n" +
                    "请给出针对性的学习建议。";
                
                String advice = callAI(prompt);
                result.put("aiAdvice", advice);
            } else {
                result.put("aiAdvice", "暂无错题记录，继续保持！");
            }

            return Result.success(result);
        } catch (Exception e) {
            log.error("分析薄弱点失败", e);
            return Result.failed("分析失败");
        }
    }

    @Override
    public Result<String> getStudyAdvice(Integer userId, String subject) {
        try {
            UserKaoyanInfo kaoyan = userKaoyanInfoMapper.selectOne(
                new LambdaQueryWrapper<UserKaoyanInfo>().eq(UserKaoyanInfo::getUserId, userId)
            );

            String prompt = "请为考研学生提供" + subject + "科目的学习建议。\n" +
                "学生情况：\n" +
                "- 目标院校：" + (kaoyan != null && kaoyan.getTargetUniversityLevel() != null ? kaoyan.getTargetUniversityLevel() : "未设置") + "\n" +
                "- 考试科目：" + (kaoyan != null && kaoyan.getEnglishSubject() != null ? kaoyan.getEnglishSubject() : "") + " " +
                (kaoyan != null && kaoyan.getMathSubject() != null ? kaoyan.getMathSubject() : "") + "\n" +
                "- 薄弱模块：" + (kaoyan != null && kaoyan.getWeakModules() != null ? kaoyan.getWeakModules() : "未设置") + "\n\n" +
                "请给出具体、可操作的学习建议。";

            String advice = callAI(prompt);
            return Result.success(advice);
        } catch (Exception e) {
            log.error("获取学习建议失败", e);
            return Result.success("建议多做真题，注重基础知识的巩固。");
        }
    }

    private String buildRecommendationPrompt(UserStudyGoal goal, UserKaoyanInfo kaoyan, UserStudyAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下学生信息，推荐4个最适合的学习任务。以JSON数组格式返回，每个任务包含title、description、reason字段。\n\n");
        sb.append("学生画像：\n");
        
        if (goal != null) {
            sb.append("- 考试类型：").append(goal.getExamType()).append("\n");
            sb.append("- 目标年份：").append(goal.getTargetYear()).append("年\n");
            sb.append("- 备考身份：").append(goal.getStudyIdentity()).append("\n");
            sb.append("- 每日学习时长：").append(goal.getDailyStudyHours()).append("小时\n");
            sb.append("- 当前进度：").append(goal.getCurrentProgress()).append("\n");
        }
        
        if (kaoyan != null) {
            sb.append("- 目标院校层次：").append(kaoyan.getTargetUniversityLevel()).append("\n");
            sb.append("- 学硕/专硕：").append(kaoyan.getDegreeType()).append("\n");
            sb.append("- 英语科目：").append(kaoyan.getEnglishSubject()).append("\n");
            sb.append("- 数学科目：").append(kaoyan.getMathSubject()).append("\n");
            sb.append("- 薄弱模块：").append(kaoyan.getWeakModules()).append("\n");
        }
        
        if (analysis != null) {
            sb.append("- 累计学习天数：").append(analysis.getTotalStudyDays()).append("天\n");
            sb.append("- 正确率：").append(analysis.getCorrectRate()).append("%\n");
        }
        
        sb.append("\n请返回格式如：[{\"title\":\"任务名称\",\"description\":\"任务描述\",\"reason\":\"推荐原因\"}]");
        
        return sb.toString();
    }

    private String callAI(String prompt) {
        try {
            Result<AiConfig> configResult = aiConfigService.getActiveConfig();
            if (configResult.getCode() != 1 || configResult.getData() == null) {
                log.warn("AI配置未激活");
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
            requestBody.put("max_tokens", 1000);
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

    private List<Map<String, Object>> parseAIResponse(String response) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            if (response.contains("[") && response.contains("]")) {
                int start = response.indexOf("[");
                int end = response.lastIndexOf("]") + 1;
                String json = response.substring(start, end);
                result = objectMapper.readValue(json, List.class);
            }
        } catch (Exception e) {
            log.warn("解析AI响应失败", e);
        }
        
        if (result.isEmpty()) {
            return getDefaultRecommendations();
        }
        return result;
    }

    private List<Map<String, Object>> getDefaultRecommendations() {
        List<Map<String, Object>> defaults = new ArrayList<>();
        
        Map<String, Object> item1 = new HashMap<>();
        item1.put("title", "模拟测试");
        item1.put("description", "检验学习成果");
        item1.put("reason", "定期测试查漏补缺");
        defaults.add(item1);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("title", "错题复习");
        item2.put("description", "巩固薄弱知识点");
        item2.put("reason", "错题是提分关键");
        defaults.add(item2);
        
        Map<String, Object> item3 = new HashMap<>();
        item3.put("title", "专项练习");
        item3.put("description", "针对性训练");
        item3.put("reason", "提升解题能力");
        defaults.add(item3);
        
        Map<String, Object> item4 = new HashMap<>();
        item4.put("title", "AI答疑");
        item4.put("description", "智能解答疑问");
        item4.put("reason", "随时获取帮助");
        defaults.add(item4);
        
        return defaults;
    }

    private int getTodayStudyMinutes(Integer userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<UserDailyLoginDuration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDailyLoginDuration::getUserId, userId)
               .eq(UserDailyLoginDuration::getLoginDate, today);
        UserDailyLoginDuration duration = userDailyLoginDurationMapper.selectOne(wrapper);
        // totalSeconds 是秒数,转换为分钟
        return duration != null && duration.getTotalSeconds() != null 
            ? duration.getTotalSeconds() / 60 : 0;
    }
}
