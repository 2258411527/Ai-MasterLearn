package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.annotation.RequireToken;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.ai.AiChatForm;
import cn.org.alan.exam.service.IAiChatService;
import cn.org.alan.exam.service.impl.AiChatServiceImpl;
import cn.org.alan.exam.service.impl.EnhancedAiChatServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * AI问答控制器
 *
 * @author AI Assistant
 * @since 2024
 */
@RestController
@RequestMapping("/ai")
@Api(tags = "AI问答相关接口")
@Slf4j
public class AiChatController {

    @Resource
    @Qualifier("aiChatServiceImpl")
    private IAiChatService aiChatService;
    
    @Resource
    private EnhancedAiChatServiceImpl enhancedAiChatService;

    /**
     * AI对话接口（默认模式）
     *
     * @param form 对话表单
     * @return AI回答
     */
    @PostMapping("/chat")
    @ApiOperation("AI对话")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    @RequireToken(value = 1, serviceType = "chat")
    public Result<String> chat(@RequestBody @Validated AiChatForm form) {
        String response = aiChatService.chat(form.getQuestion(), form.getConfigId());
        return Result.success("AI回答成功", response);
    }
    
    /**
     * 增强AI对话接口（支持RAG检索）
     *
     * @param form 对话表单
     * @param enableRag 是否启用RAG检索
     * @param topK 返回最相关的K个结果
     * @return AI回答
     */
    @PostMapping("/chat/enhanced")
    @ApiOperation("增强AI对话（支持RAG检索）")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    @RequireToken(value = 2, serviceType = "enhanced_chat")
    public Result<String> enhancedChat(@RequestBody @Validated AiChatForm form,
                                      @RequestParam(value = "enableRag", defaultValue = "true") boolean enableRag,
                                      @RequestParam(value = "topK", defaultValue = "3") int topK) {
        String response = enhancedAiChatService.chatWithRag(form.getQuestion(), enableRag, topK, form.getConfigId());
        return Result.success("AI回答成功", response);
    }

    @PostMapping("/chat/question")
    @ApiOperation("针对题目的AI问答（自动注入题目上下文）")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    @RequireToken(value = 1, serviceType = "question_chat")
    public Result<String> chatAboutQuestion(@RequestBody @Validated AiChatForm form) {
        log.info("AI题目问答 - configId:{}", form.getConfigId());
        StringBuilder contextPrompt = new StringBuilder();
        contextPrompt.append("你是一位专业的学习辅导老师，正在帮助学生解答与考试题目相关的问题。请用简洁清晰的语言回答。\n\n");

        if (form.getQuestionContent() != null && !form.getQuestionContent().isEmpty()) {
            contextPrompt.append("【当前题目】\n").append(form.getQuestionContent()).append("\n\n");
        }
        if (form.getStandardAnswer() != null && !form.getStandardAnswer().isEmpty()) {
            contextPrompt.append("【标准答案】\n").append(form.getStandardAnswer()).append("\n\n");
        }
        if (form.getUserAnswer() != null && !form.getUserAnswer().isEmpty()) {
            contextPrompt.append("【学生答案】\n").append(form.getUserAnswer()).append("\n\n");
        }

        contextPrompt.append("【学生提问】\n").append(form.getQuestion()).append("\n\n");
        contextPrompt.append("请针对学生在当前题目上的疑问进行解答，帮助其理解知识点和解题思路。");

        String response = aiChatService.chat(contextPrompt.toString(), form.getConfigId());
        return Result.success("AI回答成功", response);
    }
}