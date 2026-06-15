package cn.org.alan.exam.service;

import cn.org.alan.exam.model.form.home.CountdownForm;
import cn.org.alan.exam.model.form.home.TaskForm;
import cn.org.alan.exam.model.vo.home.CountdownVO;
import cn.org.alan.exam.model.vo.home.HomePageVO;
import cn.org.alan.exam.model.vo.home.TaskVO;

import java.util.List;

/**
 * 首页服务接口
 */
public interface HomeService {
    
    /**
     * 获取首页数据
     * @param userId 用户ID
     * @return 首页数据VO
     */
    HomePageVO getHomePageData(Integer userId);
    
    /**
     * 获取学习时长趋势（最近7天）
     * @param userId 用户ID
     * @return 学习时长列表
     */
    List<Object> getStudyDurationTrend(Integer userId);
    
    /**
     * 添加任务计划
     * @param userId 用户ID
     * @param form 任务表单
     * @return 任务VO
     */
    TaskVO addTask(Integer userId, TaskForm form);
    
    /**
     * AI生成任务计划
     * @param userId 用户ID
     * @return 生成的任务列表
     */
    List<TaskVO> generateTasksByAI(Integer userId);
    
    /**
     * 完成任务
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean completeTask(Integer userId, Integer taskId);
    
    /**
     * 添加倒计时
     * @param userId 用户ID
     * @param form 倒计时表单
     * @return 倒计时VO
     */
    CountdownVO addCountdown(Integer userId, CountdownForm form);
    
    /**
     * 获取倒计时列表
     * @param userId 用户ID
     * @return 倒计时列表
     */
    List<CountdownVO> getCountdowns(Integer userId);
    
    /**
     * 删除倒计时
     * @param userId 用户ID
     * @param countdownId 倒计时ID
     * @return 是否成功
     */
    boolean deleteCountdown(Integer userId, Integer countdownId);
    
    /**
     * 获取每日一笑
     * @param userId 用户ID
     * @return 笑话内容
     */
    String getDailyJoke(Integer userId);
    
    /**
     * 获取错题分析
     * @param userId 用户ID
     * @return 错题分析结果
     */
    Object getWrongAnalysis(Integer userId);
}