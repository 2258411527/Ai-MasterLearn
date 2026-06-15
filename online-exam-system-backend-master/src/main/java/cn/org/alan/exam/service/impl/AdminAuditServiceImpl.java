package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.AdminAuditMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.AdminAudit;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.service.IAdminAuditService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminAuditServiceImpl extends ServiceImpl<AdminAuditMapper, AdminAudit> implements IAdminAuditService {

    private final AdminAuditMapper adminAuditMapper;
    private final UserMapper userMapper;

    public AdminAuditServiceImpl(AdminAuditMapper adminAuditMapper, UserMapper userMapper) {
        this.adminAuditMapper = adminAuditMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public boolean applyForAdmin(Integer userId, Integer adminLevel) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getRoleId() != 3) {
            throw new RuntimeException("只有管理员角色可以申请管理员等级");
        }

        if (user.getAdminLevel() != null && user.getAdminLevel() != 0) {
            throw new RuntimeException("该用户已经是管理员，不能重复申请");
        }

        AdminAudit adminAudit = new AdminAudit();
        adminAudit.setUserId(userId);
        adminAudit.setAdminLevel(adminLevel);
        adminAudit.setAuditStatus(0);
        adminAudit.setCreateTime(LocalDateTime.now());
        adminAudit.setIsDeleted(0);

        return adminAuditMapper.insert(adminAudit) > 0;
    }

    @Override
    @Transactional
    public boolean auditAdminApplication(Integer auditId, Integer auditorId, Integer auditStatus, String auditRemark) {
        AdminAudit adminAudit = adminAuditMapper.selectById(auditId);
        if (adminAudit == null) {
            throw new RuntimeException("审核记录不存在");
        }

        if (adminAudit.getAuditStatus() != 0) {
            throw new RuntimeException("该申请已经审核过了");
        }

        User auditor = userMapper.selectById(auditorId);
        if (auditor == null) {
            throw new RuntimeException("审核人不存在");
        }

        if (auditor.getRoleId() != 3 || auditor.getAdminLevel() == null || auditor.getAdminLevel() == 0) {
            throw new RuntimeException("只有系统管理员可以审核管理员申请");
        }

        if (auditor.getAdminLevel() != 1) {
            throw new RuntimeException("只有系统管理员可以审核管理员申请");
        }

        adminAudit.setAuditorId(auditorId);
        adminAudit.setAuditStatus(auditStatus);
        adminAudit.setAuditRemark(auditRemark);
        adminAudit.setAuditTime(LocalDateTime.now());

        int updateResult = adminAuditMapper.updateById(adminAudit);

        if (updateResult > 0 && auditStatus == 1) {
            User applicant = userMapper.selectById(adminAudit.getUserId());
            if (applicant != null) {
                applicant.setAdminLevel(adminAudit.getAdminLevel());
                
                if (adminAudit.getAdminLevel() == 2) {
                    applicant.setMaxGradeCount(99);
                    applicant.setCanAddGrade(1);
                    applicant.setCanDelete(1);
                } else if (adminAudit.getAdminLevel() == 3) {
                    applicant.setMaxGradeCount(1);
                    applicant.setCanAddGrade(0);
                    applicant.setCanDelete(1);
                }
                
                userMapper.updateById(applicant);
            }
        }

        return updateResult > 0;
    }

    @Override
    public List<AdminAudit> getPendingAuditList(Integer auditorId) {
        User auditor = userMapper.selectById(auditorId);
        if (auditor == null) {
            throw new RuntimeException("审核人不存在");
        }

        if (auditor.getRoleId() != 3 || auditor.getAdminLevel() == null || auditor.getAdminLevel() != 1) {
            throw new RuntimeException("只有系统管理员可以查看待审核的管理员申请");
        }

        LambdaQueryWrapper<AdminAudit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminAudit::getAuditStatus, 0)
                .eq(AdminAudit::getIsDeleted, 0)
                .orderByDesc(AdminAudit::getCreateTime);

        return adminAuditMapper.selectList(queryWrapper);
    }
}