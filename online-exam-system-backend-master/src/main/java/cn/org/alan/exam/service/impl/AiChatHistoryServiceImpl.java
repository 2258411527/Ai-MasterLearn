package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.AiChatHistoryMapper;
import cn.org.alan.exam.model.entity.AiChatHistory;
import cn.org.alan.exam.model.vo.ai.AiChatHistoryVO;
import cn.org.alan.exam.service.IAiChatHistoryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * AI聊天记录服务实现类
 *
 * @author AI Assistant
 * @since 2024
 */
@Service
@Slf4j
public class AiChatHistoryServiceImpl extends ServiceImpl<AiChatHistoryMapper, AiChatHistory> implements IAiChatHistoryService {

    @Resource
    private AiChatHistoryMapper aiChatHistoryMapper;

    @Override
    @Transactional
    public Result<String> saveChatHistory(Integer userId, String role, String content) {
        try {
            AiChatHistory chatHistory = new AiChatHistory();
            chatHistory.setUserId(userId);
            chatHistory.setRole(role);
            chatHistory.setContent(content);

            boolean success = save(chatHistory);
            if (success) {
                log.info("保存聊天记录成功，用户ID：{}，角色：{}", userId, role);
                return Result.success("保存成功");
            } else {
                return Result.failed("保存失败");
            }
        } catch (Exception e) {
            log.error("保存聊天记录失败", e);
            return Result.failed("保存失败");
        }
    }

    @Override
    public Result<List<AiChatHistoryVO>> getChatHistory(Integer userId) {
        try {
            List<AiChatHistoryVO> chatHistory = aiChatHistoryMapper.selectByUserId(userId);
            return Result.success("查询成功", chatHistory);
        } catch (Exception e) {
            log.error("获取聊天记录失败", e);
            return Result.failed("获取失败");
        }
    }

    @Override
    public Result<IPage<AiChatHistoryVO>> getChatHistoryPage(Integer pageNum, Integer pageSize, Integer userId) {
        try {
            Page<AiChatHistoryVO> page = new Page<>(pageNum, pageSize);
            IPage<AiChatHistoryVO> result = aiChatHistoryMapper.selectChatHistoryPage(page, userId);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("分页查询聊天记录失败", e);
            return Result.failed("查询失败");
        }
    }

    @Override
    @Transactional
    public Result<String> clearChatHistory(Integer userId) {
        try {
            int affectedRows = aiChatHistoryMapper.deleteByUserId(userId);
            if (affectedRows > 0) {
                log.info("清除聊天记录成功，用户ID：{}，清除记录数：{}", userId, affectedRows);
                return Result.success("清除成功");
            } else {
                return Result.success("无记录可清除");
            }
        } catch (Exception e) {
            log.error("清除聊天记录失败", e);
            return Result.failed("清除失败");
        }
    }
}