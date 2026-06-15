package cn.org.alan.exam.utils.file.impl;

import cn.org.alan.exam.utils.file.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 本地文件存储工具类
 * 支持文件去重：相同内容的文件只存储一次
 * 
 * @author AI Assistant
 * @version 1.0
 * @since 2026/05/01
 */
@Service
@ConditionalOnProperty(name = "online-exam.storage.type", havingValue = "local")
@Slf4j
public class LocalFileUtil implements FileService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Override
    public String upload(MultipartFile file) throws IOException {
        log.info("开始保存文件到本地，文件名：{}，文件大小：{}字节", file.getOriginalFilename(), file.getSize());
        
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            log.info("创建上传目录：{}", uploadDir);
            boolean dirCreated = uploadDirectory.mkdirs();
            if (!dirCreated) {
                log.error("无法创建上传目录：{}", uploadDir);
                throw new IOException("无法创建上传目录：" + uploadDir);
            }
        }

        if (!uploadDirectory.canWrite()) {
            log.error("上传目录没有写入权限：{}", uploadDir);
            throw new IOException("上传目录没有写入权限：" + uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            log.error("上传文件时获取文件名失败，为null");
            throw new IOException("上传文件时获取文件名失败，为null");
        }
        
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));

        byte[] fileBytes = file.getBytes();
        String fileMd5 = computeMd5(fileBytes);
        String md5FileName = fileMd5 + fileExtension;
        
        Path existingFile = Paths.get(uploadDir, md5FileName);
        if (Files.exists(existingFile)) {
            log.info("文件已存在（MD5={}），跳过存储，直接返回已有URL", fileMd5);
            String url = "/uploads/" + md5FileName;
            return url;
        }

        Files.write(existingFile, fileBytes);
        
        log.info("新文件保存成功，MD5={}，路径：{}", fileMd5, existingFile.toAbsolutePath().toString());

        String url = "/uploads/" + md5FileName;
        log.info("生成文件访问URL：{}", url);
        
        return url;
    }

    private String computeMd5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    @Override
    public boolean isImage(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        String[] imageExtensions = {"png", "jpg", "jpeg", "bmp", "gif", "webp"};
        return Arrays.asList(imageExtensions).contains(extension);
    }

    @Override
    public boolean isOverSize(MultipartFile file) {
        return file.getSize() > 2 * 1024 * 1024;
    }
}