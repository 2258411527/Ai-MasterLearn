package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.UserProfile;
import cn.org.alan.exam.model.form.user.UserProfileForm;
import cn.org.alan.exam.service.IUserProfileService;
import cn.org.alan.exam.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "用户画像与AI规划相关接口")
@RestController
@RequestMapping("/user-profile")
@Slf4j
public class UserProfileController {

    @Autowired
    private IUserProfileService userProfileService;

    @ApiOperation("提交备考问卷并生成AI计划")
    @PostMapping("/submit")
    public Result<String> submitQuestionnaire(@RequestBody UserProfileForm form) {
        try {
            Integer userId = SecurityUtil.getUserId();
            
            // 将 Form 转换为 Entity
            UserProfile profile = new UserProfile();
            BeanUtils.copyProperties(form, profile);
            
            // 处理数组转 JSON 字符串
            if (form.getWeakModules() != null) {
                profile.setWeakModules(com.alibaba.fastjson.JSON.toJSONString(form.getWeakModules()));
            }
            if (form.getNeeds() != null) {
                profile.setNeeds(com.alibaba.fastjson.JSON.toJSONString(form.getNeeds()));
            }

            userProfileService.saveUserProfileAndGeneratePlan(userId, profile);
            return Result.success("问卷提交成功，AI正在为您生成个性化学习计划...");
        } catch (Exception e) {
            log.error("提交问卷失败", e);
            return Result.failed("提交失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取用户AI学习计划")
    @GetMapping("/plan")
    public Result<UserProfile> getLearningPlan() {
        try {
            Integer userId = SecurityUtil.getUserId();
            UserProfile profile = userProfileService.getUserProfileWithPlan(userId);
            return Result.success(profile);
        } catch (Exception e) {
            log.error("获取学习计划失败", e);
            return Result.failed("请先登录或登录已过期");
        }
    }
}
