package cn.org.alan.exam.service.impl;

import cn.hutool.json.JSONObject;
import cn.org.alan.exam.mapper.UserProfileMapper;
import cn.org.alan.exam.model.entity.UserProfile;
import cn.org.alan.exam.service.IUserProfileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements IUserProfileService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void saveUserProfileAndGeneratePlan(Integer userId, UserProfile profile) {
        profile.setUserId(userId);
        profile.setPlanStatus(0); // 初始状态为未生成
        this.saveOrUpdate(profile);

        // 异步调用 AI 生成计划
        CompletableFuture.runAsync(() -> generateAiPlan(userId));
    }

    @Override
    public UserProfile getUserProfileWithPlan(Integer userId) {
        return this.getOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
    }

    private void generateAiPlan(Integer userId) {
        UserProfile profile = this.getUserProfileWithPlan(userId);
        if (profile == null) return;

        try {
            // 构建 Prompt
            String prompt = buildLearningPlanPrompt(profile);
            
            // 调用 Coze API (简化实现，实际应使用封装好的工具类)
            String aiResponse = callCozeAPI(prompt);
            
            // 解析并更新
            profile.setAiPlan(aiResponse);
            profile.setPlanStatus(1);
            this.updateById(profile);
            
            log.info("用户 {} 的 AI 学习计划生成成功", userId);
        } catch (Exception e) {
            log.error("生成 AI 学习计划失败: {}", e.getMessage());
        }
    }

    private String buildLearningPlanPrompt(UserProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位资深的备考规划专家。请根据以下用户画像制定一份详细的个性化学习计划：\n");
        sb.append("目标考试：").append(profile.getTargetExamType()).append("\n");
        sb.append("备考身份：").append(profile.getIdentity()).append("\n");
        sb.append("每日可用时长：").append(profile.getDailyHours()).append("\n");
        sb.append("当前进度：").append(profile.getCurrentProgress()).append("\n");
        sb.append("薄弱模块：").append(profile.getWeakModules()).append("\n");
        sb.append("需求倾向：").append(profile.getNeeds()).append("\n");
        
        if ("POSTGRADUATE".equals(profile.getTargetExamType())) {
            sb.append("考研届数：").append(profile.getExamRound()).append("\n");
            sb.append("目标院校层次：").append(profile.getUniversityLevel()).append("\n");
        } else if ("CIVIL_SERVICE".equals(profile.getTargetExamType())) {
            sb.append("考公类型：").append(profile.getCivilExamType()).append("\n");
            sb.append("意向地区：").append(profile.getTargetRegion()).append("\n");
        }
        
        sb.append("\n请输出包含以下内容的 JSON 格式：\n");
        sb.append("1. 总体复习策略（学习总结）\n");
        sb.append("2. 阶段性进度安排（基础、强化、冲刺阶段的时间节点）\n");
        sb.append("3. 针对薄弱模块的专项建议\n");
        sb.append("4. 每日任务模板");
        
        return sb.toString();
    }

    private String callCozeAPI(String prompt) {
        // 这里简化处理，实际应从配置文件或数据库获取 Token 和 Bot ID
        String url = "https://api.coze.cn/open_api/v2/chat";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // headers.set("Authorization", "Bearer YOUR_TOKEN"); 
        
        Map<String, Object> body = new HashMap<>();
        body.put("bot_id", "YOUR_BOT_ID");
        body.put("user", "user_" + System.currentTimeMillis());
        body.put("query", prompt);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }
}
