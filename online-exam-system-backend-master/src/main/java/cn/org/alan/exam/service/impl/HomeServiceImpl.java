package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.home.CountdownForm;
import cn.org.alan.exam.model.form.home.TaskForm;
import cn.org.alan.exam.model.vo.home.*;
import cn.org.alan.exam.service.HomeService;
import cn.org.alan.exam.service.IAiConfigService;
import cn.org.alan.exam.common.result.Result;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 首页服务实现类
 */
@Slf4j
@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private UserDailyLoginDurationMapper userDailyLoginDurationMapper;

    @Autowired
    private DailyTaskMapper dailyTaskMapper;

    @Autowired
    private CountdownConfigMapper countdownConfigMapper;

    @Autowired
    private UserExerciseRecordMapper userExerciseRecordMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserStudyGoalMapper userStudyGoalMapper;

    @Autowired
    private IAiConfigService aiConfigService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public HomePageVO getHomePageData(Integer userId) {
        long startTime = System.currentTimeMillis();
        HomePageVO homePage = new HomePageVO();
        
        try {
            // 使用并行加载提升性能，所有数据查询同时进行
            CompletableFuture<List<StudyDurationVO>> studyTrendFuture = 
                CompletableFuture.supplyAsync(() -> getStudyDurationTrendData(userId));
            
            CompletableFuture<List<TaskVO>> tasksFuture = 
                CompletableFuture.supplyAsync(() -> getTodayTasks(userId));
            
            CompletableFuture<List<CountdownVO>> countdownsFuture = 
                CompletableFuture.supplyAsync(() -> getCountdowns(userId));
            
            CompletableFuture<WrongAnalysisVO> wrongAnalysisFuture = 
                CompletableFuture.supplyAsync(() -> getWrongAnalysisData(userId));
            
            CompletableFuture<UserInfoVO> userInfoFuture = 
                CompletableFuture.supplyAsync(() -> getUserInfo(userId));
            
            // 等待所有任务完成
            CompletableFuture.allOf(studyTrendFuture, tasksFuture, countdownsFuture, 
                                   wrongAnalysisFuture, userInfoFuture).join();
            
            // 获取结果
            homePage.setStudyDurationTrend(studyTrendFuture.get());
            homePage.setTodayTasks(tasksFuture.get());
            homePage.setCountdowns(countdownsFuture.get());
            homePage.setWrongAnalysis(wrongAnalysisFuture.get());
            homePage.setUserInfo(userInfoFuture.get());
            
            // 每日一笑单独加载（因为是调用外部API，可能较慢）
            try {
                homePage.setDailyJoke(getDailyJoke(userId));
            } catch (Exception e) {
                log.warn("获取每日一笑失败，使用默认笑话", e);
                homePage.setDailyJoke("学习路上总会有困难，但每一次克服都是成长！💪");
            }
            
            long endTime = System.currentTimeMillis();
            log.info("首页数据加载完成，耗时: {} ms", endTime - startTime);
            
        } catch (Exception e) {
            log.error("加载首页数据失败", e);
            // 如果并行加载失败，使用原来的串行方式作为降级方案
            homePage.setStudyDurationTrend(getStudyDurationTrendData(userId));
            homePage.setTodayTasks(getTodayTasks(userId));
            homePage.setCountdowns(getCountdowns(userId));
            homePage.setDailyJoke(getDailyJoke(userId));
            homePage.setWrongAnalysis(getWrongAnalysisData(userId));
            homePage.setUserInfo(getUserInfo(userId));
        }
        
        return homePage;
    }

    @Override
    public List<Object> getStudyDurationTrend(Integer userId) {
        return getStudyDurationTrendData(userId).stream().map(vo -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", vo.getDate());
            map.put("durationMinutes", vo.getDurationMinutes());
            map.put("durationHours", vo.getDurationHours());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskVO addTask(Integer userId, TaskForm form) {
        log.info("添加任务，用户ID: {}, 任务内容: {}", userId, form.getTaskContent());
        
        DailyTask task = new DailyTask();
        task.setUserId(userId);
        task.setTaskDate(LocalDate.now());
        task.setSubject(form.getSubject());
        task.setTaskContent(form.getTaskContent());
        task.setEstimatedMinutes(form.getEstimatedMinutes());
        task.setPriority(form.getPriority());
        task.setIsCompleted(0);

        dailyTaskMapper.insert(task);
        log.info("任务添加成功，任务ID: {}", task.getId());

        return convertToTaskVO(task);
    }

    @Override
    @Transactional
    public List<TaskVO> generateTasksByAI(Integer userId) {
        log.info("========== AI生成任务开始 ==========");
        log.info("用户ID: {}", userId);
        
        List<TaskVO> tasks = new ArrayList<>();

        try {
            String prompt = buildTaskGenerationPrompt(userId);
            log.info("构建的AI提示词长度: {} 字符", prompt.length());
            
            String aiResponse = callAIAPI(prompt);
            log.info("AI API调用完成，响应长度: {} 字符", aiResponse != null ? aiResponse.length() : 0);

            if (aiResponse != null && !aiResponse.isEmpty()) {
                log.info("AI响应内容预览: {}", aiResponse.substring(0, Math.min(200, aiResponse.length())));
                
                // 尝试解析JSON响应
                try {
                    JSONObject responseJson = JSON.parseObject(aiResponse);
                    if (responseJson.containsKey("tasks")) {
                        JSONArray tasksArray = responseJson.getJSONArray("tasks");
                        log.info("解析到 {} 个任务", tasksArray.size());
                        
                        for (int i = 0; i < tasksArray.size(); i++) {
                            JSONObject taskJson = tasksArray.getJSONObject(i);
                            DailyTask task = new DailyTask();
                            task.setUserId(userId);
                            task.setTaskDate(LocalDate.now());
                            task.setSubject(taskJson.getString("subject"));
                            task.setTaskContent(taskJson.getString("content"));
                            task.setEstimatedMinutes(taskJson.getInteger("durationMinutes"));
                            task.setPriority(taskJson.getInteger("priority"));
                            task.setIsCompleted(0);
                            task.setAiSuggestion(taskJson.getString("suggestion"));

                            dailyTaskMapper.insert(task);
                            tasks.add(convertToTaskVO(task));
                        }
                        log.info("✅ AI生成任务成功，任务数量: {}", tasks.size());
                    } else {
                        log.warn("⚠️ AI响应中未找到tasks字段，使用默认任务");
                        tasks = generateDefaultTasks(userId);
                    }
                } catch (Exception jsonEx) {
                    log.error("❌ JSON解析失败: {}", jsonEx.getMessage(), jsonEx);
                    log.info("使用默认任务作为降级方案");
                    tasks = generateDefaultTasks(userId);
                }
            } else {
                log.info("⚠️ AI服务未配置或响应为空，使用默认任务");
                tasks = generateDefaultTasks(userId);
            }
        } catch (Exception e) {
            log.error("❌ AI生成任务异常: {}", e.getMessage(), e);
            log.info("使用默认任务作为降级方案");
            tasks = generateDefaultTasks(userId);
        }

        log.info("========== AI生成任务结束，返回 {} 个任务 ==========", tasks.size());
        return tasks;
    }

    @Override
    @Transactional
    public boolean completeTask(Integer userId, Integer taskId) {
        log.info("完成任务，用户ID: {}, 任务ID: {}", userId, taskId);
        DailyTask task = dailyTaskMapper.selectById(taskId);
        if (task != null && task.getUserId().equals(userId)) {
            task.setIsCompleted(1);
            task.setCompletedTime(LocalDateTime.now());
            dailyTaskMapper.updateById(task);
            log.info("任务完成成功");
            return true;
        }
        log.warn("任务不存在或无权操作");
        return false;
    }

    @Override
    @Transactional
    public CountdownVO addCountdown(Integer userId, CountdownForm form) {
        log.info("添加倒计时，用户ID: {}, 事件名称: {}", userId, form.getEventName());
        
        CountdownConfig config = new CountdownConfig();
        config.setUserId(userId);
        config.setEventName(form.getEventName());
        config.setTargetDate(form.getTargetDate());
        config.setIsActive(1);

        countdownConfigMapper.insert(config);
        log.info("倒计时添加成功，ID: {}", config.getId());

        return convertToCountdownVO(config);
    }

    @Override
    public List<CountdownVO> getCountdowns(Integer userId) {
        QueryWrapper<CountdownConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("is_active", 1);
        wrapper.orderByDesc("target_date");

        List<CountdownConfig> configs = countdownConfigMapper.selectList(wrapper);
        return configs.stream().map(this::convertToCountdownVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteCountdown(Integer userId, Integer countdownId) {
        log.info("删除倒计时，用户ID: {}, 倒计时ID: {}", userId, countdownId);
        CountdownConfig config = countdownConfigMapper.selectById(countdownId);
        if (config != null && config.getUserId().equals(userId)) {
            countdownConfigMapper.deleteById(countdownId);
            log.info("倒计时删除成功");
            return true;
        }
        log.warn("倒计时不存在或无权操作");
        return false;
    }

    @Override
    public String getDailyJoke(Integer userId) {
        log.info("获取每日一笑，用户ID: {}", userId);
        try {
            String prompt = "请生成一个简短有趣的笑话，适合学生群体，积极健康，不要涉及敏感内容。";
            String joke = callAIAPI(prompt);
            if (joke != null && !joke.isEmpty()) {
                log.info("获取每日一笑成功");
                return joke.trim();
            }
        } catch (Exception e) {
            log.error("获取每日一笑失败: {}", e.getMessage(), e);
        }
        return "为什么程序员总是分不清圣诞节和万圣节？因为Dec 25等于Oct 31！";
    }

    @Override
    public Object getWrongAnalysis(Integer userId) {
        return getWrongAnalysisData(userId);
    }

    // ========== 私有辅助方法 ==========

    private List<StudyDurationVO> getStudyDurationTrendData(Integer userId) {
        List<StudyDurationVO> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            StudyDurationVO vo = new StudyDurationVO();
            vo.setDate(date.format(DateTimeFormatter.ofPattern("MM-dd")));

            QueryWrapper<UserDailyLoginDuration> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.eq("login_date", date);

            UserDailyLoginDuration duration = userDailyLoginDurationMapper.selectOne(wrapper);
            int seconds = duration != null ? duration.getTotalSeconds() : 0;
            int minutes = seconds / 60;

            vo.setDurationMinutes(minutes);
            vo.setDurationHours(Math.round(minutes / 60.0 * 10.0) / 10.0);

            trend.add(vo);
        }

        return trend;
    }

    private List<TaskVO> getTodayTasks(Integer userId) {
        QueryWrapper<DailyTask> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("task_date", LocalDate.now());
        wrapper.orderByAsc("priority");

        return dailyTaskMapper.selectList(wrapper).stream()
                .map(this::convertToTaskVO)
                .collect(Collectors.toList());
    }

    private WrongAnalysisVO getWrongAnalysisData(Integer userId) {
        WrongAnalysisVO analysis = new WrongAnalysisVO();

        try {
            // 查询用户的所有错题记录
            QueryWrapper<UserExerciseRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.eq("is_correct", 0);
            wrapper.orderByDesc("create_time"); // 按时间倒序

            List<UserExerciseRecord> wrongRecords = userExerciseRecordMapper.selectList(wrapper);
            analysis.setTotalWrongCount(wrongRecords.size());

            List<WeakPointVO> weakPoints = new ArrayList<>();
            
            if (wrongRecords.size() > 0) {
                // 真实统计：按知识点分组统计错误次数
                Map<String, Map<String, Object>> knowledgeStats = new HashMap<>();
                
                for (UserExerciseRecord record : wrongRecords) {
                    // 从错题记录中获取知识点信息
                    String knowledgePoint = record.getKnowledgePoint();
                    String subject = record.getSubject();
                    
                    if (knowledgePoint == null || knowledgePoint.isEmpty()) {
                        knowledgePoint = "未分类";
                    }
                    if (subject == null || subject.isEmpty()) {
                        subject = "综合科目";
                    }
                    
                    Map<String, Object> stats = knowledgeStats.computeIfAbsent(knowledgePoint, k -> new HashMap<>());
                    stats.put("knowledgePoint", knowledgePoint);
                    stats.put("subject", subject);
                    stats.put("wrongCount", ((Integer)stats.getOrDefault("wrongCount", 0)) + 1);
                }
                
                // 按错误次数排序，取前5个薄弱知识点
                knowledgeStats.entrySet().stream()
                        .sorted((e1, e2) -> {
                            Integer count1 = (Integer) e1.getValue().get("wrongCount");
                            Integer count2 = (Integer) e2.getValue().get("wrongCount");
                            return count2.compareTo(count1);
                        })
                        .limit(5)
                        .forEach(entry -> {
                            Map<String, Object> stats = entry.getValue();
                            WeakPointVO point = new WeakPointVO();
                            point.setKnowledgePointName((String) stats.get("knowledgePoint"));
                            point.setSubject((String) stats.get("subject"));
                            point.setWrongCount((Integer) stats.get("wrongCount"));
                            // 计算掌握度：错题越少，掌握度越高
                            int masteryRate = Math.max(20, 100 - (Integer) stats.get("wrongCount") * 15);
                            point.setMasteryRate((double) masteryRate);
                            point.setSuggestion(generateStudySuggestion((String) stats.get("subject"), (String) stats.get("knowledgePoint")));
                            weakPoints.add(point);
                        });
            }
            
            analysis.setWeakPoints(weakPoints);
            analysis.setAiSuggestion(generateAiAnalysisSuggestion(userId, weakPoints));

        } catch (Exception e) {
            log.error("获取错题分析失败: {}", e.getMessage(), e);
            analysis.setTotalWrongCount(0);
            analysis.setWeakPoints(new ArrayList<>());
            analysis.setAiSuggestion("暂无错题数据");
        }

        return analysis;
    }
    
    private String generateStudySuggestion(String subject, String knowledgePoint) {
        if (knowledgePoint == null || knowledgePoint.isEmpty()) {
            return "建议复习相关知识点，多做练习题巩固";
        }
        return "建议重点复习" + knowledgePoint + "，加强练习相关题目";
    }

    private UserInfoVO getUserInfo(Integer userId) {
        UserInfoVO info = new UserInfoVO();

        User user = userMapper.selectById(userId);
        if (user != null) {
            info.setUserId(userId);
            info.setUsername(user.getUserName());
            info.setAvatarUrl(user.getAvatar());
        }

        UserStudyGoal goal = userStudyGoalMapper.selectOne(new QueryWrapper<UserStudyGoal>().eq("user_id", userId));
        if (goal != null) {
            info.setExamType(goal.getExamType());
            info.setTargetYear(goal.getTargetYear());
        }

        info.setStreakDays(calculateStreakDays(userId));
        info.setTodayStudyMinutes(getTodayStudyMinutes(userId));

        return info;
    }

    private TaskVO convertToTaskVO(DailyTask task) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setSubject(task.getSubject());
        vo.setTaskContent(task.getTaskContent());
        vo.setEstimatedMinutes(task.getEstimatedMinutes());
        vo.setPriority(task.getPriority());
        vo.setPriorityText(task.getPriority() == 1 ? "高" : task.getPriority() == 2 ? "中" : "低");
        vo.setIsCompleted(task.getIsCompleted());
        vo.setCompletedTime(task.getCompletedTime());
        vo.setAiSuggestion(task.getAiSuggestion());
        return vo;
    }

    private CountdownVO convertToCountdownVO(CountdownConfig config) {
        CountdownVO vo = new CountdownVO();
        vo.setId(config.getId());
        vo.setEventName(config.getEventName());
        vo.setTargetDate(config.getTargetDate());
        vo.setIsActive(config.getIsActive());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetDateTime = LocalDateTime.of(config.getTargetDate(), LocalTime.MAX);
        long secondsDiff = java.time.Duration.between(now, targetDateTime).getSeconds();

        if (secondsDiff <= 0) {
            vo.setRemainingDays(0);
            vo.setRemainingHours(0);
            vo.setRemainingMinutes(0);
            vo.setRemainingSeconds(0);
        } else {
            vo.setRemainingDays((int) (secondsDiff / (24 * 60 * 60)));
            vo.setRemainingHours((int) ((secondsDiff % (24 * 60 * 60)) / (60 * 60)));
            vo.setRemainingMinutes((int) ((secondsDiff % (60 * 60)) / 60));
            vo.setRemainingSeconds((int) (secondsDiff % 60));
        }

        return vo;
    }

    private String buildTaskGenerationPrompt(Integer userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据学生的学习情况生成今日学习任务计划。\n");
        prompt.append("要求：\n");
        prompt.append("1. 返回JSON格式，包含tasks数组\n");
        prompt.append("2. 每个任务包含：subject(科目), content(任务内容), durationMinutes(预计时长分钟), priority(优先级1-高2-中3-低), suggestion(建议)\n");
        prompt.append("3. 生成3-5个任务\n");
        prompt.append("4. 涵盖数学、英语、专业课等主要科目\n");
        prompt.append("5. 任务要具体可行\n");
        prompt.append("示例格式：{\"tasks\":[{\"subject\":\"数学\",\"content\":\"完成导数练习题20道\",\"durationMinutes\":45,\"priority\":1,\"suggestion\":\"重点练习复合函数求导\"}]}\n");
        return prompt.toString();
    }

    private List<TaskVO> generateDefaultTasks(Integer userId) {
        List<TaskVO> tasks = new ArrayList<>();
        String[][] defaultTasks = {
                {"数学", "完成高等数学练习题15道", "45", "1", "重点练习极限和导数"},
                {"英语", "阅读英文文章2篇并完成题目", "30", "2", "注意理解长难句"},
                {"专业课", "复习专业课笔记一章", "60", "1", "整理重点知识点"},
                {"政治", "背诵马原知识点", "20", "3", "结合例题理解"}
        };

        for (String[] taskData : defaultTasks) {
            DailyTask task = new DailyTask();
            task.setUserId(userId);
            task.setTaskDate(LocalDate.now());
            task.setSubject(taskData[0]);
            task.setTaskContent(taskData[1]);
            task.setEstimatedMinutes(Integer.parseInt(taskData[2]));
            task.setPriority(Integer.parseInt(taskData[3]));
            task.setIsCompleted(0);
            task.setAiSuggestion(taskData[4]);

            dailyTaskMapper.insert(task);
            tasks.add(convertToTaskVO(task));
        }

        return tasks;
    }

    private String generateAiAnalysisSuggestion(Integer userId, List<WeakPointVO> weakPoints) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("根据以下薄弱知识点分析，为学生提供学习建议：\n");
            for (WeakPointVO point : weakPoints) {
                prompt.append("知识点：").append(point.getKnowledgePointName());
                prompt.append("，错误次数：").append(point.getWrongCount()).append("\n");
            }
            prompt.append("请给出具体的改进建议，建议要实用可行，不超过200字。");

            String result = callAIAPI(prompt.toString());
            return result != null && !result.isEmpty() ? result.trim() : 
                   "建议针对薄弱知识点加强练习，多做相关题目，理解概念本质。";
        } catch (Exception e) {
            log.error("生成AI分析建议失败: {}", e.getMessage(), e);
            return "建议针对薄弱知识点加强练习，多做相关题目，理解概念本质。";
        }
    }

    private String callAIAPI(String prompt) {
        Result<AiConfig> configResult = aiConfigService.resolveConfig(null);
        if (configResult.getCode() != 1 || configResult.getData() == null) {
            log.warn("AI服务未配置，使用模拟响应");
            return "";
        }
        AiConfig activeConfig = configResult.getData();

        try {
            String url = activeConfig.getBaseUrl() + "/chat/completions";
            log.info("调用AI API，URL: {}", url);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", activeConfig.getModel());

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            requestBody.put("messages", messages);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("max_tokens", 2000);
            parameters.put("temperature", 0.7);
            requestBody.put("parameters", parameters);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + activeConfig.getApiKey());

            log.info("AI请求体: {}", JSON.toJSONString(requestBody));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                log.info("AI API响应成功，响应长度: {} 字符", responseBody != null ? responseBody.length() : 0);
                log.debug("AI API响应内容: {}", responseBody);

                JSONObject jsonResponse = JSON.parseObject(responseBody);

                if (jsonResponse.containsKey("choices")) {
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty()) {
                        JSONObject choice = choices.getJSONObject(0);
                        if (choice.containsKey("message")) {
                            JSONObject message = choice.getJSONObject("message");
                            if (message.containsKey("content")) {
                                return message.getString("content");
                            }
                        } else if (choice.containsKey("text")) {
                            return choice.getString("text");
                        }
                    }
                } else if (jsonResponse.containsKey("output")) {
                    return jsonResponse.getString("output");
                } else if (jsonResponse.containsKey("result")) {
                    return jsonResponse.getString("result");
                }

                return responseBody;
            }

            log.error("AI API调用失败，状态码: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("调用AI API异常: {}", e.getMessage(), e);
        }

        return "";
    }

    private Integer calculateStreakDays(Integer userId) {
        int streak = 0;
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 365; i++) {
            LocalDate date = today.minusDays(i);
            QueryWrapper<UserDailyLoginDuration> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.eq("login_date", date);

            if (userDailyLoginDurationMapper.selectCount(wrapper) > 0) {
                streak++;
            } else if (i > 0) {
                break;
            }
        }

        return streak;
    }

    private Integer getTodayStudyMinutes(Integer userId) {
        QueryWrapper<UserDailyLoginDuration> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("login_date", LocalDate.now());

        UserDailyLoginDuration duration = userDailyLoginDurationMapper.selectOne(wrapper);
        return duration != null ? duration.getTotalSeconds() / 60 : 0;
    }
}