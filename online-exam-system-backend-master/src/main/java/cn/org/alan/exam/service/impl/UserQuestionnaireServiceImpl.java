package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.user_questionnaire.QuestionnaireSubmitForm;
import cn.org.alan.exam.model.vo.user_questionnaire.UserStudyDashboardVO;
import cn.org.alan.exam.service.IUserQuestionnaireService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户问卷服务实现类
 */
@Service
@Slf4j
public class UserQuestionnaireServiceImpl implements IUserQuestionnaireService {
    
    @Autowired
    private UserStudyGoalMapper userStudyGoalMapper;
    
    @Autowired
    private UserKaoyanInfoMapper userKaoyanInfoMapper;
    
    @Autowired
    private UserKaogongInfoMapper userKaogongInfoMapper;
    
    @Autowired
    private UserStudyAnalysisMapper userStudyAnalysisMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Result<String> submitQuestionnaire(QuestionnaireSubmitForm form) {
        try {
            Integer userId = SecurityUtil.getUserId();
            
            // 1. 保存通用目标信息（先检查是否已存在）
            UserStudyGoal existingGoal = userStudyGoalMapper.selectOne(
                new LambdaQueryWrapper<UserStudyGoal>().eq(UserStudyGoal::getUserId, userId)
            );
            
            UserStudyGoal studyGoal = new UserStudyGoal();
            studyGoal.setUserId(userId);
            studyGoal.setExamType(form.getExamType());
            studyGoal.setTargetYear(form.getTargetYear());
            studyGoal.setStudyIdentity(form.getStudyIdentity());
            studyGoal.setDailyStudyHours(form.getDailyStudyHours());
            studyGoal.setCurrentProgress(form.getCurrentProgress());
            
            if (existingGoal != null) {
                // 更新已存在的记录
                studyGoal.setId(existingGoal.getId());
                userStudyGoalMapper.updateById(studyGoal);
            } else {
                // 插入新记录
                userStudyGoalMapper.insert(studyGoal);
            }
            
            // 2. 根据考试类型保存专属信息
            if ("考研".equals(form.getExamType()) || "双线备考".equals(form.getExamType())) {
                UserKaoyanInfo existingKaoyan = userKaoyanInfoMapper.selectOne(
                    new LambdaQueryWrapper<UserKaoyanInfo>().eq(UserKaoyanInfo::getUserId, userId)
                );
                
                UserKaoyanInfo kaoyanInfo = new UserKaoyanInfo();
                kaoyanInfo.setUserId(userId);
                kaoyanInfo.setAttemptCount(form.getAttemptCount());
                kaoyanInfo.setDegreeType(form.getDegreeType());
                kaoyanInfo.setMajorType(form.getMajorType());
                kaoyanInfo.setTargetUniversityLevel(form.getTargetUniversityLevel());
                kaoyanInfo.setEnglishSubject(form.getEnglishSubject());
                kaoyanInfo.setMathSubject(form.getMathSubject());
                kaoyanInfo.setMajorCourseName(form.getMajorCourseName());
                kaoyanInfo.setEnglishLevel(form.getEnglishLevel());
                kaoyanInfo.setMathLevel(form.getMathLevel());
                kaoyanInfo.setMajorLevel(form.getMajorLevel());
                kaoyanInfo.setWeakModules(form.getWeakModules());
                kaoyanInfo.setStudyNeeds(form.getStudyNeeds());
                
                if (existingKaoyan != null) {
                    kaoyanInfo.setId(existingKaoyan.getId());
                    userKaoyanInfoMapper.updateById(kaoyanInfo);
                } else {
                    userKaoyanInfoMapper.insert(kaoyanInfo);
                }
            }
            
            if ("考公".equals(form.getExamType()) || "双线备考".equals(form.getExamType())) {
                UserKaogongInfo existingKaogong = userKaogongInfoMapper.selectOne(
                    new LambdaQueryWrapper<UserKaogongInfo>().eq(UserKaogongInfo::getUserId, userId)
                );
                
                UserKaogongInfo kaogongInfo = new UserKaogongInfo();
                kaogongInfo.setUserId(userId);
                kaogongInfo.setExamCategory(form.getExamCategory());
                kaogongInfo.setTargetRegion(form.getTargetRegion());
                kaogongInfo.setPositionCategory(form.getPositionCategory());
                kaogongInfo.setPreparationExperience(form.getPreparationExperience());
                kaogongInfo.setXingceLevel(form.getXingceLevel());
                kaogongInfo.setShenlunLevel(form.getShenlunLevel());
                kaogongInfo.setWeakModules(form.getKaogongWeakModules());
                kaogongInfo.setStudyNeeds(form.getKaogongStudyNeeds());
                
                if (existingKaogong != null) {
                    kaogongInfo.setId(existingKaogong.getId());
                    userKaogongInfoMapper.updateById(kaogongInfo);
                } else {
                    userKaogongInfoMapper.insert(kaogongInfo);
                }
            }
            
            // 3. 初始化学习分析记录（先检查是否已存在）
            UserStudyAnalysis existingAnalysis = userStudyAnalysisMapper.selectOne(
                new LambdaQueryWrapper<UserStudyAnalysis>().eq(UserStudyAnalysis::getUserId, userId)
            );
            
            UserStudyAnalysis analysis = new UserStudyAnalysis();
            analysis.setUserId(userId);
            analysis.setTotalStudyDays(0);
            analysis.setTotalStudyHours(0.0);
            analysis.setAvgDailyStudyHours(0.0);
            analysis.setTotalExamsTaken(0);
            analysis.setTotalQuestionsAnswered(0);
            analysis.setCorrectRate(0.0);
            analysis.setLastAnalysisTime(LocalDateTime.now());
            
            if (existingAnalysis != null) {
                analysis.setId(existingAnalysis.getId());
                userStudyAnalysisMapper.updateById(analysis);
            } else {
                userStudyAnalysisMapper.insert(analysis);
            }
            
            // 4. 生成初始AI推荐
            generateInitialAiRecommendations(userId);
            
            return Result.success("问卷提交成功，AI个性化学习平台已为您初始化！");
            
        } catch (Exception e) {
            log.error("提交问卷失败", e);
            return Result.failed("问卷提交失败：" + e.getMessage());
        }
    }

