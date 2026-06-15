package cn.org.alan.exam.model.vo.home;

import lombok.Data;

/**
 * 学习时长数据VO
 */
@Data
public class StudyDurationVO {
    
    /**
     * 日期（格式：MM-dd）
     */
    private String date;
    
    /**
     * 学习时长（分钟）
     */
    private Integer durationMinutes;
    
    /**
     * 学习时长（小时，保留1位小数）
     */
    private Double durationHours;
}