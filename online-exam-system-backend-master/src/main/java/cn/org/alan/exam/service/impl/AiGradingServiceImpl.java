package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.entity.Option;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.entity.UserExamsScore;
import cn.org.alan.exam.model.form.ai.AiGradingForm;
import cn.org.alan.exam.model.vo.ai.AiGradingResultVO;
import cn.org.alan.exam.service.*;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AiGradingServiceImpl implements IAiGradingService {

    @Autowired
    private IQuestionService questionService;

    @Autowired
    private IOptionService optionService;

    @Autowired
    private IExamQuAnswerService examQuAnswerService;

    @Autowired
    private IAutoScoringService autoScoringService;

    @Autowired
    private IExamService examService;

    @Autowired
    private RealAiGradingService realAiGradingService;

    @Autowired
    private IUserExamsScoreService userExamsScoreService;

    @Override
    public Result<AiGradingResultVO> gradeSingleQuestion(AiGradingForm form) {
        if (!SecurityUtil.isTeacherOrAdmin()) {
            return Result.failed("仅教师和管理员可以使用AI阅卷评分功能");
        }

        try {
            if (form.getQuestionId() == null || form.getQuestionId() <= 0) {
                return Result.failed("题目ID不能为空且必须大于0");
            }

            Question question = questionService.getById(form.getQuestionId());
            if (question == null) {
                return Result.failed("题目不存在");
            }

            form.setStandardAnswer(getStandardAnswer(question));
            if (form.getQuestionContent() == null || form.getQuestionContent().isEmpty()) {
                form.setQuestionContent(question.getContent());
            }
            if (form.getQuestionType() == null) {
                form.setQuestionType(String.valueOf(question.getQuType()));
            }
            form.setGradingMode(true);

            Result<AiGradingResultVO> aiResult = realAiGradingService.gradeWithRealAI(form);

            if (aiResult.getCode() != 1) {
                log.error("AI评分调用失败: {}", aiResult.getMsg());
                return Result.failed("AI评分服务调用失败: " + aiResult.getMsg());
            }

            markExamAsAiGraded(form.getExamId(), form.getUserId());

            log.info("教师AI阅卷完成 - 题目ID: {}, 评分: {}", form.getQuestionId(), aiResult.getData().getAiScore());
            return aiResult;

        } catch (Exception e) {
            log.error("AI阅卷异常", e);
            return Result.failed("AI阅卷失败: " + e.getMessage());
        }
    }

    @Override
    public Result<AiGradingResultVO> analyzeQuestion(AiGradingForm form) {
        try {
            if (form.getQuestionId() == null || form.getQuestionId() <= 0) {
                return Result.failed("题目ID不能为空且必须大于0");
            }

            Question question = questionService.getById(form.getQuestionId());
            if (question == null) {
                return Result.failed("题目不存在");
            }

            form.setStandardAnswer(getStandardAnswer(question));
            if (form.getQuestionContent() == null || form.getQuestionContent().isEmpty()) {
                form.setQuestionContent(question.getContent());
            }
            if (form.getQuestionType() == null) {
                form.setQuestionType(String.valueOf(question.getQuType()));
            }
            form.setGradingMode(false);

            Result<AiGradingResultVO> aiResult = realAiGradingService.gradeWithRealAI(form);

            if (aiResult.getCode() != 1) {
                log.error("AI解析调用失败: {}", aiResult.getMsg());
                return Result.failed("AI解析服务调用失败: " + aiResult.getMsg());
            }

            log.info("学生AI解析完成 - 题目ID: {}", form.getQuestionId());
            return aiResult;

        } catch (Exception e) {
            log.error("AI解析异常", e);
            return Result.failed("AI解析失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Object> gradeEntireExam(Integer examId, Integer userId) {
        if (!SecurityUtil.isTeacherOrAdmin()) {
            return Result.failed("仅教师和管理员可以使用AI阅卷评分功能");
        }

        try {
            LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamQuAnswer::getExamId, examId)
                    .eq(ExamQuAnswer::getUserId, userId)
                    .eq(ExamQuAnswer::getQuestionType, 4)
                    .isNull(ExamQuAnswer::getAiScore);

            long ungradedCount = examQuAnswerService.count(wrapper);

            if (ungradedCount == 0) {
                List<ExamQuAnswer> scoredAnswers = examQuAnswerService.list(
                        new LambdaQueryWrapper<ExamQuAnswer>()
                                .eq(ExamQuAnswer::getExamId, examId)
                                .eq(ExamQuAnswer::getUserId, userId)
                                .isNotNull(ExamQuAnswer::getAiScore));

                int totalAIScore = scoredAnswers.stream()
                        .mapToInt(ExamQuAnswer::getAiScore)
                        .sum();

                AiGradingResultVO summary = new AiGradingResultVO();
                summary.setAiScore(totalAIScore);
                summary.setDetailedAnalysis("AI阅卷已完成！简答题AI评分合计：" + totalAIScore + "分");
                summary.setImprovementSuggestions("建议查看各题详细评分。");
                return Result.success(summary);
            }

            Result<Object> batchResult = realAiGradingService.batchGradeWithRealAI(examId, userId);

            if (batchResult.getCode() == 1) {
                markExamAsAiGraded(examId, userId);
            }

            return batchResult;

        } catch (Exception e) {
            log.error("整卷AI阅卷异常", e);
            return Result.failed("整卷AI阅卷失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Object> getAiGradingHistory(Integer examId, Integer userId) {
        try {
            LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamQuAnswer::getExamId, examId)
                    .eq(ExamQuAnswer::getUserId, userId)
                    .eq(ExamQuAnswer::getQuestionType, 4)
                    .isNotNull(ExamQuAnswer::getAiScore);

            List<ExamQuAnswer> answers = examQuAnswerService.list(wrapper);

            AiGradingResultVO history = new AiGradingResultVO();
            if (!answers.isEmpty()) {
                int totalScore = answers.stream().mapToInt(ExamQuAnswer::getAiScore).sum();
                history.setAiScore(totalScore);
                history.setDetailedAnalysis("简答题AI评分历史记录，共 " + answers.size() + " 道简答题已完成评分。");
            } else {
                history.setAiScore(0);
                history.setDetailedAnalysis("暂无AI评分历史记录");
            }

            return Result.success(history);
        } catch (Exception e) {
            log.error("获取AI阅卷历史异常", e);
            return Result.failed("获取历史记录失败: " + e.getMessage());
        }
    }

    private void markExamAsAiGraded(Integer examId, Integer userId) {
        try {
            LambdaQueryWrapper<UserExamsScore> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserExamsScore::getExamId, examId)
                    .eq(UserExamsScore::getUserId, userId);
            UserExamsScore score = userExamsScoreService.getOne(wrapper);
            if (score != null && (score.getAiGraded() == null || score.getAiGraded() != 1)) {
                score.setAiGraded(1);
                userExamsScoreService.updateById(score);
            }
        } catch (Exception e) {
            log.warn("标记AI阅卷状态失败: examId={}, userId={}", examId, userId);
        }
    }

    private String getStandardAnswer(Question question) {
        switch (question.getQuType()) {
            case 1:
            case 2:
                LambdaQueryWrapper<Option> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Option::getQuId, question.getId())
                        .eq(Option::getIsRight, 1);
                List<Option> correctOptions = optionService.list(wrapper);
                StringBuilder answer = new StringBuilder();
                for (Option option : correctOptions) {
                    answer.append(option.getSort()).append(".");
                }
                return answer.length() > 0 ? answer.substring(0, answer.length() - 1) : "";
            case 3:
                LambdaQueryWrapper<Option> judgmentWrapper = new LambdaQueryWrapper<>();
                judgmentWrapper.eq(Option::getQuId, question.getId())
                        .eq(Option::getIsRight, 1);
                List<Option> correctJudgmentOptions = optionService.list(judgmentWrapper);
                if (!correctJudgmentOptions.isEmpty()) {
                    Option correctOption = correctJudgmentOptions.get(0);
                    return "1".equals(String.valueOf(correctOption.getSort())) ? "正确" : "错误";
                }
                return "正确";
            case 4:
                return question.getAnalysis() != null ? question.getAnalysis() : "暂无标准答案";
            default:
                return question.getAnalysis() != null ? question.getAnalysis() : "暂无标准答案";
        }
    }
}