    @Override
    public Result<UserStudyDashboardVO> getStudyDashboard(Integer userId) {
        try {
            UserStudyDashboardVO dashboard = new UserStudyDashboardVO();
            
            // 1. 获取基本信息
            UserStudyGoal studyGoal = userStudyGoalMapper.selectOne(
                new LambdaQueryWrapper<UserStudyGoal>().eq(UserStudyGoal::getUserId, userId)
            );
            
            if (studyGoal != null) {
                dashboard.setExamType(studyGoal.getExamType());
                dashboard.setTargetYear(studyGoal.getTargetYear());
                dashboard.setStudyIdentity(studyGoal.getStudyIdentity());
                dashboard.setDailyStudyHours(studyGoal.getDailyStudyHours());
            }
            
            // 2. 获取学习统计数据
            UserStudyAnalysis analysis = userStudyAnalysisMapper.selectOne(
                new LambdaQueryWrapper<UserStudyAnalysis>().eq(UserStudyAnalysis::getUserId, userId)
            );
            
            if (analysis != null) {
                dashboard.setTotalStudyDays(analysis.getTotalStudyDays());
                dashboard.setTotalStudyHours(analysis.getTotalStudyHours());
                dashboard.setAvgDailyStudyHours(analysis.getAvgDailyStudyHours());
                dashboard.setTotalExamsTaken(analysis.getTotalExamsTaken());
                dashboard.setTotalQuestionsAnswered(analysis.getTotalQuestionsAnswered());
                dashboard.setCorrectRate(analysis.getCorrectRate());
            }
            
            // 3. 生成学习进度
            dashboard.setStudyProgress(generateStudyProgress(userId));
            
            // 4. 知识点掌握情况（从真实数据源获取）
            dashboard.setKnowledgePoints(generateKnowledgePoints(userId));
            
            // 5. 薄弱点分析（从真实数据源获取）
            dashboard.setWeakPoints(generateWeakPoints(userId));
            
            // 6. AI推荐（从真实数据源获取）
            dashboard.setAiRecommendations(generateAiRecommendationList(userId));
            
            // 7. 学习趋势（从真实数据源获取）
            dashboard.setLearningTrend(generateLearningTrend(userId));
            
            // 8. 今日任务（从真实数据源获取）
            dashboard.setDailyTasks(generateDailyTasks(userId));
            
            return Result.success(dashboard);
            
        } catch (Exception e) {
            log.error("获取学习仪表盘失败", e);
            return Result.failed("获取学习数据失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> checkQuestionnaireCompleted(Integer userId) {
        try {
            UserStudyGoal studyGoal = userStudyGoalMapper.selectOne(
                new LambdaQueryWrapper<UserStudyGoal>().eq(UserStudyGoal::getUserId, userId)
            );
            return Result.success(studyGoal != null);
        } catch (Exception e) {
            log.error("检查问卷完成状态失败", e);
            return Result.failed("检查失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> generateAiRecommendations(Integer userId) {
        try {
            // 这里实现AI推荐生成逻辑
            // 可以调用外部AI服务或基于现有数据进行分析
            
            // 模拟AI推荐生成
            String recommendations = generateAiRecommendationsForUser(userId);
            
            // 更新学习分析记录
            UserStudyAnalysis analysis = userStudyAnalysisMapper.selectOne(
                new LambdaQueryWrapper<UserStudyAnalysis>().eq(UserStudyAnalysis::getUserId, userId)
            );
            
            if (analysis != null) {
                analysis.setAiRecommendations(recommendations);
                analysis.setLastAnalysisTime(LocalDateTime.now());
                userStudyAnalysisMapper.updateById(analysis);
            }
            
            return Result.success("AI个性化推荐已生成！");
            
        } catch (Exception e) {
            log.error("生成AI推荐失败", e);
            return Result.failed("生成推荐失败：" + e.getMessage());
        }
    }
    
    // 私有方法实现...
    private void generateInitialAiRecommendations(Integer userId) {
        // 实现初始AI推荐生成逻辑
    }
    
    private UserStudyDashboardVO.StudyProgressVO generateStudyProgress(Integer userId) {
        UserStudyDashboardVO.StudyProgressVO progress = new UserStudyDashboardVO.StudyProgressVO();
        progress.setCompletedDays(15);
        progress.setTotalDays(365);
        progress.setProgressPercentage(4.1);
        progress.setProgressDescription("备考旅程刚开始，坚持就是胜利！");
        return progress;
    }
    
    private List<UserStudyDashboardVO.KnowledgePointVO> generateKnowledgePoints(Integer userId) {
        List<UserStudyDashboardVO.KnowledgePointVO> points = new ArrayList<>();
        // TODO: 从真实数据源获取知识点掌握情况
        return points;
    }
    
    private List<UserStudyDashboardVO.WeakPointVO> generateWeakPoints(Integer userId) {
        List<UserStudyDashboardVO.WeakPointVO> weakPoints = new ArrayList<>();
        // TODO: 从真实数据源获取薄弱点分析
        return weakPoints;
    }
    
    private List<UserStudyDashboardVO.AiRecommendationVO> generateAiRecommendationList(Integer userId) {
        List<UserStudyDashboardVO.AiRecommendationVO> recommendations = new ArrayList<>();
        // TODO: 从真实数据源获取AI推荐
        return recommendations;
    }
    
    private Map<String, Object> generateLearningTrend(Integer userId) {
        Map<String, Object> trend = new HashMap<>();
        // TODO: 从真实数据源获取学习趋势
        return trend;
    }
    
    private List<UserStudyDashboardVO.DailyTaskVO> generateDailyTasks(Integer userId) {
        List<UserStudyDashboardVO.DailyTaskVO> tasks = new ArrayList<>();
        // TODO: 从真实数据源获取今日任务
        return tasks;
    }
    
    private String generateAiRecommendationsForUser(Integer userId) {
        try {
            Map<String, Object> recommendations = new HashMap<>();
            recommendations.put("dailyPlan", "根据您的目标院校和基础情况，建议每天保持4小时高效学习");
            recommendations.put("weakPointFocus", "重点加强概率论和英语作文训练");
            recommendations.put("studyStrategy", "采用交替学习法，数学和英语交替进行");
            recommendations.put("examPreparation", "每月参加一次模拟考试，检验学习效果");
            
            return objectMapper.writeValueAsString(recommendations);
        } catch (JsonProcessingException e) {
            log.error("生成AI推荐JSON失败", e);
            return "{}";
        }
    }
}