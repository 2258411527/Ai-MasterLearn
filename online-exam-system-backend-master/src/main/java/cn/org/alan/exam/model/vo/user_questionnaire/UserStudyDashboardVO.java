package cn.org.alan.exam.model.vo.user_questionnaire;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 用户学习仪表盘VO
 */
@Data
public class UserStudyDashboardVO {
    
    // 基本信息
    private String examType;
    private Integer targetYear;
    private String studyIdentity;
    private String dailyStudyHours;
    
    // 学习统计数据
    private Integer totalStudyDays;
    private Double totalStudyHours;
    private Double avgDailyStudyHours;
    private Integer totalExamsTaken;
    private Integer totalQuestionsAnswered;
    private Double correctRate;
    
    // 学习进度
    private StudyProgressVO studyProgress;
    
    // 知识点掌握情况
    private List<KnowledgePointVO> knowledgePoints;
    
    // 薄弱点分析
    private List<WeakPointVO> weakPoints;
    
    // AI推荐内容
    private List<AiRecommendationVO> aiRecommendations;
    
    // 学习趋势
    private Map<String, Object> learningTrend;
    
    // 今日任务
    private List<DailyTaskVO> dailyTasks;
    
    @Data
    public static class StudyProgressVO {
        private Integer completedDays;
        private Integer totalDays;
        private Double progressPercentage;
        private String progressDescription;
    }
    
    @Data
    public static class KnowledgePointVO {
        private String pointName;
        private Double masteryLevel;
        private Integer questionCount;
        private Integer correctCount;
        private String status;
    }
    
    @Data
    public static class WeakPointVO {
        private String pointName;
        private Double weaknessLevel;
        private String suggestion;
        private Integer priority;
    }
    
    @Data
    public static class AiRecommendationVO {
        private String type;
        private String title;
        private String content;
        private Integer priority;
        private String actionUrl;
        private Boolean isCompleted;
    }
    
    @Data
    public static class DailyTaskVO {
        private String taskName;
        private String description;
        private Integer estimatedTime;
        private Boolean isCompleted;
        private String category;
    }
}