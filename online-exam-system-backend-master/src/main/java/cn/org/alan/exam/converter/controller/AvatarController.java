package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.service.IAvatarService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 头像管理控制器
 * 
 * @author AI Assistant
 * @version 1.0
 * @since 2026/05/01
 */
@Api(tags = "头像管理接口")
@RestController
@RequestMapping("/avatar")
@Slf4j
public class AvatarController {

    @Autowired
    private IAvatarService avatarService;

    /**
     * 上传头像
     */
    @ApiOperation("上传头像")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            log.info("开始上传头像，文件大小：{}字节", file.getSize());
            return avatarService.uploadAvatar(file);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.failed("头像上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户头像
     */
    @ApiOperation("获取用户头像")
    @GetMapping("/user/{userId}")
    public Result<String> getUserAvatar(@PathVariable Integer userId) {
        try {
            return avatarService.getUserAvatar(userId);
        } catch (Exception e) {
            log.error("获取用户头像失败，用户ID：{}", userId, e);
            return Result.failed("获取头像失败");
        }
    }

    /**
     * 获取当前用户头像
     */
    @ApiOperation("获取当前用户头像")
    @GetMapping("/current")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> getCurrentUserAvatar() {
        try {
            return avatarService.getCurrentUserAvatar();
        } catch (Exception e) {
            log.error("获取当前用户头像失败", e);
            return Result.failed("获取头像失败");
        }
    }

    /**
     * 直接显示头像图片（用于img标签src属性）
     */
    @ApiOperation("显示头像图片")
    @GetMapping("/display/{filename}")
    public void displayAvatar(@PathVariable String filename, HttpServletResponse response) {
        try {
            avatarService.displayAvatar(filename, response);
        } catch (Exception e) {
            log.error("显示头像失败，文件名：{}", filename, e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 重置为默认头像
     */
    @ApiOperation("重置为默认头像")
    @PutMapping("/reset")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> resetToDefaultAvatar() {
        try {
            return avatarService.resetToDefaultAvatar();
        } catch (Exception e) {
            log.error("重置头像失败", e);
            return Result.failed("重置头像失败");
        }
    }
}