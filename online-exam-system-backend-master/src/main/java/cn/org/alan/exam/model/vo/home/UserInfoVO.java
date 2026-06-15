package cn.org.alan.exam.model.vo.home;

import lombok.Data;

/**
 * 用户信息VO
 */
@Data
public class UserInfoVO {
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 目标考试类型
     */
    private String examType;
    
    /**
     * 目标年份
     */
    private Integer targetYear;
    
    /**
     * 连续学习天数
     */
    private Integer streakDays;
    
    /**
     * 今日学习时长（分钟）
     */
    private Integer todayStudyMinutes;
}