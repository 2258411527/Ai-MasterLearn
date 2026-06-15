package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.service.IAvatarService;
import cn.org.alan.exam.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

/**
 * 头像服务实现类
 * 
 * @author AI Assistant
 * @version 1.0
 * @since 2026/05/01
 */
@Service
@Slf4j
public class AvatarServiceImpl implements IAvatarService {

    @Autowired
    private UserMapper userMapper;

    @Value("${avatar.upload-dir:./avatars}")
    private String uploadDir;

    @Value("${avatar.max-size:2097152}")
    private long maxSize; // 2MB

    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
    private static final String DEFAULT_AVATAR = "/avatar/display/default-avatar.jpg";

    @Override
    public Result<String> uploadAvatar(MultipartFile file) {
        try {
            // 验证文件
            if (!validateAvatarFile(file)) {
                return Result.failed("头像文件格式或大小不符合要求");
            }

            // 获取当前用户ID
            Integer userId = SecurityUtil.getUserId();
            if (userId == null) {
                return Result.failed("用户未登录");
            }

            // 确保上传目录存在
            ensureUploadDirectory();

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path destination = Paths.get(uploadDir, uniqueFilename);
            Files.copy(file.getInputStream(), destination);

            // 生成访问URL
            String avatarUrl = "/avatar/display/" + uniqueFilename;

            // 更新数据库
            User user = new User();
            user.setId(userId);
            user.setAvatar(avatarUrl);
            int rows = userMapper.updateById(user);

            if (rows > 0) {
                log.info("用户 {} 头像上传成功，文件名：{}", userId, uniqueFilename);
                return Result.success("头像上传成功", avatarUrl);
            } else {
                // 删除已上传的文件
                Files.deleteIfExists(destination);
                return Result.failed("更新用户头像失败");
            }

        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.failed("头像上传失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> getUserAvatar(Integer userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.failed("用户不存在");
            }

            String avatarUrl = user.getAvatar();
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                avatarUrl = getDefaultAvatarUrl();
            }

            return Result.success("获取头像成功", avatarUrl);
        } catch (Exception e) {
            log.error("获取用户头像失败，用户ID：{}", userId, e);
            return Result.failed("获取头像失败");
        }
    }

    @Override
    public Result<String> getCurrentUserAvatar() {
        try {
            Integer userId = SecurityUtil.getUserId();
            if (userId == null) {
                return Result.failed("用户未登录");
            }
            return getUserAvatar(userId);
        } catch (Exception e) {
            log.error("获取当前用户头像失败", e);
            return Result.failed("获取头像失败");
        }
    }

    @Override
    public void displayAvatar(String filename, HttpServletResponse response) {
        try {
            // 安全检查：防止路径遍历攻击
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            File avatarFile;
            if ("default-avatar.jpg".equals(filename)) {
                // 默认头像
                avatarFile = new File("src/main/resources/static/default-avatar.jpg");
                if (!avatarFile.exists()) {
                    // 创建默认头像文件
                    createDefaultAvatar();
                }
            } else {
                // 用户上传的头像
                avatarFile = new File(uploadDir, filename);
            }

            if (!avatarFile.exists()) {
                // 如果文件不存在，返回默认头像
                avatarFile = new File("src/main/resources/static/default-avatar.jpg");
                if (!avatarFile.exists()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }

            // 设置响应头
            response.setContentType(getContentType(filename));
            response.setHeader("Cache-Control", "public, max-age=86400"); // 缓存1天

            // 输出文件内容
            try (FileInputStream fis = new FileInputStream(avatarFile);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

        } catch (Exception e) {
            log.error("显示头像失败，文件名：{}", filename, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<String> resetToDefaultAvatar() {
        try {
            Integer userId = SecurityUtil.getUserId();
            if (userId == null) {
                return Result.failed("用户未登录");
            }

            User user = new User();
            user.setId(userId);
            user.setAvatar(getDefaultAvatarUrl());
            int rows = userMapper.updateById(user);

            if (rows > 0) {
                log.info("用户 {} 重置为默认头像", userId);
                return Result.success("重置头像成功", getDefaultAvatarUrl());
            } else {
                return Result.failed("重置头像失败");
            }
        } catch (Exception e) {
            log.error("重置头像失败", e);
            return Result.failed("重置头像失败");
        }
    }

    @Override
    public boolean validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 检查文件大小
        if (file.getSize() > maxSize) {
            log.warn("头像文件过大：{}字节", file.getSize());
            return false;
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.lastIndexOf(".") == -1) {
            return false;
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        return Arrays.asList(ALLOWED_EXTENSIONS).contains(extension);
    }

    @Override
    public String getDefaultAvatarUrl() {
        return DEFAULT_AVATAR;
    }

    private void ensureUploadDirectory() throws IOException {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("无法创建头像上传目录：" + uploadDir);
            }
            log.info("创建头像上传目录：{}", uploadDir);
        }
    }

    private String getContentType(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        } else if (filename.endsWith(".bmp")) {
            return "image/bmp";
        } else if (filename.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    private void createDefaultAvatar() {
        // 这里可以创建一个简单的默认头像
        // 实际项目中应该放置一个真实的默认头像图片
        log.info("创建默认头像文件");
    }
}