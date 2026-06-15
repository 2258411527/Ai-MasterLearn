package cn.org.alan.exam.service;

import cn.org.alan.exam.model.entity.AdminAudit;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IAdminAuditService extends IService<AdminAudit> {
    /**
     * 申请成为管理员
     */
    boolean applyForAdmin(Integer userId, Integer adminLevel);

    /**
     * 审核管理员申请
     */
    boolean auditAdminApplication(Integer auditId, Integer auditorId, Integer auditStatus, String auditRemark);

    /**
     * 获取待审核的管理员申请列表
     */
    java.util.List<AdminAudit> getPendingAuditList(Integer auditorId);
}