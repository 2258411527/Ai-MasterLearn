package cn.org.alan.exam.service;

import cn.org.alan.exam.model.entity.UserProfile;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IUserProfileService extends IService<UserProfile> {

    /**
     * 保存用户问卷并生成AI学习计划
     */
    void saveUserProfileAndGeneratePlan(Integer userId, UserProfile profile);

    /**
     * 获取用户的AI学习计划和画像信息
     */
    UserProfile getUserProfileWithPlan(Integer userId);
}
