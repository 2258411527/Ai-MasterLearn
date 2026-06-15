package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.RagKnowledge;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG检索增强相关接口
 */
@RestController
@RequestMapping("/rag")
@Api(tags = "RAG检索增强相关接口")
@Slf4j
public class RagController {

    @Resource
    private IRagKnowledgeService ragKnowledgeService;
    
    @Resource
    private IStudyMaterialService studyMaterialService;
    
    @Resource
    private IRagParseTaskService ragParseTaskService;

    /**
     * 异步解析学习资料到知识库(后台处理)
     */
    @PostMapping("/parse/{materialId}")
    @ApiOperation("异步解析学习资料到知识库")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<Map<String, Object>> parseMaterial(@PathVariable("materialId") Integer materialId) {
        long startTime = System.currentTimeMillis();
        log.info("========== 接收到异步解析请求 ==========");
        log.info("资料ID：{}", materialId);
        
        try {
            Integer userId = SecurityUtil.getUserId();
            log.info("用户ID：{}", userId);
            
            // 验证用户权限
            log.info("开始验证用户权限...");
            StudyMaterial material = studyMaterialService.getById(materialId);
            if (material == null || !material.getUserId().equals(userId)) {
                log.warn("⚠️ 资料不存在或无权操作，资料ID：{}，用户ID：{}", materialId, userId);
                return Result.failed("资料不存在或无权操作");
            }
            
            // 检查文件大小(100MB限制)
            long fileSize = material.getFileSize() != null ? material.getFileSize() : 0;
            long maxSize = 100 * 1024 * 1024; // 100MB
            if (fileSize > maxSize) {
                log.warn("⚠️ 文件过大：{} MB，超过100MB限制", fileSize / 1024.0 / 1024.0);
                return Result.failed("文件大小超过100MB限制，无法解析");
            }
            
            log.info("✅ 权限验证通过");
            log.info("文件信息 - 名称：{}，类型：{}，大小：{} bytes ({} MB)", 
                     material.getOriginalName(), 
                     material.getFileType(),
                     fileSize,
                     String.format("%.2f", fileSize / 1024.0 / 1024.0));
            
            // 创建解析任务
            log.info("创建解析任务...");
            Integer taskId = ragParseTaskService.createTask(userId, materialId);
            log.info("✅ 任务创建成功，任务ID：{}", taskId);
            
            // 启动异步解析线程
            Thread asyncThread = new Thread(() -> {
                try {
                    log.info("========== 开始异步解析任务 ==========");
                    log.info("任务ID：{}，资料ID：{}", taskId, materialId);
                    
                    // 更新任务状态为处理中
                    ragParseTaskService.updateProgress(taskId, 1, 0);
                    
                    // 执行解析
                    Result<String> result = ragKnowledgeService.parseAndStoreMaterialWithProgress(material, taskId);
                    
                    if (result.getCode() == 1) {
                        log.info("✅ 异步解析成功：{}", result.getMsg());
                    } else {
                        log.error("❌ 异步解析失败：{}", result.getMsg());
                    }
                    
                    log.info("======================================");
                } catch (Exception e) {
                    log.error("❌ 异步解析异常", e);
                    ragParseTaskService.markFailed(taskId, "解析异常：" + e.getMessage());
                }
            });
            
            asyncThread.setDaemon(true);
            asyncThread.start();
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.info("✅ 异步解析任务已提交");
            log.info("耗时：{} ms", duration);
            log.info("======================================");
            
            // 返回任务信息
            Map<String, Object> response = new HashMap<>();
            response.put("taskId", taskId);
            response.put("message", "解析任务已提交，正在后台处理中");
            response.put("status", "pending");
            
            return Result.success("任务已提交", response);
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.error("❌ 提交解析任务失败");
            log.error("资料ID：{}", materialId);
            log.error("耗时：{} ms", duration);
            log.error("错误类型：{}", e.getClass().getSimpleName());
            log.error("错误信息：{}", e.getMessage());
            log.error("堆栈跟踪：", e);
            log.info("======================================");
            
            return Result.failed("提交任务失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询解析任务进度
     */
    @GetMapping("/task/{taskId}")
    @ApiOperation("查询解析任务进度")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<RagParseTask> getTaskProgress(@PathVariable("taskId") Integer taskId) {
        try {
            Integer userId = SecurityUtil.getUserId();
            log.info("查询任务进度 - 用户ID：{}，任务ID：{}", userId, taskId);
            
            RagParseTask task = ragParseTaskService.getById(taskId);
            if (task == null) {
                return Result.failed("任务不存在");
            }
            
            // 验证权限
            if (!task.getUserId().equals(userId)) {
                return Result.failed("无权查看此任务");
            }
            
            return Result.success("查询成功", task);
        } catch (Exception e) {
            log.error("查询任务进度失败 - 任务ID：{}", taskId, e);
            return Result.failed("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户的解析任务列表
     */
    @GetMapping("/tasks")
    @ApiOperation("获取用户的解析任务列表")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<List<RagParseTask>> getUserTasks() {
        try {
            Integer userId = SecurityUtil.getUserId();
            log.info("获取用户解析任务列表 - 用户ID：{}", userId);
            
            return ragParseTaskService.getUserTasks(userId);
        } catch (Exception e) {
            log.error("获取用户解析任务列表失败", e);
            return Result.failed("获取任务列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取特定资料的最新解析任务
     */
    @GetMapping("/task/material/{materialId}")
    @ApiOperation("获取特定资料的最新解析任务")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<RagParseTask> getLatestTaskByMaterial(@PathVariable("materialId") Integer materialId) {
        try {
            Integer userId = SecurityUtil.getUserId();
            log.info("获取资料最新任务 - 用户ID：{}，资料ID：{}", userId, materialId);
            
            // 验证权限
            StudyMaterial material = studyMaterialService.getById(materialId);
            if (material == null || !material.getUserId().equals(userId)) {
                return Result.failed("资料不存在或无权操作");
            }
            
            return ragParseTaskService.getLatestTaskByMaterial(materialId);
        } catch (Exception e) {
            log.error("获取资料最新任务失败 - 资料ID：{}", materialId, e);
            return Result.failed("获取任务信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 解析用户所有学习资料到知识库
     */
    @PostMapping("/parse-all")
    @ApiOperation("解析用户所有学习资料到知识库")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> parseAllMaterials() {
        try {
            Integer userId = SecurityUtil.getUserId();
            log.info("开始解析用户所有学习资料，用户ID：{}", userId);
            
            // 获取用户的所有学习资料
            Result<List<StudyMaterialVO>> materialsResult = studyMaterialService.getMaterialsByUserId(userId);
            List<StudyMaterial> materials = new ArrayList<>();
            
            if (materialsResult.getCode() == 1 && materialsResult.getData() != null) {
                // 将StudyMaterialVO转换为StudyMaterial
                for (StudyMaterialVO vo : materialsResult.getData()) {
                    StudyMaterial material = new StudyMaterial();
                    material.setId(vo.getId());
                    material.setUserId(vo.getUserId());
                    material.setFileName(vo.getFileName());
                    material.setOriginalName(vo.getOriginalName());
                    material.setFilePath(vo.getFilePath());
                    material.setFileType(vo.getFileType());
                    material.setUploadTime(vo.getUploadTime());
                    materials.add(material);
                }
            }
            
            if (materials.isEmpty()) {
                return Result.success("用户没有上传任何学习资料");
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (StudyMaterial material : materials) {
                try {
                    Result<String> result = ragKnowledgeService.parseAndStoreMaterial(material);
                    if (result.getCode() == 1) {
                        successCount++;
                        log.info("解析资料成功，文件名：{}", material.getOriginalName());
                    } else {
                        failCount++;
                        log.warn("解析资料失败，文件名：{}，错误：{}", material.getOriginalName(), result.getMsg());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("解析资料异常，文件名：{}，错误：{}", material.getOriginalName(), e.getMessage());
                }
            }
            
            String message = String.format("解析完成！成功：%d个，失败：%d个", successCount, failCount);
            log.info("用户所有学习资料解析完成，用户ID：{}，{}", userId, message);
            
            return Result.success(message);
            
        } catch (Exception e) {
            log.error("解析用户所有学习资料失败，用户ID：{}，错误信息：{}", SecurityUtil.getUserId(), e.getMessage(), e);
            return Result.failed("解析失败：" + e.getMessage());
        }
    }

    /**
     * 获取知识库统计信息
     */
    @GetMapping("/stats")
    @ApiOperation("获取知识库统计信息")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<Object> getKnowledgeStats() {
        try {
            Integer userId = SecurityUtil.getUserId();
            Result<Object> result = ragKnowledgeService.getKnowledgeStats(userId);
            log.info("获取知识库统计信息，用户ID：{}，结果：{}", userId, result.getMsg());
            return result;
            
        } catch (Exception e) {
            log.error("获取知识库统计信息失败，用户ID：{}，错误信息：{}", SecurityUtil.getUserId(), e.getMessage(), e);
            return Result.failed("获取统计信息失败：" + e.getMessage());
        }
    }

    /**
     * 清空用户知识库
     */
    @DeleteMapping("/clear")
    @ApiOperation("清空用户知识库")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> clearKnowledge() {
        try {
            Integer userId = SecurityUtil.getUserId();
            Result<String> result = ragKnowledgeService.deleteUserKnowledge(userId);
            log.info("清空用户知识库，用户ID：{}，结果：{}", userId, result.getMsg());
            return result;
            
        } catch (Exception e) {
            log.error("清空用户知识库失败，用户ID：{}，错误信息：{}", SecurityUtil.getUserId(), e.getMessage(), e);
            return Result.failed("清空失败：" + e.getMessage());
        }
    }

    /**
     * 测试RAG检索
     */
    @PostMapping("/test")
    @ApiOperation("测试RAG检索")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<Object> testRagSearch(@RequestParam("question") String question,
                                       @RequestParam(value = "topK", defaultValue = "3") int topK) {
        try {
            Integer userId = SecurityUtil.getUserId();
            
            log.info("测试RAG检索，用户ID：{}，问题：{}，topK：{}", userId, question, topK);
            
            // 检索相关知识
            Result<List<String>> searchResult = ragKnowledgeService.searchKnowledge(userId, question, topK);
            
            // 构建测试结果
            Map<String, Object> testResult = new HashMap<>();
            testResult.put("question", question);
            testResult.put("topK", topK);
            testResult.put("foundCount", searchResult.getData() != null ? searchResult.getData().size() : 0);
            testResult.put("searchResults", searchResult.getData());
            testResult.put("message", searchResult.getMsg());
            
            int foundCount = searchResult.getData() != null ? searchResult.getData().size() : 0;
            log.info("RAG检索测试完成，用户ID：{}，找到相关结果：{}个", userId, foundCount);
            return Result.success("检索测试完成", testResult);
            
        } catch (Exception e) {
            log.error("RAG检索测试失败，用户ID：{}，错误信息：{}", SecurityUtil.getUserId(), e.getMessage(), e);
            return Result.failed("检索测试失败：" + e.getMessage());
        }
    }

    /**
     * 删除特定资料的知识库数据
     */
    @DeleteMapping("/material/{materialId}")
    @ApiOperation("删除特定资料的知识库数据")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> deleteMaterialKnowledge(@PathVariable("materialId") Integer materialId) {
        try {
            Integer userId = SecurityUtil.getUserId();
            
            // 验证用户权限
            StudyMaterial material = studyMaterialService.getById(materialId);
            if (material == null || !material.getUserId().equals(userId)) {
                return Result.failed("资料不存在或无权操作");
            }
            
            Result<String> result = ragKnowledgeService.deleteMaterialKnowledge(materialId);
            log.info("删除资料知识库数据，用户ID：{}，资料ID：{}，结果：{}", userId, materialId, result.getMsg());
            
            return result;
            
        } catch (Exception e) {
            log.error("删除资料知识库数据失败，资料ID：{}，错误信息：{}", materialId, e.getMessage(), e);
            return Result.failed("删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取已解析的资料列表
     */
    @GetMapping("/materials")
    @ApiOperation("获取已解析的资料列表")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<Object> getParsedMaterials() {
        try {
            Integer userId = SecurityUtil.getUserId();
            
            // 获取用户的所有学习资料
            Result<List<StudyMaterialVO>> materialsResult = studyMaterialService.getMaterialsByUserId(userId);
            
            if (materialsResult.getCode() == 1 && materialsResult.getData() != null) {
                // 过滤已解析的资料（在知识库中有记录的）
                List<StudyMaterialVO> parsedMaterials = new ArrayList<>();
                for (StudyMaterialVO material : materialsResult.getData()) {
                    // 这里可以改为查询最新的任务状态
                    Result<RagParseTask> taskResult = ragParseTaskService.getLatestTaskByMaterial(material.getId());
                    if (taskResult.getData() != null && taskResult.getData().getTaskStatus() == 2) {
                        parsedMaterials.add(material);
                    }
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("total", parsedMaterials.size());
                result.put("materials", parsedMaterials);
                
                log.info("获取已解析资料列表，用户ID：{}，总数：{}", userId, parsedMaterials.size());
                return Result.success("获取成功", result);
            } else {
                return Result.failed("获取资料列表失败");
            }
            
        } catch (Exception e) {
            log.error("获取已解析资料列表失败，用户ID：{}，错误信息：{}", SecurityUtil.getUserId(), e.getMessage(), e);
            return Result.failed("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取资料的解析内容块
     */
    @GetMapping("/material/{materialId}/chunks")
    @ApiOperation("获取资料的解析内容块")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<Object> getMaterialChunks(@PathVariable("materialId") Integer materialId) {
        try {
            Integer userId = SecurityUtil.getUserId();
            
            // 验证权限
            StudyMaterial material = studyMaterialService.getById(materialId);
            if (material == null || !material.getUserId().equals(userId)) {
                return Result.failed("资料不存在或无权操作");
            }
            
            // 获取该资料的所有知识块
            List<RagKnowledge> chunks = ragKnowledgeService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RagKnowledge>()
                    .eq("user_id", userId)
                    .eq("study_material_id", materialId)
                    .orderByAsc("chunk_index")
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("materialName", material.getOriginalName());
            result.put("totalChunks", chunks.size());
            result.put("chunks", chunks.stream().map(chunk -> {
                Map<String, Object> chunkMap = new HashMap<>();
                chunkMap.put("index", chunk.getChunkIndex());
                chunkMap.put("content", chunk.getChunkContent());
                chunkMap.put("metadata", chunk.getMetadata());
                return chunkMap;
            }).collect(java.util.stream.Collectors.toList()));
            
            log.info("获取资料解析内容块，用户ID：{}，资料ID：{}，块数量：{}", userId, materialId, chunks.size());
            return Result.success("获取成功", result);
            
        } catch (Exception e) {
            log.error("获取资料解析内容块失败，资料ID：{}，错误信息：{}", materialId, e.getMessage(), e);
            return Result.failed("获取失败：" + e.getMessage());
        }
    }

    /**
     * 删除单个解析任务
     */
    @DeleteMapping("/task/{taskId}")
    @ApiOperation("删除单个解析任务")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> deleteTask(@PathVariable("taskId") Integer taskId) {
        try {
            Integer userId = SecurityUtil.getUserId();
            
            // 验证权限
            RagParseTask task = ragParseTaskService.getById(taskId);
            if (task == null) {
                return Result.failed("任务不存在");
            }
            
            if (!task.getUserId().equals(userId)) {
                return Result.failed("无权删除此任务");
            }
            
            // 删除任务
            boolean success = ragParseTaskService.removeById(taskId);
            if (success) {
                log.info("删除解析任务成功，用户ID：{}，任务ID：{}", userId, taskId);
                return Result.success("删除任务成功");
            } else {
                log.error("删除解析任务失败，用户ID：{}，任务ID：{}", userId, taskId);
                return Result.failed("删除任务失败");
            }
            
        } catch (Exception e) {
            log.error("删除解析任务失败，任务ID：{}，错误信息：{}", taskId, e.getMessage(), e);
            return Result.failed("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除解析任务
     */
    @DeleteMapping("/tasks/batch-delete")
    @ApiOperation("批量删除解析任务")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> deleteTasks(@RequestBody List<Integer> taskIds) {
        try {
            Integer userId = SecurityUtil.getUserId();
            
            if (taskIds == null || taskIds.isEmpty()) {
                return Result.failed("请选择要删除的任务");
            }
            
            // 验证权限并过滤用户的任务
            List<Integer> userTaskIds = new ArrayList<>();
            for (Integer taskId : taskIds) {
                RagParseTask task = ragParseTaskService.getById(taskId);
                if (task != null && task.getUserId().equals(userId)) {
                    userTaskIds.add(taskId);
                }
            }
            
            if (userTaskIds.isEmpty()) {
                return Result.failed("没有找到符合条件的任务");
            }
            
            // 批量删除
            boolean success = ragParseTaskService.removeByIds(userTaskIds);
            if (success) {
                log.info("批量删除解析任务成功，用户ID：{}，任务数量：{}", userId, userTaskIds.size());
                return Result.success("批量删除成功，共删除" + userTaskIds.size() + "个任务");
            } else {
                log.error("批量删除解析任务失败，用户ID：{}，任务数量：{}", userId, userTaskIds.size());
                return Result.failed("批量删除失败");
            }
            
        } catch (Exception e) {
            log.error("批量删除解析任务失败，错误信息：{}", e.getMessage(), e);
            return Result.failed("批量删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/all-materials")
    @ApiOperation("获取所有学习资料（包括解析状态）")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<Object> getAllMaterials() {
        try {
            Integer userId = SecurityUtil.getUserId();
            
            // 获取用户的所有学习资料
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
                    
                    // 检查解析状态
                    Result<RagParseTask> taskResult = ragParseTaskService.getLatestTaskByMaterial(material.getId());
                    boolean isParsed = taskResult.getData() != null && taskResult.getData().getTaskStatus() == 2;
                    materialInfo.put("isParsed", isParsed);
                    
                    resultList.add(materialInfo);
                }
                
                log.info("获取所有学习资料列表，用户ID：{}，总数：{}", userId, resultList.size());
                return Result.success("获取成功", resultList);
            } else {
                return Result.failed("获取资料列表失败");
            }
            
        } catch (Exception e) {
            log.error("获取所有学习资料列表失败，用户ID：{}，错误信息：{}", SecurityUtil.getUserId(), e.getMessage(), e);
            return Result.failed("获取失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin-grading/parse/{materialId}")
    @ApiOperation("管理员解析阅卷参考材料")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<Map<String, Object>> parseAdminGradingMaterial(@PathVariable("materialId") Integer materialId) {
        try {
            StudyMaterial material = studyMaterialService.getById(materialId);
            if (material == null) {
                return Result.failed("资料不存在");
            }

            Integer taskId = ragParseTaskService.createTask(material.getUserId(), materialId);

            Thread asyncThread = new Thread(() -> {
                try {
                    ragParseTaskService.updateProgress(taskId, 1, 0);
                    Result<String> result = ragKnowledgeService.parseAndStoreGradingMaterial(material, taskId);
                    if (result.getCode() == 1) {
                        log.info("管理员阅卷材料解析成功：{}", result.getMsg());
                    } else {
                        log.error("管理员阅卷材料解析失败：{}", result.getMsg());
                    }
                } catch (Exception e) {
                    log.error("管理员阅卷材料解析异常", e);
                    ragParseTaskService.markFailed(taskId, "解析异常：" + e.getMessage());
                }
            });
            asyncThread.setDaemon(true);
            asyncThread.start();

            Map<String, Object> response = new HashMap<>();
            response.put("taskId", taskId);
            response.put("message", "阅卷参考材料解析任务已提交");
            response.put("status", "pending");
            return Result.success("任务已提交", response);
        } catch (Exception e) {
            log.error("提交管理员阅卷解析任务失败", e);
            return Result.failed("提交任务失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin-grading/stats")
    @ApiOperation("获取管理员阅卷知识库统计")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<Object> getAdminGradingStats() {
        try {
            return ragKnowledgeService.getAdminGradingKnowledgeStats();
        } catch (Exception e) {
            log.error("获取管理员阅卷知识库统计失败", e);
            return Result.failed("获取统计信息失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/admin-grading/clear")
    @ApiOperation("清空管理员阅卷知识库")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> clearAdminGradingKnowledge() {
        try {
            return ragKnowledgeService.deleteAdminGradingKnowledge();
        } catch (Exception e) {
            log.error("清空管理员阅卷知识库失败", e);
            return Result.failed("清空失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin-grading/test")
    @ApiOperation("测试管理员阅卷RAG检索")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<Object> testAdminGradingSearch(@RequestParam("question") String question,
                                                   @RequestParam(value = "topK", defaultValue = "3") int topK) {
        try {
            Result<List<String>> searchResult = ragKnowledgeService.searchAdminGradingKnowledge(question, topK);
            Map<String, Object> testResult = new HashMap<>();
            testResult.put("question", question);
            testResult.put("topK", topK);
            testResult.put("foundCount", searchResult.getData() != null ? searchResult.getData().size() : 0);
            testResult.put("searchResults", searchResult.getData());
            return Result.success("检索测试完成", testResult);
        } catch (Exception e) {
            log.error("管理员阅卷RAG检索测试失败", e);
            return Result.failed("检索测试失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin-grading/materials")
    @ApiOperation("获取管理员可用的阅卷参考材料列表")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<Object> getAdminGradingMaterials() {
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
            return Result.failed("获取资料列表失败");
        } catch (Exception e) {
            log.error("获取管理员阅卷参考材料列表失败", e);
            return Result.failed("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin-grading/tasks")
    @ApiOperation("获取管理员阅卷解析任务列表")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<List<RagParseTask>> getAdminGradingTasks() {
        try {
            Integer userId = SecurityUtil.getUserId();
            return ragParseTaskService.getUserTasks(userId);
        } catch (Exception e) {
            log.error("获取管理员阅卷解析任务列表失败", e);
            return Result.failed("获取任务列表失败：" + e.getMessage());
        }
    }
}