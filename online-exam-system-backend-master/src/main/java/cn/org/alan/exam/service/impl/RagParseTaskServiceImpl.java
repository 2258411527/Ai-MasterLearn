package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.RagParseTaskMapper;
import cn.org.alan.exam.model.entity.RagParseTask;
import cn.org.alan.exam.service.IRagParseTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RAG解析任务服务实现类
 *
 * @author AI Assistant
 * @since 2026
 */
@Slf4j
@Service
public class RagParseTaskServiceImpl extends ServiceImpl<RagParseTaskMapper, RagParseTask> implements IRagParseTaskService {

    @Override
    public Integer createTask(Integer userId, Integer studyMaterialId) {
        log.info("创建解析任务 - 用户ID: {}, 资料ID: {}", userId, studyMaterialId);
        
        RagParseTask task = new RagParseTask();
        task.setUserId(userId);
        task.setStudyMaterialId(studyMaterialId);
        task.setTaskStatus(0); // 0-等待处理
        task.setProgress(0);
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());
        
        save(task);
        log.info("解析任务创建成功，任务ID: {}", task.getId());
        
        return task.getId();
    }

    @Override
    public void updateProgress(Integer taskId, Integer progress, Integer processedPages) {
        log.debug("更新任务进度 - 任务ID: {}, 进度: {}%, 已处理页数: {}", taskId, progress, processedPages);
        
        baseMapper.updateProgress(taskId, progress, processedPages);
    }

    @Override
    public void markCompleted(Integer taskId, Integer totalChunks) {
        log.info("标记任务完成 - 任务ID: {}, 总分块数: {}", taskId, totalChunks);
        
        baseMapper.updateToCompleted(taskId, totalChunks);
    }

    @Override
    public void markFailed(Integer taskId, String errorMessage) {
        log.error("标记任务失败 - 任务ID: {}, 错误信息: {}", taskId, errorMessage);
        
        baseMapper.updateToFailed(taskId, errorMessage);
    }

    @Override
    public Result<List<RagParseTask>> getUserTasks(Integer userId) {
        try {
            List<RagParseTask> tasks = baseMapper.selectByUserId(userId);
            log.info("获取用户解析任务列表 - 用户ID: {}, 任务数量: {}", userId, tasks.size());
            return Result.success("获取成功", tasks);
        } catch (Exception e) {
            log.error("获取用户解析任务列表失败 - 用户ID: {}", userId, e);
            return Result.failed("获取任务列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<RagParseTask> getLatestTaskByMaterial(Integer studyMaterialId) {
        try {
            RagParseTask task = baseMapper.selectLatestByMaterialId(studyMaterialId);
            if (task == null) {
                return Result.failed("未找到解析任务");
            }
            log.info("获取最新解析任务 - 资料ID: {}, 任务ID: {}", studyMaterialId, task.getId());
            return Result.success("获取成功", task);
        } catch (Exception e) {
            log.error("获取最新解析任务失败 - 资料ID: {}", studyMaterialId, e);
            return Result.failed("获取任务信息失败：" + e.getMessage());
        }
    }
}
