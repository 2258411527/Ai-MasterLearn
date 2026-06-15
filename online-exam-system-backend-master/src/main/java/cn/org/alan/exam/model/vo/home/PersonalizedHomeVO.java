package cn.org.alan.exam.model.vo.home;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PersonalizedHomeVO {
    private UserInfo userInfo;
    private List<RecommendItem> recommendations;
    private StudyStats studyStats;
    private List<Map<String, Object>> studyTrend;
    private List<TaskItem> todayTasks;
    private List<CountdownItem> countdowns;
    private WeakAnalysis weakAnalysis;
    private String dailyQuote;
    private Long cacheExpireTime;

    @Data
    public static class UserInfo {
        private String username;
        private String avatarUrl;
        private Integer streakDays;
        private Integer todayStudyMinutes;
        private String examType;
        private String targetYear;
        private String studyIdentity;
        private String targetUniversityLevel;
        private String degreeType;
        private String majorType;
    }

    @Data
    public static class RecommendItem {
        private Integer id;
        private String type;
        private String title;
        private String description;
        private String icon;
        private String color;
        private String actionUrl;
        private Integer priority;
        private String reason;
    }

    @Data
    public static class StudyStats {
        private Double weeklyStudyHours;
        private Integer completedTasks;
        private Integer totalTasks;
        private Integer wrongCount;
        private Integer goalProgress;
    }

    @Data
    public static class TaskItem {
        private Integer id;
        private String taskContent;
        private String subject;
        private Integer isCompleted;
        private Integer priority;
        private Integer estimatedMinutes;
    }

    @Data
    public static class CountdownItem {
        private Integer id;
        private String eventName;
        private String targetDate;
        private Long remainingDays;
        private Long remainingHours;
        private Long remainingMinutes;
    }

    @Data
    public static class WeakAnalysis {
        private Integer totalWrongCount;
        private List<WeakPoint> weakPoints;
        private String aiSuggestion;
    }

    @Data
    public static class WeakPoint {
        private String subject;
        private String knowledgePointName;
        private Integer wrongCount;
        private Integer masteryRate;
    }
}
