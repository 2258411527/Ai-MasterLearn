package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.vo.home.PersonalizedHomeVO;
import cn.org.alan.exam.service.IPersonalizedHomeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PersonalizedHomeServiceImpl implements IPersonalizedHomeService {

    private static final String CACHE_PREFIX = "home:data:";
    private static final long CACHE_EXPIRE_MINUTES = 10;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserStudyGoalMapper userStudyGoalMapper;

    @Autowired
    private UserKaoyanInfoMapper userKaoyanInfoMapper;

    @Autowired
    private UserStudyAnalysisMapper userStudyAnalysisMapper;

    @Autowired
    private UserDailyLoginDurationMapper userDailyLoginDurationMapper;

    // TODO: UserTaskMapper 对应的实体类和Mapper尚未实现
    // @Autowired
    // private UserTaskMapper userTaskMapper;

    // TODO: UserCountdownMapper 对应的实体类和Mapper尚未实现
    // @Autowired
    // private UserCountdownMapper userCountdownMapper;

    // TODO: RepWrongQuestionMapper 对应的实体类和Mapper尚未实现
    // @Autowired
    // private RepWrongQuestionMapper repWrongQuestionMapper;

    // TODO: ExamRecordMapper 对应的实体类和Mapper尚未实现
    // @Autowired
    // private ExamRecordMapper examRecordMapper;

    @Override
    public Result<PersonalizedHomeVO> getPersonalizedHomeData(Integer userId) {
        try {
            String cacheKey = CACHE_PREFIX + userId;
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            
            if (cachedData != null) {
                PersonalizedHomeVO cached = objectMapper.readValue(cachedData, PersonalizedHomeVO.class);
                long remaining = stringRedisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
                cached.setCacheExpireTime(System.currentTimeMillis() + remaining * 1000);
                log.info("从缓存获取首页数据，用户ID: {}", userId);
                return Result.success(cached);
            }

            PersonalizedHomeVO data = buildHomeData(userId);
            
            String jsonData = objectMapper.writeValueAsString(data);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            data.setCacheExpireTime(System.currentTimeMillis() + CACHE_EXPIRE_MINUTES * 60 * 1000);
            
            log.info("构建并缓存首页数据，用户ID: {}", userId);
            return Result.success(data);
            
        } catch (Exception e) {
            log.error("获取首页数据失败", e);
            return Result.failed("获取数据失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> refreshCache(Integer userId) {
        try {
            clearCacheInternal(userId);
            getPersonalizedHomeData(userId);
            return Result.success("缓存已刷新");
        } catch (Exception e) {
            log.error("刷新缓存失败", e);
            return Result.failed("刷新失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> clearCache(Integer userId) {
        try {
            clearCacheInternal(userId);
            return Result.success("缓存已清除");
        } catch (Exception e) {
            log.error("清除缓存失败", e);
            return Result.failed("清除失败：" + e.getMessage());
        }
    }

    private void clearCacheInternal(Integer userId) {
        String cacheKey = CACHE_PREFIX + userId;
        stringRedisTemplate.delete(cacheKey);
    }

    private PersonalizedHomeVO buildHomeData(Integer userId) {
        PersonalizedHomeVO home = new PersonalizedHomeVO();
        
        home.setUserInfo(buildUserInfo(userId));
        home.setRecommendations(buildRecommendations(userId));
        home.setStudyStats(buildStudyStats(userId));
        home.setStudyTrend(buildStudyTrend(userId));
        home.setTodayTasks(buildTodayTasks(userId));
        home.setCountdowns(buildCountdowns(userId));
        home.setWeakAnalysis(buildWeakAnalysis(userId));
        home.setDailyQuote(getDailyQuote());
        
        return home;
    }

    private PersonalizedHomeVO.UserInfo buildUserInfo(Integer userId) {
        PersonalizedHomeVO.UserInfo info = new PersonalizedHomeVO.UserInfo();
        
        User user = userMapper.selectById(userId);
        if (user != null) {
            info.setUsername(user.getUserName());
            info.setAvatarUrl(user.getAvatar());
        }
        
        UserStudyGoal goal = userStudyGoalMapper.selectOne(
            new LambdaQueryWrapper<UserStudyGoal>().eq(UserStudyGoal::getUserId, userId)
        );
        
        if (goal != null) {
            info.setExamType(goal.getExamType());
            info.setTargetYear(String.valueOf(goal.getTargetYear()));
            info.setStudyIdentity(goal.getStudyIdentity());
            
            UserKaoyanInfo kaoyan = userKaoyanInfoMapper.selectOne(
                new LambdaQueryWrapper<UserKaoyanInfo>().eq(UserKaoyanInfo::getUserId, userId)
            );
            if (kaoyan != null) {
                info.setTargetUniversityLevel(kaoyan.getTargetUniversityLevel());
                info.setDegreeType(kaoyan.getDegreeType());
                info.setMajorType(kaoyan.getMajorType());
            }
        }
        
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<UserDailyLoginDuration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDailyLoginDuration::getUserId, userId)
               .eq(UserDailyLoginDuration::getLoginDate, today);
        UserDailyLoginDuration duration = userDailyLoginDurationMapper.selectOne(wrapper);
        // totalSeconds 是秒数,转换为分钟
        info.setTodayStudyMinutes(duration != null && duration.getTotalSeconds() != null 
            ? duration.getTotalSeconds() / 60 : 0);
        
        info.setStreakDays(calculateStreakDays(userId));
        
        return info;
    }

    private List<PersonalizedHomeVO.RecommendItem> buildRecommendations(Integer userId) {
        List<PersonalizedHomeVO.RecommendItem> items = new ArrayList<>();
        
        UserStudyGoal goal = userStudyGoalMapper.selectOne(
            new LambdaQueryWrapper<UserStudyGoal>().eq(UserStudyGoal::getUserId, userId)
        );
        
        UserKaoyanInfo kaoyan = userKaoyanInfoMapper.selectOne(
            new LambdaQueryWrapper<UserKaoyanInfo>().eq(UserKaoyanInfo::getUserId, userId)
        );
        
        int priority = 1;
        
        items.add(createRecommend(
            priority++, "exam", "开始模拟测试",
            "检验学习成果，查漏补缺",
            "el-icon-document", "#6366F1", "/prepare-exam",
            "基于您的学习进度推荐"
        ));
        
        items.add(createRecommend(
            priority++, "practice", "练习中心",
            "针对性练习，巩固知识点",
            "el-icon-edit-outline", "#8B5CF6", "/exercise-center",
            "每日练习提升解题能力"
        ));
        
        if (goal != null && "考研".equals(goal.getExamType()) && kaoyan != null) {
            String mathSubject = kaoyan.getMathSubject();
            if (mathSubject != null && !mathSubject.contains("不考")) {
                items.add(createRecommend(
                    priority++, "math", mathSubject + "专项",
                    "数学科目重点突破",
                    "el-icon-cpu", "#10B981", "/exercise-center",
                    "根据您的数学科目推荐"
                ));
            }
            
            String weakModules = kaoyan.getWeakModules();
            if (weakModules != null && !weakModules.isEmpty()) {
                items.add(createRecommend(
                    priority++, "weak", "薄弱模块",
                    "针对性复习，快速提分",
                    "el-icon-warning-outline", "#F59E0B", "/wrong-book",
                    "重点攻克薄弱知识点"
                ));
            }
        }
        
        items.add(createRecommend(
            priority++, "wrong", "错题本",
            "错题复习，避免再错",
            "el-icon-document-delete", "#EF4444", "/wrong-book",
            "错题是提分的关键"
        ));
        
        items.add(createRecommend(
            priority++, "ai", "AI学习助手",
            "智能答疑，个性化指导",
            "el-icon-chat-dot-round", "#06B6D4", "/ai-chat",
            "随时为您解答疑问"
        ));
        
        return items;
    }

    private PersonalizedHomeVO.RecommendItem createRecommend(int priority, String type, String title, 
            String desc, String icon, String color, String url, String reason) {
        PersonalizedHomeVO.RecommendItem item = new PersonalizedHomeVO.RecommendItem();
        item.setId(priority);
        item.setType(type);
        item.setTitle(title);
        item.setDescription(desc);
        item.setIcon(icon);
        item.setColor(color);
        item.setActionUrl(url);
        item.setPriority(priority);
        item.setReason(reason);
        return item;
    }

    private PersonalizedHomeVO.StudyStats buildStudyStats(Integer userId) {
        PersonalizedHomeVO.StudyStats stats = new PersonalizedHomeVO.StudyStats();
        
        LocalDate weekStart = LocalDate.now().minusDays(6);
        LambdaQueryWrapper<UserDailyLoginDuration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDailyLoginDuration::getUserId, userId)
               .ge(UserDailyLoginDuration::getLoginDate, weekStart);
        List<UserDailyLoginDuration> durations = userDailyLoginDurationMapper.selectList(wrapper);
        
        int totalMinutes = durations.stream()
            .mapToInt(d -> d.getTotalSeconds() != null ? d.getTotalSeconds() / 60 : 0)
            .sum();
        stats.setWeeklyStudyHours(Math.round(totalMinutes / 60.0 * 10) / 10.0);
        
        // TODO: UserTaskMapper 尚未实现
        // LocalDate today = LocalDate.now();
        // LambdaQueryWrapper<UserTask> taskWrapper = new LambdaQueryWrapper<>();
        // taskWrapper.eq(UserTask::getUserId, userId)
        //            .eq(UserTask::getTaskDate, today);
        // List<UserTask> tasks = userTaskMapper.selectList(taskWrapper);
        // 
        // int completed = (int) tasks.stream().filter(t -> t.getIsCompleted() == 1).count();
        // stats.setCompletedTasks(completed);
        // stats.setTotalTasks(tasks.size());
        stats.setCompletedTasks(0);
        stats.setTotalTasks(0);
        
        // TODO: RepWrongQuestionMapper 尚未实现
        // LambdaQueryWrapper<RepWrongQuestion> wrongWrapper = new LambdaQueryWrapper<>();
        // wrongWrapper.eq(RepWrongQuestion::getUserId, userId);
        // Long wrongCount = repWrongQuestionMapper.selectCount(wrongWrapper);
        // stats.setWrongCount(wrongCount != null ? wrongCount.intValue() : 0);
        stats.setWrongCount(0);
        
        UserStudyGoal goal = userStudyGoalMapper.selectOne(
            new LambdaQueryWrapper<UserStudyGoal>().eq(UserStudyGoal::getUserId, userId)
        );
        
        if (goal != null && goal.getTargetYear() != null) {
            LocalDate now = LocalDate.now();
            LocalDate target = LocalDate.of(goal.getTargetYear(), 12, 25);
            LocalDate start = LocalDate.of(now.getYear(), 1, 1);
            
            long totalDays = ChronoUnit.DAYS.between(start, target);
            long elapsedDays = ChronoUnit.DAYS.between(start, now);
            
            if (totalDays > 0) {
                stats.setGoalProgress((int) Math.min(100, (elapsedDays * 100 / totalDays)));
            } else {
                stats.setGoalProgress(100);
            }
        } else {
            stats.setGoalProgress(0);
        }
        
        return stats;
    }

    private List<Map<String, Object>> buildStudyTrend(Integer userId) {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString().substring(5));
            
            LambdaQueryWrapper<UserDailyLoginDuration> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserDailyLoginDuration::getUserId, userId)
                   .eq(UserDailyLoginDuration::getLoginDate, date);
            UserDailyLoginDuration duration = userDailyLoginDurationMapper.selectOne(wrapper);
            // totalSeconds 是秒数,转换为分钟
            item.put("durationMinutes", duration != null && duration.getTotalSeconds() != null 
                ? duration.getTotalSeconds() / 60 : 0);
            trend.add(item);
        }
        
        return trend;
    }

    private List<PersonalizedHomeVO.TaskItem> buildTodayTasks(Integer userId) {
        List<PersonalizedHomeVO.TaskItem> result = new ArrayList<>();
        
        // TODO: UserTaskMapper 尚未实现
        // LocalDate today = LocalDate.now();
        // LambdaQueryWrapper<UserTask> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(UserTask::getUserId, userId)
        //        .eq(UserTask::getTaskDate, today)
        //        .orderByAsc(UserTask::getPriority);
        // List<UserTask> tasks = userTaskMapper.selectList(wrapper);
        // List<UserTask> tasks = new ArrayList<>();
        // 
        // for (UserTask task : tasks) {
        //     PersonalizedHomeVO.TaskItem item = new PersonalizedHomeVO.TaskItem();
        //     item.setId(task.getId());
        //     item.setTaskContent(task.getTaskContent());
        //     item.setSubject(task.getSubject());
        //     item.setIsCompleted(task.getIsCompleted());
        //     item.setPriority(task.getPriority());
        //     item.setEstimatedMinutes(task.getEstimatedMinutes());
        //     result.add(item);
        // }
        // 临时返回空列表
        
        return result;
    }

    private List<PersonalizedHomeVO.CountdownItem> buildCountdowns(Integer userId) {
        List<PersonalizedHomeVO.CountdownItem> result = new ArrayList<>();
        
        // TODO: UserCountdownMapper 尚未实现
        // LambdaQueryWrapper<UserCountdown> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(UserCountdown::getUserId, userId)
        //        .ge(UserCountdown::getTargetDate, LocalDate.now())
        //        .orderByAsc(UserCountdown::getTargetDate);
        // List<UserCountdown> countdowns = userCountdownMapper.selectList(wrapper);
        // List<UserCountdown> countdowns = new ArrayList<>();
        // 
        // LocalDateTime now = LocalDateTime.now();
        // for (UserCountdown c : countdowns) {
        //     PersonalizedHomeVO.CountdownItem item = new PersonalizedHomeVO.CountdownItem();
        //     item.setId(c.getId());
        //     item.setEventName(c.getEventName());
        //     item.setTargetDate(c.getTargetDate().toString());
        //     
        //     LocalDateTime target = c.getTargetDate().atTime(0, 0);
        //     long totalMinutes = ChronoUnit.MINUTES.between(now, target);
        //     
        //     if (totalMinutes > 0) {
        //         item.setRemainingDays(totalMinutes / (24 * 60));
        //         item.setRemainingHours((totalMinutes % (24 * 60)) / 60);
        //         item.setRemainingMinutes(totalMinutes % 60);
        //     } else {
        //         item.setRemainingDays(0L);
        //         item.setRemainingHours(0L);
        //         item.setRemainingMinutes(0L);
        //     }
        //     
        //     result.add(item);
        // }
        // 临时返回空列表
        
        return result;
    }

    private PersonalizedHomeVO.WeakAnalysis buildWeakAnalysis(Integer userId) {
        PersonalizedHomeVO.WeakAnalysis analysis = new PersonalizedHomeVO.WeakAnalysis();
        
        // TODO: RepWrongQuestionMapper 尚未实现
        // LambdaQueryWrapper<RepWrongQuestion> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(RepWrongQuestion::getUserId, userId);
        // Long wrongCount = repWrongQuestionMapper.selectCount(wrapper);
        // analysis.setTotalWrongCount(wrongCount != null ? wrongCount.intValue() : 0);
        analysis.setTotalWrongCount(0);
        
        List<PersonalizedHomeVO.WeakPoint> weakPoints = new ArrayList<>();
        
        // if (wrongCount != null && wrongCount > 0) {
        //     PersonalizedHomeVO.WeakPoint point = new PersonalizedHomeVO.WeakPoint();
        //     point.setSubject("综合");
        //     point.setKnowledgePointName("错题知识点");
        //     point.setWrongCount(wrongCount.intValue());
        //     point.setMasteryRate(Math.max(0, 100 - wrongCount.intValue() * 5));
        //     weakPoints.add(point);
        // }
        
        analysis.setWeakPoints(weakPoints);
        
        UserStudyGoal goal = userStudyGoalMapper.selectOne(
            new LambdaQueryWrapper<UserStudyGoal>().eq(UserStudyGoal::getUserId, userId)
        );
        
        if (goal != null && "考研".equals(goal.getExamType())) {
            analysis.setAiSuggestion("建议每天复习错题，保持学习连贯性，坚持就是胜利！");
        } else {
            analysis.setAiSuggestion("继续保持学习热情，每天进步一点点！");
        }
        
        return analysis;
    }

    private String getDailyQuote() {
        String[] quotes = {
            "生活就像海洋，只有意志坚强的人才能到达彼岸。",
            "世上无难事，只要肯登攀。",
            "宝剑锋从磨砺出，梅花香自苦寒来。",
            "书山有路勤为径，学海无涯苦作舟。",
            "不积跬步，无以至千里；不积小流，无以成江海。",
            "锲而不舍，金石可镂。",
            "业精于勤荒于嬉，行成于思毁于随。"
        };
        
        int dayOfYear = LocalDate.now().getDayOfYear();
        return quotes[dayOfYear % quotes.length];
    }

    private Integer calculateStreakDays(Integer userId) {
        int streak = 0;
        LocalDate date = LocalDate.now();
        
        while (true) {
            LambdaQueryWrapper<UserDailyLoginDuration> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserDailyLoginDuration::getUserId, userId)
                   .eq(UserDailyLoginDuration::getLoginDate, date);
            UserDailyLoginDuration duration = userDailyLoginDurationMapper.selectOne(wrapper);
            
            if (duration != null && duration.getTotalSeconds() != null 
                && duration.getTotalSeconds() > 0) {
                streak++;
                date = date.minusDays(1);
            } else {
                break;
            }
            
            if (streak > 365) break;
        }
        
        return streak;
    }
}
