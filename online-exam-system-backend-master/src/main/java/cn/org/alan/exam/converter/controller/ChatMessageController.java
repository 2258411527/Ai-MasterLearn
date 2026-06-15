package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.ChatMessage;
import cn.org.alan.exam.service.IChatMessageService;
import cn.org.alan.exam.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 聊天消息控制器
 */
@RestController
@RequestMapping("/chat")
@Api(tags = "聊天消息接口")
public class ChatMessageController {

    @Resource
    private IChatMessageService chatMessageService;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    @ApiOperation("发送消息")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<ChatMessage> sendMessage(@RequestBody ChatMessage chatMessage) {
        Integer currentUserId = SecurityUtil.getUserId();
        chatMessage.setFromUserId(currentUserId);
        return chatMessageService.sendMessage(chatMessage);
    }

    /**
     * 获取聊天记录
     */
    @GetMapping("/history/{friendId}")
    @ApiOperation("获取聊天记录")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<List<ChatMessage>> getChatHistory(@PathVariable("friendId") Integer friendId,
                                                   @RequestParam(required = false, defaultValue = "50") Integer limit) {
        Integer userId = SecurityUtil.getUserId();
        return chatMessageService.getChatHistory(userId, friendId, limit);
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read/{friendId}")
    @ApiOperation("标记消息为已读")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> markMessagesAsRead(@PathVariable("friendId") Integer friendId) {
        Integer userId = SecurityUtil.getUserId();
        return chatMessageService.markMessagesAsRead(friendId, userId);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    @ApiOperation("获取未读消息数量")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<Integer> getUnreadMessageCount() {
        Integer userId = SecurityUtil.getUserId();
        return chatMessageService.getUnreadMessageCount(userId);
    }

    /**
     * 删除聊天记录
     */
    @DeleteMapping("/history/{friendId}")
    @ApiOperation("删除聊天记录")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> deleteChatHistory(@PathVariable("friendId") Integer friendId) {
        Integer userId = SecurityUtil.getUserId();
        return chatMessageService.deleteChatHistory(userId, friendId);
    }
}