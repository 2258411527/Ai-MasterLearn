package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.UserNotification;
import cn.org.alan.exam.service.IUserNotificationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

@Api(tags = "消息中心相关接口")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Resource
    private IUserNotificationService notificationService;

    @ApiOperation("获取我的通知列表")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<IPage<UserNotification>> getMyNotifications(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "type", required = false) String type) {
        return notificationService.getMyNotifications(pageNum, pageSize, type);
    }

    @ApiOperation("获取未读通知数量")
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<Integer> getUnreadCount() {
        return notificationService.getUnreadCount();
    }

    @ApiOperation("获取必读未读数量")
    @GetMapping("/must-read-count")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<Integer> getMustReadUnreadCount() {
        return notificationService.getMustReadUnreadCount();
    }

    @ApiOperation("标记单条已读")
    @PutMapping("/read/{id}")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> markAsRead(@PathVariable("id") Integer id) {
        return notificationService.markAsRead(id);
    }

    @ApiOperation("全部标记已读")
    @PutMapping("/read-all")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> markAllAsRead() {
        return notificationService.markAllAsRead();
    }

    @ApiOperation("发送系统通知（管理员）")
    @PostMapping("/broadcast")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> broadcastNotification(
            @RequestParam("type") @NotBlank String type,
            @RequestParam("title") @NotBlank String title,
            @RequestParam("content") @NotBlank String content,
            @RequestParam(value = "isMustRead", required = false, defaultValue = "0") Integer isMustRead) {
        return notificationService.broadcastNotification(type, title, content, isMustRead);
    }

    @ApiOperation("获取指定类型的未读通知数量")
    @GetMapping("/unread-count/{type}")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<Integer> getUnreadCountByType(@PathVariable("type") String type) {
        return notificationService.getUnreadCountByType(type);
    }

    @ApiOperation("获取各类通知的未读数量汇总（红标）")
    @GetMapping("/unread-summary")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<java.util.Map<String, Integer>> getUnreadSummary() {
        java.util.Map<String, Integer> summary = new java.util.HashMap<>();
        summary.put("notice", notificationService.getUnreadCountByType("notice").getData());
        summary.put("friend_request", notificationService.getUnreadCountByType("friend_request").getData());
        summary.put("friend_message", notificationService.getUnreadCountByType("friend_message").getData());
        summary.put("grading_complete", notificationService.getUnreadCountByType("grading_complete").getData());
        summary.put("token_low", notificationService.getUnreadCountByType("token_low").getData());
        summary.put("system_upgrade", notificationService.getUnreadCountByType("system_upgrade").getData());
        summary.put("system", notificationService.getUnreadCountByType("system").getData());
        summary.put("total", notificationService.getUnreadCount().getData());
        return Result.success("查询成功", summary);
    }

    @ApiOperation("按类型全部标记已读")
    @PutMapping("/read-all/{type}")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> markAllAsReadByType(@PathVariable("type") String type) {
        return notificationService.markAllAsReadByType(type);
    }

    @ApiOperation("发送Token不足通知给指定用户")
    @PostMapping("/token-low")
    @PreAuthorize("hasAnyAuthority('role_admin', 'role_student')")
    public Result<String> sendTokenLowNotification(
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "remaining", defaultValue = "0") Integer remaining) {
        Integer targetUserId = userId != null ? userId : cn.org.alan.exam.utils.SecurityUtil.getUserId();
        String title = "Token余额不足";
        String content = "您的Token余额已不足（剩余" + remaining + "个），部分AI功能将无法使用，请及时充值。";
        return notificationService.createNotification(targetUserId, "token_low", null, title, content, 1);
    }

    @ApiOperation("发送系统升级通知（管理员）")
    @PostMapping("/system-upgrade")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> sendSystemUpgradeNotification(
            @RequestParam("title") @NotBlank String title,
            @RequestParam("content") @NotBlank String content,
            @RequestParam(value = "isMustRead", required = false, defaultValue = "1") Integer isMustRead) {
        return notificationService.broadcastNotification("system_upgrade", title, content, isMustRead);
    }

    @ApiOperation("发送好友消息通知")
    @PostMapping("/friend-message")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> sendFriendMessageNotification(
            @RequestParam("userId") Integer userId,
            @RequestParam("senderId") Integer senderId,
            @RequestParam("senderName") String senderName,
            @RequestParam("senderAvatar") String senderAvatar,
            @RequestParam("message") String message) {
        String title = "好友消息";
        return notificationService.createNotificationWithSender(userId, "friend_message", null, senderId, senderName, senderAvatar, title, message, 0);
    }
}
