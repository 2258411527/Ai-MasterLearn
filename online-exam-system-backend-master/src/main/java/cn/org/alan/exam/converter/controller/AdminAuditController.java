package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.AdminAudit;
import cn.org.alan.exam.service.IAdminAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(tags = "管理员审核相关接口")
@RequestMapping("/admin-audit")
public class AdminAuditController {

    @Resource
    private IAdminAuditService adminAuditService;

    @ApiOperation("申请成为管理员")
    @PostMapping("/apply")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> applyForAdmin(@RequestParam Integer userId, @RequestParam Integer adminLevel) {
        try {
            boolean success = adminAuditService.applyForAdmin(userId, adminLevel);
            if (success) {
                return Result.success("申请成功，等待系统管理员审核");
            } else {
                return Result.failed("申请失败");
            }
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        }
    }

    @ApiOperation("审核管理员申请")
    @PostMapping("/audit")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> auditAdminApplication(@RequestParam Integer auditId,
                                             @RequestParam Integer auditorId,
                                             @RequestParam Integer auditStatus,
                                             @RequestParam(required = false) String auditRemark) {
        try {
            boolean success = adminAuditService.auditAdminApplication(auditId, auditorId, auditStatus, auditRemark);
            if (success) {
                if (auditStatus == 1) {
                    return Result.success("审核通过");
                } else {
                    return Result.success("审核拒绝");
                }
            } else {
                return Result.failed("审核失败");
            }
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        }
    }

    @ApiOperation("获取待审核的管理员申请列表")
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<List<AdminAudit>> getPendingAuditList(@RequestParam Integer auditorId) {
        try {
            List<AdminAudit> list = adminAuditService.getPendingAuditList(auditorId);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        }
    }
}