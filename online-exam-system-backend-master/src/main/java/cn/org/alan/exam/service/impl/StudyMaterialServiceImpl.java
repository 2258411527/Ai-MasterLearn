package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.StudyMaterialMapper;
import cn.org.alan.exam.model.entity.StudyMaterial;
import cn.org.alan.exam.model.vo.study.StudyMaterialVO;
import cn.org.alan.exam.service.IStudyMaterialService;
import cn.org.alan.exam.service.IRagKnowledgeService;
import cn.org.alan.exam.service.IRagParseTaskService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 学习资料服务实现类
 *
 * @author AI Assistant
 * @since 2024
 */
@Service
@Slf4j
public class StudyMaterialServiceImpl extends ServiceImpl<StudyMaterialMapper, StudyMaterial> implements IStudyMaterialService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
    
    @Value("${file.skip-db-save:false}")
    private boolean skipDbSave;
    
    @Resource
    private StudyMaterialMapper studyMaterialMapper;
    
    @Resource
    private IRagKnowledgeService ragKnowledgeService;
    
    @Resource
    private IRagParseTaskService ragParseTaskService;

    // 允许的文件类型
    private static final String[] ALLOWED_FILE_TYPES = {"pdf", "doc", "docx", "xls", "xlsx"};
    
    // 最大文件大小：200MB
    private static final long MAX_FILE_SIZE = 200 * 1024 * 1024;

    @Override
    @Transactional
    public Result<String> uploadMaterial(MultipartFile file, Integer userId) {
        String originalFilename = null;
        String filePath = null;
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.failed("请选择要上传的文件");
            }
            
            originalFilename = file.getOriginalFilename();
            log.info("开始上传文件，用户ID：{}，文件名：{}，文件大小：{}字节", userId, originalFilename, file.getSize());
            
            // 验证文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                return Result.failed("文件大小不能超过200MB");
            }
            
            // 验证文件类型
            String fileExtension = getFileExtension(originalFilename);
            if (!isAllowedFileType(fileExtension)) {
                return Result.failed("只支持上传PDF、Word、Excel格式的文件");
            }
            
            // 创建上传目录
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                log.info("创建上传目录：{}", uploadDir);
                boolean dirCreated = uploadDirectory.mkdirs();
                if (!dirCreated) {
                    return Result.failed("无法创建上传目录");
                }
            }
            
            // 检查目录权限
            if (!uploadDirectory.canWrite()) {
                return Result.failed("上传目录没有写入权限");
            }
            
            // 生成唯一文件名
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
            
            // 确保使用绝对路径
            File uploadDirFile = new File(uploadDir);
            String absoluteUploadDir = uploadDirFile.getAbsolutePath();
            filePath = Paths.get(absoluteUploadDir, uniqueFileName).toString();
            log.info("生成文件路径：{}", filePath);
            
            // 保存文件
            Path destination = Paths.get(filePath);
            Files.copy(file.getInputStream(), destination);
            log.info("文件保存成功，路径：{}", filePath);
            
            // 检查是否跳过数据库保存
            if (skipDbSave) {
                log.warn("已启用跳过数据库保存模式，文件已保存到磁盘但未记录到数据库");
                return Result.success("文件上传成功（注：数据库保存已禁用）");
            }
            
            // 保存到数据库
            StudyMaterial material = new StudyMaterial();
            material.setUserId(userId);
            material.setFileName(uniqueFileName);
            material.setOriginalName(originalFilename);
            material.setFilePath(filePath);
            material.setFileSize(file.getSize());
            material.setFileType(fileExtension.toUpperCase());
            material.setUploadTime(LocalDateTime.now());
            
            // 设置权限控制：学生上传的文件默认不在电子展馆展示，管理员/教师上传的文件默认展示
            boolean isStudent = isStudentRole(userId);
            material.setShowInGallery(!isStudent); // 学生默认不展示，管理员/教师默认展示
            
            boolean success = false;
            Exception dbException = null;
            
            // 带重试的数据库保存逻辑
            for (int retryCount = 0; retryCount < 3; retryCount++) {
                try {
                    success = save(material);
                    if (success) {
                        break;
                    }
                } catch (Exception e) {
                    dbException = e;
                    log.warn("数据库保存失败，尝试重试 ({}/3)，错误：{}", retryCount + 1, e.getMessage());
                    if (retryCount < 2) {
                        try {
                            Thread.sleep(1000); // 延迟1秒后重试
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            
            if (success) {
                log.info("文件上传成功，用户ID：{}，文件名：{}", userId, originalFilename);
                
                // 不再自动解析到RAG知识库，用户需要在AI问答页面手动选择解析
                log.info("文件上传成功，ID：{}，文件名：{}", material.getId(), originalFilename);
                log.info("提示：如需使用RAG功能，请在AI问答页面手动选择解析此资料");
                
                return Result.success("文件上传成功");
            } else {
                // 删除已上传的文件
                try {
                    Files.deleteIfExists(destination);
                } catch (Exception e) {
                    log.warn("清理上传失败的文件异常：{}", e.getMessage());
                }
                
                String errorMsg = "数据库保存失败";
                if (dbException != null) {
                    if (dbException.getMessage().contains("Communications link failure") || 
                        dbException.getMessage().contains("Connect timed out")) {
                        errorMsg = "数据库连接失败，请检查MySQL服务是否运行。若要临时禁用数据库，请配置环境变量 SKIP_DB_SAVE=true";
                    } else {
                        errorMsg = "数据库保存失败：" + dbException.getMessage();
                    }
                }
                return Result.failed(errorMsg);
            }
            
        } catch (Exception e) {
            log.error("文件上传失败，用户ID：{}，文件名：{}，错误信息：{}", userId, originalFilename, e.getMessage(), e);
            
            // 清理已上传的文件
            if (filePath != null) {
                try {
                    Files.deleteIfExists(Paths.get(filePath));
                } catch (Exception deleteEx) {
                    log.warn("清理上传文件失败：{}", deleteEx.getMessage());
                }
            }
            
            return Result.failed("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<StudyMaterialVO>> getMaterialsByUserId(Integer userId) {
        try {
            List<StudyMaterialVO> materials = studyMaterialMapper.selectByUserId(userId);
            return Result.success("查询成功", materials);
        } catch (Exception e) {
            log.error("获取学习资料失败", e);
            return Result.failed("获取失败");
        }
    }

    @Override
    public Result<IPage<StudyMaterialVO>> getMaterialPage(Integer pageNum, Integer pageSize, Integer userId, String fileName) {
        try {
            Page<StudyMaterialVO> page = new Page<>(pageNum, pageSize);
            IPage<StudyMaterialVO> result = studyMaterialMapper.selectStudyMaterialPage(page, userId, fileName);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("分页查询学习资料失败", e);
            return Result.failed("查询失败");
        }
    }

    @Override
    public void downloadMaterial(Integer id, Integer userId, HttpServletResponse response) {
        try {
            log.info("开始下载文件，ID：{}，用户ID：{}", id, userId);
            StudyMaterialVO material = studyMaterialMapper.selectByIdAndUserId(id, userId);
            if (material == null) {
                log.warn("文件不存在，ID：{}，用户ID：{}", id, userId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            log.info("找到文件信息：{}，路径：{}", material.getOriginalName(), material.getFilePath());
            
            File file = new File(material.getFilePath());
            if (!file.exists()) {
                log.warn("物理文件不存在，路径：{}", material.getFilePath());
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            log.info("文件存在，大小：{}字节", file.length());
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + 
                new String(material.getOriginalName().getBytes("UTF-8"), "ISO-8859-1"));
            response.setContentLengthLong(file.length());
            
            // 写入文件流
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
                log.info("文件下载完成，ID：{}，用户ID：{}", id, userId);
            }
            
        } catch (Exception e) {
            log.error("文件下载失败，ID：{}，用户ID：{}，错误信息：{}", id, userId, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void previewMaterial(Integer id, Integer userId, HttpServletResponse response) {
        try {
            log.info("开始预览文件，ID：{}，用户ID：{}", id, userId);
            StudyMaterialVO material = studyMaterialMapper.selectByIdAndUserId(id, userId);
            if (material == null) {
                log.warn("文件不存在，ID：{}，用户ID：{}", id, userId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            log.info("找到文件信息：{}，路径：{}，类型：{}", material.getOriginalName(), material.getFilePath(), material.getFileType());
            
            File file = new File(material.getFilePath());
            if (!file.exists()) {
                log.warn("物理文件不存在，路径：{}", material.getFilePath());
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            log.info("文件存在，大小：{}字节", file.length());
            
            // 检查文件类型是否支持预览
            boolean previewSupported = isPreviewSupported(material.getFileType());
            log.info("文件类型{}支持预览：{}", material.getFileType(), previewSupported);
            
            // 根据文件类型设置Content-Type
            String contentType = getContentType(material.getFileType());
            log.info("设置Content-Type：{}", contentType);
            response.setContentType(contentType);
            
            // 如果不支持预览，强制下载
            String contentDisposition = previewSupported ? 
                "inline; filename=" : "attachment; filename=";
            response.setHeader("Content-Disposition", contentDisposition + 
                new String(material.getOriginalName().getBytes("UTF-8"), "ISO-8859-1"));
            
            // 写入文件流
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
                log.info("文件预览完成，ID：{}，用户ID：{}", id, userId);
            }
            
        } catch (Exception e) {
            log.error("文件预览失败，ID：{}，用户ID：{}，错误信息：{}", id, userId, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 检查用户是否是管理员
     * 这里简化实现，实际项目中应该查询用户角色信息
     */
    private boolean isAdminUser(Integer userId) {
        // 简化实现：假设用户ID为1的是管理员
        // 实际项目中应该查询数据库中的用户角色信息
        return userId != null && userId == 1;
    }

    @Override
    @Transactional
    public Result<String> deleteMaterial(Integer id, Integer userId) {
        try {
            StudyMaterial material = getById(id);
            if (material == null) {
                return Result.failed("文件不存在");
            }
            
            // 检查权限：管理员可以删除所有文件，普通用户只能删除自己的文件
            boolean hasPermission = material.getUserId().equals(userId) || isAdminUser(userId);
            if (!hasPermission) {
                return Result.failed("无权删除此文件");
            }
            
            // 先清理相关的RAG数据
            log.info("开始清理学习资料的RAG数据，资料ID：{}", id);
            
            try {
                // 1. 删除该资料相关的知识库数据
                Result<String> deleteResult = ragKnowledgeService.deleteMaterialKnowledge(id);
                if (deleteResult.getCode() == 1) {
                    log.info("成功删除知识库数据，资料ID：{}", id);
                }
                
                // 2. 删除该资料相关的解析任务
                // 查询所有相关任务并删除
                List<cn.org.alan.exam.model.entity.RagParseTask> tasks = ragParseTaskService.lambdaQuery()
                    .eq(cn.org.alan.exam.model.entity.RagParseTask::getStudyMaterialId, id)
                    .list();
                
                if (!tasks.isEmpty()) {
                    List<Integer> taskIds = tasks.stream()
                        .map(cn.org.alan.exam.model.entity.RagParseTask::getId)
                        .collect(java.util.stream.Collectors.toList());
                    
                    boolean deleteTasksSuccess = ragParseTaskService.removeByIds(taskIds);
                    if (deleteTasksSuccess) {
                        log.info("成功删除RAG解析任务，数量：{}", tasks.size());
                    }
                }
                
            } catch (Exception ragException) {
                log.warn("清理RAG数据时出现异常，但不影响资料删除：{}", ragException.getMessage());
            }
            
            // 逻辑删除学习资料
            boolean success = removeById(id);
            if (success) {
                // 可选：物理删除文件
                // try {
                //     Files.deleteIfExists(Paths.get(material.getFilePath()));
                // } catch (Exception fileException) {
                //     log.warn("物理删除文件失败：{}", fileException.getMessage());
                // }
                log.info("删除学习资料成功，ID：{}，用户ID：{}", id, userId);
                return Result.success("删除成功");
            } else {
                return Result.failed("删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除学习资料失败", e);
            return Result.failed("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (StringUtils.hasText(filename)) {
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < filename.length() - 1) {
                return filename.substring(dotIndex + 1).toLowerCase();
            }
        }
        return "";
    }
    
    /**
     * 检查文件类型是否允许
     */
    private boolean isAllowedFileType(String fileExtension) {
        for (String allowedType : ALLOWED_FILE_TYPES) {
            if (allowedType.equalsIgnoreCase(fileExtension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 根据文件类型获取Content-Type
     */
    private String getContentType(String fileType) {
        switch (fileType.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "doc":
            case "docx":
                return "application/msword";
            case "xls":
            case "xlsx":
                return "application/vnd.ms-excel";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 检查文件类型是否支持预览
     */
    private boolean isPreviewSupported(String fileType) {
        // 目前只支持PDF文件直接预览
        // Word和Excel文件在浏览器中预览会出现乱码
        return "pdf".equalsIgnoreCase(fileType);
    }
    
    /**
     * 判断用户是否为学生角色
     */
    private boolean isStudentRole(Integer userId) {
        // 这里需要根据实际情况实现用户角色判断
        // 暂时返回false，假设所有用户都不是学生
        // 实际项目中应该查询用户角色表
        // 为了确保教师/管理员上传的文件始终公开，这里返回false
        return false;
    }

    @Override
    public Result<IPage<StudyMaterialVO>> getGalleryMaterials(Integer pageNum, Integer pageSize, String fileName, String uploaderName) {
        try {
            Page<StudyMaterialVO> page = new Page<>(pageNum, pageSize);
            IPage<StudyMaterialVO> result = studyMaterialMapper.selectGalleryMaterialsPage(page, fileName, uploaderName);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("获取电子展馆资料失败", e);
            return Result.failed("获取失败");
        }
    }

    @Override
    @Transactional
    public Result<String> addToLibrary(Integer id, Integer userId) {
        try {
            // 查询原始文件信息
            StudyMaterial originalMaterial = this.getById(id);
            if (originalMaterial == null) {
                return Result.failed("文件不存在");
            }
            
            // 检查用户是否已经添加过该文件
            StudyMaterial existingMaterial = this.lambdaQuery()
                .eq(StudyMaterial::getUserId, userId)
                .eq(StudyMaterial::getOriginalName, originalMaterial.getOriginalName())
                .one();
            
            if (existingMaterial != null) {
                return Result.failed("该文件已存在于您的资料库中");
            }
            
            // 创建新的学习资料记录
            StudyMaterial newMaterial = new StudyMaterial();
            newMaterial.setUserId(userId);
            newMaterial.setFileName(originalMaterial.getFileName());
            newMaterial.setOriginalName(originalMaterial.getOriginalName());
            newMaterial.setFilePath(originalMaterial.getFilePath());
            newMaterial.setFileSize(originalMaterial.getFileSize());
            newMaterial.setFileType(originalMaterial.getFileType());
            newMaterial.setUploadTime(LocalDateTime.now());
            newMaterial.setIsDeleted(false);
            
            // 学生添加的文件默认不在电子展馆展示
            newMaterial.setShowInGallery(false);
            
            boolean saved = this.save(newMaterial);
            if (saved) {
                return Result.success("文件已成功添加到您的资料库");
            } else {
                return Result.failed("添加文件失败");
            }
        } catch (Exception e) {
            log.error("添加文件到个人资料库失败", e);
            return Result.failed("添加失败");
        }
    }

    @Override
    @Transactional
    public Result<String> removeFromLibrary(Integer id, Integer userId) {
        try {
            // 检查原始文件是否存在
            StudyMaterial originalMaterial = this.getById(id);
            if (originalMaterial == null) {
                return Result.failed("文件不存在");
            }
            
            // 查找用户在资料库中的对应文件
            // 通过文件名匹配来找到用户收藏的文件副本
            StudyMaterial userMaterial = this.lambdaQuery()
                .eq(StudyMaterial::getUserId, userId)
                .eq(StudyMaterial::getOriginalName, originalMaterial.getOriginalName())
                .eq(StudyMaterial::getIsDeleted, false)
                .one();
            
            if (userMaterial == null) {
                return Result.failed("该文件不在您的资料库中");
            }
            
            // 逻辑删除用户资料库中的文件（设置is_deleted为true）
            userMaterial.setIsDeleted(true);
            boolean deleted = this.updateById(userMaterial);
            
            if (deleted) {
                return Result.success("文件已从您的资料库移除");
            } else {
                return Result.failed("移除文件失败");
            }
        } catch (Exception e) {
            log.error("从个人资料库移除文件失败", e);
            return Result.failed("移除失败");
        }
    }

    @Override
    @Transactional
    public Result<String> setGalleryPermission(Integer id, Integer userId, Boolean showInGallery) {
        try {
            // 检查文件是否存在且属于当前用户
            StudyMaterial material = this.lambdaQuery()
                .eq(StudyMaterial::getId, id)
                .eq(StudyMaterial::getUserId, userId)
                .eq(StudyMaterial::getIsDeleted, false)
                .one();
            
            if (material == null) {
                return Result.failed("文件不存在或您没有权限操作此文件");
            }
            
            // 更新展示权限
            material.setShowInGallery(showInGallery);
            boolean updated = this.updateById(material);
            
            if (updated) {
                String status = showInGallery ? "展示" : "隐藏";
                return Result.success("文件在电子展馆中的展示状态已设置为" + status);
            } else {
                return Result.failed("设置权限失败");
            }
        } catch (Exception e) {
            log.error("设置电子展馆权限失败", e);
            return Result.failed("设置权限失败");
        }
    }
}