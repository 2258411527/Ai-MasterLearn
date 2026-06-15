package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.RagParseTask;
import cn.org.alan.exam.model.entity.StudyMaterial;
import cn.org.alan.exam.model.vo.study.StudyMaterialVO;
import cn.org.alan.exam.service.IRagKnowledgeService;
import cn.org.alan.exam.service.IRagParseTaskService;
import cn.org.alan.exam.service.IStudyMaterialService;
import cn.org.alan.exam.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/grading-rag")
@Api(tags = "教师阅卷RAG知识库")
@Slf4j
@PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
public class GradingRagController {

    @Resource
    private IRagKnowledgeService ragKnowledgeService;

    @Resource
    private IStudyMaterialService studyMaterialService;

    @Resource
    private IRagParseTaskService ragParseTaskService;

    @PostMapping("/parse/{materialId}")
    @ApiOperation("异步解析阅卷参考材料到阅卷知识库")
    public Result<Map<String, Object>> parseGradingMaterial(@PathVariable("materialId") Integer materialId) {
        log.info("开始异步解析阅卷参考材料 - 资料ID: {}, 用户ID: {}", materialId, SecurityUtil.getUserId());
        try {
            Integer userId = SecurityUtil.getUserId();

            StudyMaterial material = studyMaterialService.getById(materialId);
            if (material == null || !material.getUserId().equals(userId)) {
                return Result.failed("资料不存在或无权操作");
            }

            long fileSize = material.getFileSize() != null ? material.getFileSize() : 0;
            long maxSize = 100 * 1024 * 1024;
            if (fileSize > maxSize) {
                return Result.failed("文件大小超过100MB限制");
            }

            Integer taskId = ragParseTaskService.createTask(userId, materialId);

            Thread asyncThread = new Thread(() -> {
                try {
                    ragParseTaskService.updateProgress(taskId, 1, 0);
                    Result<String> result = ragKnowledgeService.parseAndStoreGradingMaterial(material, taskId);
                    if (result.getCode() == 1) {
                        log.info("阅卷材料解析成功: {}", result.getMsg());
                    } else {
                        log.error("阅卷材料解析失败: {}", result.getMsg());
                    }
                } catch (Exception e) {
                    log.error("阅卷材料解析异常", e);
                    ragParseTaskService.markFailed(taskId, "解析异常: " + e.getMessage());
                }
            });
            asyncThread.setDaemon(true);
            asyncThread.start();

            Map<String, Object> response = new HashMap<>();
            response.put("taskId", taskId);
            response.put("message", "阅卷材料解析任务已提交");
            response.put("status", "pending");
            return Result.success("任务已提交", response);

        } catch (Exception e) {
            log.error("提交阅卷材料解析任务失败", e);
            return Result.failed("提交任务失败: " + e.getMessage());
        }
    }

    @GetMapping("/task/{taskId}")
    @ApiOperation("查询阅卷解析任务进度")
    public Result<RagParseTask> getTaskProgress(@PathVariable("taskId") Integer taskId) {
        try {
            Integer userId = SecurityUtil.getUserId();
            RagParseTask task = ragParseTaskService.getById(taskId);
            if (task == null || !task.getUserId().equals(userId)) {
                return Result.failed("任务不存在或无权查看");
            }
            return Result.success("查询成功", task);
        } catch (Exception e) {
            log.error("查询任务进度失败", e);
            return Result.failed("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/tasks")
    @ApiOperation("获取阅卷解析任务列表")
    public Result<List<RagParseTask>> getUserTasks() {
        try {
            return ragParseTaskService.getUserTasks(SecurityUtil.getUserId());
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            return Result.failed("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/materials")
    @ApiOperation("获取教师已上传的学习材料（可作阅卷参考）")
    public Result<Object> getMaterials() {
        try {
            Integer userId = SecurityUtil.getUserId();
            Result<List<StudyMaterialVO>> materialsResult = studyMaterialService.getMaterialsByUserId(userId);

            if (materialsResult.getCode() == 1 && materialsResult.getData() != null) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                for (StudyMaterialVO material : materialsResult.getData()) {
                    Map<String, Object> materialInfo = new HashMap<>();
                    materialInfo.put("id", material.getId());
                    materialInfo.put("originalName", material.getOriginalName());
                    materialInfo.put("displayName", material.getFileName());
                    materialInfo.put("fileType", material.getFileType());
                    materialInfo.put("fileSize", material.getFileSize());
                    materialInfo.put("uploadTime", material.getUploadTime());

                    Result<RagParseTask> taskResult = ragParseTaskService.getLatestTaskByMaterial(material.getId());
                    boolean isParsed = taskResult.getData() != null && taskResult.getData().getTaskStatus() == 2;
                    materialInfo.put("isParsed", isParsed);
                    resultList.add(materialInfo);
                }
                return Result.success("获取成功", resultList);
            }
            return Result.failed("获取材料列表失败");
        } catch (Exception e) {
            log.error("获取材料列表失败", e);
            return Result.failed("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    @ApiOperation("获取阅卷知识库统计信息")
    public Result<Object> getKnowledgeStats() {
        try {
            Integer userId = SecurityUtil.getUserId();
            return ragKnowledgeService.getGradingKnowledgeStats(userId);
        } catch (Exception e) {
            log.error("获取阅卷知识库统计失败", e);
            return Result.failed("获取失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear")
    @ApiOperation("清空阅卷知识库")
    public Result<String> clearKnowledge() {
        try {
            Integer userId = SecurityUtil.getUserId();
            return ragKnowledgeService.deleteGradingKnowledge(userId);
        } catch (Exception e) {
            log.error("清空阅卷知识库失败", e);
            return Result.failed("清空失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/material/{materialId}")
    @ApiOperation("删除特定材料的阅卷知识库数据")
    public Result<String> deleteMaterialKnowledge(@PathVariable("materialId") Integer materialId) {
        try {
            Integer userId = SecurityUtil.getUserId();
            StudyMaterial material = studyMaterialService.getById(materialId);
            if (material == null || !material.getUserId().equals(userId)) {
                return Result.failed("资料不存在或无权操作");
            }
            return ragKnowledgeService.deleteMaterialKnowledge(materialId);
        } catch (Exception e) {
            log.error("删除材料知识库数据失败", e);
            return Result.failed("删除失败: " + e.getMessage());
        }
    }
}