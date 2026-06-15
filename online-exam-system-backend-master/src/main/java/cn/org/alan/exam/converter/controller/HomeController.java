package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.home.CountdownForm;
import cn.org.alan.exam.model.form.home.TaskForm;
import cn.org.alan.exam.model.vo.home.CountdownVO;
import cn.org.alan.exam.model.vo.home.HomePageVO;
import cn.org.alan.exam.model.vo.home.TaskVO;
import cn.org.alan.exam.service.HomeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/home")
@Api(tags = "首页模块")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping("/data")
    @ApiOperation("获取首页数据")
    public Result<HomePageVO> getHomePageData(@RequestParam Integer userId) {
        try {
            HomePageVO data = homeService.getHomePageData(userId);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取首页数据失败: {}", e.getMessage(), e);
            return Result.failed("获取首页数据失败");
        }
    }

    @GetMapping("/study-trend")
    @ApiOperation("获取学习时长趋势（最近7天）")
    public Result<List<Object>> getStudyDurationTrend(@RequestParam Integer userId) {
        try {
            List<Object> trend = homeService.getStudyDurationTrend(userId);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取学习时长趋势失败: {}", e.getMessage(), e);
            return Result.failed("获取学习时长趋势失败");
        }
    }

    @PostMapping("/task")
    @ApiOperation("添加任务计划")
    public Result<TaskVO> addTask(@RequestParam Integer userId, @RequestBody TaskForm form) {
        try {
            TaskVO task = homeService.addTask(userId, form);
            return Result.success(task);
        } catch (Exception e) {
            log.error("添加任务失败: {}", e.getMessage(), e);
            return Result.failed("添加任务失败");
        }
    }

    @PostMapping("/task/ai-generate")
    @ApiOperation("AI生成任务计划")
    public Result<List<TaskVO>> generateTasksByAI(@RequestParam Integer userId) {
        try {
            List<TaskVO> tasks = homeService.generateTasksByAI(userId);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("AI生成任务失败: {}", e.getMessage(), e);
            return Result.failed("AI生成任务失败");
        }
    }

    @PutMapping("/task/{taskId}/complete")
    @ApiOperation("完成任务")
    public Result<Boolean> completeTask(@RequestParam Integer userId, @PathVariable Integer taskId) {
        try {
            boolean success = homeService.completeTask(userId, taskId);
            return success ? Result.success(true) : Result.failed("任务不存在或无权操作");
        } catch (Exception e) {
            log.error("完成任务失败: {}", e.getMessage(), e);
            return Result.failed("完成任务失败");
        }
    }

    @PostMapping("/countdown")
    @ApiOperation("添加倒计时")
    public Result<CountdownVO> addCountdown(@RequestParam Integer userId, @RequestBody CountdownForm form) {
        try {
            CountdownVO countdown = homeService.addCountdown(userId, form);
            return Result.success(countdown);
        } catch (Exception e) {
            log.error("添加倒计时失败: {}", e.getMessage(), e);
            return Result.failed("添加倒计时失败");
        }
    }

    @GetMapping("/countdowns")
    @ApiOperation("获取倒计时列表")
    public Result<List<CountdownVO>> getCountdowns(@RequestParam Integer userId) {
        try {
            List<CountdownVO> countdowns = homeService.getCountdowns(userId);
            return Result.success(countdowns);
        } catch (Exception e) {
            log.error("获取倒计时列表失败: {}", e.getMessage(), e);
            return Result.failed("获取倒计时列表失败");
        }
    }

    @DeleteMapping("/countdown/{countdownId}")
    @ApiOperation("删除倒计时")
    public Result<Boolean> deleteCountdown(@RequestParam Integer userId, @PathVariable Integer countdownId) {
        try {
            boolean success = homeService.deleteCountdown(userId, countdownId);
            return success ? Result.success(true) : Result.failed("倒计时不存在或无权操作");
        } catch (Exception e) {
            log.error("删除倒计时失败: {}", e.getMessage(), e);
            return Result.failed("删除倒计时失败");
        }
    }

    @GetMapping("/daily-joke")
    @ApiOperation("获取每日一笑")
    public Result<String> getDailyJoke(@RequestParam Integer userId) {
        try {
            String joke = homeService.getDailyJoke(userId);
            return Result.success(joke);
        } catch (Exception e) {
            log.error("获取每日一笑失败: {}", e.getMessage(), e);
            return Result.failed("获取每日一笑失败");
        }
    }

    @GetMapping("/wrong-analysis")
    @ApiOperation("获取错题分析")
    public Result<Object> getWrongAnalysis(@RequestParam Integer userId) {
        try {
            Object analysis = homeService.getWrongAnalysis(userId);
            return Result.success(analysis);
        } catch (Exception e) {
            log.error("获取错题分析失败: {}", e.getMessage(), e);
            return Result.failed("获取错题分析失败");
        }
    }
}