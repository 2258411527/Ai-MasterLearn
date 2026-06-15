package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.user_questionnaire.QuestionnaireSubmitForm;
import cn.org.alan.exam.model.vo.user_questionnaire.UserStudyDashboardVO;
import cn.org.alan.exam.service.IUserQuestionnaireService;
import cn.org.alan.exam.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户问卷控制器
 */
@RestController
@RequestMapping("/user-questionnaire")
@Api(tags = "用户问卷管理")
public class UserQuestionnaireController {

    @Autowired
    private IUserQuestionnaireService userQuestionnaireService;

    @PostMapping("/submit")
    @ApiOperation("提交用户问卷")
    public Result<String> submitQuestionnaire(@Validated @RequestBody QuestionnaireSubmitForm form) {
        return userQuestionnaireService.submitQuestionnaire(form);
    }

    @GetMapping("/dashboard")
    @ApiOperation("获取用户学习仪表盘")
    public Result<UserStudyDashboardVO> getStudyDashboard() {
        Integer userId = SecurityUtil.getUserId();
        return userQuestionnaireService.getStudyDashboard(userId);
    }

    @GetMapping("/check-completed")
    @ApiOperation("检查用户是否已完成问卷")
    public Result<Boolean> checkQuestionnaireCompleted() {
        Integer userId = SecurityUtil.getUserId();
        return userQuestionnaireService.checkQuestionnaireCompleted(userId);
    }

    @PostMapping("/generate-recommendations")
    @ApiOperation("生成AI个性化推荐")
    public Result<String> generateAiRecommendations() {
        Integer userId = SecurityUtil.getUserId();
        return userQuestionnaireService.generateAiRecommendations(userId);
    }
}