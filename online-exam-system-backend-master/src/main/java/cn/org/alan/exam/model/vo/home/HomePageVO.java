package cn.org.alan.exam.model.vo.home;

import lombok.Data;

import java.util.List;

/**
 * 首页数据VO
 */
@Data
public class HomePageVO {
    
    /**
     * 学习时长趋势数据
     */
    private List<StudyDurationVO> studyDurationTrend;
    
    /**
     * 今日任务列表
     */
    private List<TaskVO> todayTasks;
    
    /**
     * 倒计时配置列表
     */
    private List<CountdownVO> countdowns;
    
    /**
     * 每日一笑
     */
    private String dailyJoke;
    
    /**
     * 错题分析结果
     */
    private WrongAnalysisVO wrongAnalysis;
    
    /**
     * 用户基本信息
     */
    private UserInfoVO userInfo;
}