package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.ai.AiChatHistoryVO;
import cn.org.alan.exam.service.IAiChatHistoryService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * AI聊天记录控制器
 *
 * @author AI Assistant
 * @since 2024
 */
@Api(tags = "AI聊天记录相关接口")
@RestController
@RequestMapping("/ai-chat-history")
public class AiChatHistoryController {

    @Resource
    private IAiChatHistoryService aiChatHistoryService;

    /**
     * 获取当前用户的聊天记录
     *
     * @return 聊天记录列表
     */
    @ApiOperation("获取当前用户的聊天记录")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<List<AiChatHistoryVO>> getChatHistory() {
        Integer userId = SecurityUtil.getUserId();
        return aiChatHistoryService.getChatHistory(userId);
    }

    /**
     * 分页查询当前用户的聊天记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @ApiOperation("分页查询当前用户的聊天记录")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<IPage<AiChatHistoryVO>> getChatHistoryPage(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize) {
        Integer userId = SecurityUtil.getUserId();
        return aiChatHistoryService.getChatHistoryPage(pageNum, pageSize, userId);
    }

    /**
     * 清除当前用户的聊天记录
     *
     * @return 清除结果
     */
    @ApiOperation("清除当前用户的聊天记录")
    @DeleteMapping
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> clearChatHistory() {
        Integer userId = SecurityUtil.getUserId();
        return aiChatHistoryService.clearChatHistory(userId);
    }
}