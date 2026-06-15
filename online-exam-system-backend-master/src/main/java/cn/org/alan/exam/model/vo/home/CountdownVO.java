package cn.org.alan.exam.model.vo.home;

import lombok.Data;

import java.time.LocalDate;

/**
 * 倒计时VO
 */
@Data
public class CountdownVO {
    
    /**
     * 倒计时ID
     */
    private Integer id;
    
    /**
     * 倒计时名称
     */
    private String eventName;
    
    /**
     * 目标日期
     */
    private LocalDate targetDate;
    
    /**
     * 剩余天数
     */
    private Integer remainingDays;
    
    /**
     * 剩余小时数
     */
    private Integer remainingHours;
    
    /**
     * 剩余分钟数
     */
    private Integer remainingMinutes;
    
    /**
     * 剩余秒数
     */
    private Integer remainingSeconds;
    
    /**
     * 是否激活
     */
    private Integer isActive;
}