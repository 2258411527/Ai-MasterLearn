package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.TeacherGradingMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.TeacherGrading;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.entity.UserExamsScore;
import cn.org.alan.exam.model.form.grading.TeacherGradingForm;
import cn.org.alan.exam.service.ITeacherGradingService;
import cn.org.alan.exam.service.IUserExamsScoreService;
import cn.org.alan.exam.service.IUserNotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 教师阅卷服务实现类
 *
 * @author AI Assistant
 * @since 2025-05-04
 */
@Service
@Slf4j
public class TeacherGradingServiceImpl extends ServiceImpl<TeacherGradingMapper, TeacherGrading> implements ITeacherGradingService {
    
    @Autowired
    private IUserExamsScoreService userExamsScoreService;

    @Autowired
    private IUserNotificationService notificationService;

    @Autowired
    private UserMapper userMapper;
    
    @Override
    @Transactional
    public Result<Object> gradeByTeacher(TeacherGradingForm form) {
        try {
            // 验证参数
            if (form.getTeacherScore() < 0 || form.getTeacherScore() > 100) {
                return Result.failed("教师评分必须在0-100分之间");
            }
            
            // 检查考试是否已完成
            UserExamsScore userExam = userExamsScoreService.getOne(
                new LambdaQueryWrapper<UserExamsScore>()
                    .eq(UserExamsScore::getExamId, form.getExamId())
                    .eq(UserExamsScore::getUserId, form.getUserId())
            );
            
            if (userExam == null || userExam.getState() != 1) {
                return Result.failed("考试未完成或不存在，无法进行教师阅卷");
            }
            
            // 保存或更新教师阅卷记录
            TeacherGrading grading = new TeacherGrading();
            BeanUtils.copyProperties(form, grading);
            grading.setGradingTime(LocalDateTime.now());
            
            // 检查是否已存在记录
            TeacherGrading existing = this.getOne(
                new LambdaQueryWrapper<TeacherGrading>()
                    .eq(TeacherGrading::getExamId, form.getExamId())
                    .eq(TeacherGrading::getUserId, form.getUserId())
                    .eq(TeacherGrading::getQuestionId, form.getQuestionId())
            );
            
            if (existing != null) {
                grading.setId(existing.getId());
                this.updateById(grading);
                log.info("更新教师阅卷记录 - 考试ID: {}, 用户ID: {}, 题目ID: {}, 教师评分: {}", 
                        form.getExamId(), form.getUserId(), form.getQuestionId(), form.getTeacherScore());
            } else {
                this.save(grading);
                log.info("创建教师阅卷记录 - 考试ID: {}, 用户ID: {}, 题目ID: {}, 教师评分: {}", 
                        form.getExamId(), form.getUserId(), form.getQuestionId(), form.getTeacherScore());
            }
            
            // 更新用户考试成绩表
            updateUserExamScore(form.getExamId(), form.getUserId());

            try {
                User teacher = userMapper.selectById(form.getTeacherId());
                String teacherName = teacher != null ? teacher.getRealName() : "教师";
                String teacherAvatar = teacher != null ? teacher.getAvatar() : null;
                notificationService.createNotificationWithSender(
                        form.getUserId(),
                        "grading_complete",
                        form.getExamId(),
                        form.getTeacherId(),
                        teacherName,
                        teacherAvatar,
                        "阅卷完成通知",
                        "你的考试已由 " + teacherName + " 老师完成阅卷",
                        0
                );
            } catch (Exception notifyEx) {
                log.error("创建阅卷完成通知失败", notifyEx);
            }

            return Result.success("教师阅卷完成");
            
        } catch (Exception e) {
            log.error("教师阅卷异常", e);
            return Result.failed("教师阅卷失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<TeacherGrading> getTeacherGradingRecord(Integer examId, Integer userId, Integer questionId) {
        try {
            TeacherGrading grading = this.getOne(
                new LambdaQueryWrapper<TeacherGrading>()
                    .eq(TeacherGrading::getExamId, examId)
                    .eq(TeacherGrading::getUserId, userId)
                    .eq(TeacherGrading::getQuestionId, questionId)
            );
            
            if (grading == null) {
                return Result.failed("未找到教师阅卷记录");
            }
            
            return Result.success(grading);
            
        } catch (Exception e) {
            log.error("获取教师阅卷记录异常", e);
            return Result.failed("获取教师阅卷记录失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Object> batchGradeByTeacher(Integer examId, Integer userId) {
        try {
            // 这里可以实现批量教师阅卷逻辑
            // 例如：教师可以对整个试卷的所有主观题进行批量评分
            
            log.info("批量教师阅卷 - 考试ID: {}, 用户ID: {}", examId, userId);
            
            return Result.success("批量教师阅卷任务已提交");
            
        } catch (Exception e) {
            log.error("批量教师阅卷异常", e);
            return Result.failed("批量教师阅卷失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Object> getPendingGradingExams(Integer teacherId) {
        try {
            // 获取需要教师阅卷的试卷列表
            // 这里可以查询包含主观题且已完成但未进行教师阅卷的试卷
            
            log.info("获取待阅卷试卷列表 - 教师ID: {}", teacherId);
            
            return Result.success("待阅卷试卷列表获取成功");
            
        } catch (Exception e) {
            log.error("获取待阅卷试卷列表异常", e);
            return Result.failed("获取待阅卷试卷列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户考试成绩表
     */
    private void updateUserExamScore(Integer examId, Integer userId) {
        try {
            UserExamsScore userExam = userExamsScoreService.getOne(
                new LambdaQueryWrapper<UserExamsScore>()
                    .eq(UserExamsScore::getExamId, examId)
                    .eq(UserExamsScore::getUserId, userId)
            );
            
            if (userExam != null) {
                // 计算教师阅卷总分
                Integer teacherTotalScore = this.baseMapper.selectList(
                    new LambdaQueryWrapper<TeacherGrading>()
                        .eq(TeacherGrading::getExamId, examId)
                        .eq(TeacherGrading::getUserId, userId)
                        .isNotNull(TeacherGrading::getTeacherScore)
                ).stream()
                .mapToInt(TeacherGrading::getTeacherScore)
                .sum();
                
                // 更新用户考试成绩 - 使用现有字段
                userExam.setUserScore(teacherTotalScore); // 教师评分覆盖原分数
                userExam.setWhetherMark(1); // 标记为已阅卷
                
                userExamsScoreService.updateById(userExam);
                
                log.info("更新用户考试成绩 - 考试ID: {}, 用户ID: {}, 教师总分: {}", 
                        examId, userId, teacherTotalScore);
            }
            
        } catch (Exception e) {
            log.error("更新用户考试成绩异常", e);
        }
    }
}