package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.user.UserAuditForm;
import cn.org.alan.exam.service.IUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.annotation.Resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户审核管理
 *
 * @Author AI Assistant
 * @Version 1.0
 * @Date 2026/05/01
 */
@Api(tags = "用户审核管理接口")
@RestController
@RequestMapping("/user-audit")
public class UserAuditController {

    @Resource
    private IUserService userService;

    /**
     * 获取待审核用户列表
     */
    @ApiOperation("获取待审核用户列表")
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('role_admin')")
    public Result<IPage<User>> getPendingUsers(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAuditStatus, 0) // 待审核
               .orderByDesc(User::getCreateTime);
        
        IPage<User> userPage = userService.page(page, wrapper);
        return Result.success("获取待审核用户列表成功", userPage);
    }

    /**
     * 审核通过用户
     */
    @ApiOperation("审核通过用户")
    @PostMapping("/approve/{userId}")
    @PreAuthorize("hasAuthority('role_admin')")
    public Result<String> approveUser(@PathVariable Integer userId, 
                                     @RequestBody(required = false) UserAuditForm auditForm) {
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.failed("用户不存在");
        }
        
        if (user.getAuditStatus() != 0) {
            return Result.failed("该用户不需要审核或已审核");
        }
        
        user.setAuditStatus(1); // 审核通过
        user.setAuditTime(LocalDateTime.now());
        user.setAuditorId(getCurrentUserId());
        
        if (auditForm != null && auditForm.getAuditRemark() != null) {
            user.setAuditRemark(auditForm.getAuditRemark());
        }
        
        userService.updateById(user);
        return Result.success("审核通过成功");
    }

    /**
     * 审核拒绝用户
     */
    @ApiOperation("审核拒绝用户")
    @PostMapping("/reject/{userId}")
    @PreAuthorize("hasAuthority('role_admin')")
    public Result<String> rejectUser(@PathVariable Integer userId, 
                                     @RequestBody(required = false) UserAuditForm auditForm) {
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.failed("用户不存在");
        }
        
        if (user.getAuditStatus() != 0) {
            return Result.failed("该用户不需要审核或已审核");
        }
        
        user.setAuditStatus(2); // 审核拒绝
        user.setAuditTime(LocalDateTime.now());
        user.setAuditorId(getCurrentUserId());
        
        if (auditForm != null && auditForm.getAuditRemark() != null) {
            user.setAuditRemark(auditForm.getAuditRemark());
        } else {
            user.setAuditRemark("审核未通过");
        }
        
        userService.updateById(user);
        return Result.success("审核拒绝成功");
    }

    /**
     * 获取所有审核记录
     */
    @ApiOperation("获取所有审核记录")
    @GetMapping("/records")
    @PreAuthorize("hasAuthority('role_admin')")
    public Result<IPage<User>> getAuditRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer auditStatus) {
        
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (auditStatus != null) {
            wrapper.eq(User::getAuditStatus, auditStatus);
        } else {
            wrapper.in(User::getAuditStatus, 0, 1, 2);
        }
        
        wrapper.orderByDesc(User::getAuditTime, User::getCreateTime);
        IPage<User> userPage = userService.page(page, wrapper);
        return Result.success("获取审核记录成功", userPage);
    }

    /**
     * 获取当前登录用户ID
     */
    private Integer getCurrentUserId() {
        // 这里需要根据实际的安全工具类获取当前用户ID
        // 暂时返回一个默认值，实际项目中需要实现
        return 1; // 管理员ID
    }
}