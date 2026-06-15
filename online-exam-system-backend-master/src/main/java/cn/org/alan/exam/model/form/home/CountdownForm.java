package cn.org.alan.exam.model.form.home;

import lombok.Data;

import java.time.LocalDate;

/**
 * 添加倒计时表单
 */
@Data
public class CountdownForm {
    
    /**
     * 倒计时名称
     */
    private String eventName;
    
    /**
     * 目标日期
     */
    private LocalDate targetDate;
}