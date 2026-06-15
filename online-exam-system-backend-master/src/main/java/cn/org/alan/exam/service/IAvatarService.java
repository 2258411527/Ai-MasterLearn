package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 头像服务接口
 * 
 * @author AI Assistant
 * @version 1.0
 * @since 2026/05/01
 */
public interface IAvatarService {

    /**
     * 上传头像
     * 
     * @param file 头像文件
     * @return 上传结果
     */
    Result<String> uploadAvatar(MultipartFile file);

    /**
     * 获取用户头像URL
     * 
     * @param userId 用户ID
     * @return 头像URL
     */
    Result<String> getUserAvatar(Integer userId);

    /**
     * 获取当前用户头像URL
     * 
     * @return 当前用户头像URL
     */
    Result<String> getCurrentUserAvatar();

    /**
     * 直接显示头像图片
     * 
     * @param filename 文件名
     * @param response HTTP响应
     */
    void displayAvatar(String filename, HttpServletResponse response);

    /**
     * 重置为默认头像
     * 
     * @return 重置结果
     */
    Result<String> resetToDefaultAvatar();

    /**
     * 验证头像文件
     * 
     * @param file 文件
     * @return 验证结果
     */
    boolean validateAvatarFile(MultipartFile file);

    /**
     * 获取默认头像URL
     * 
     * @return 默认头像URL
     */
    String getDefaultAvatarUrl();
}