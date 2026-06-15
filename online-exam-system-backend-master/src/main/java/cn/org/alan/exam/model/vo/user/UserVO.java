package cn.org.alan.exam.model.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/31 13:14
 */
@Data
public class UserVO {

    private Integer id;
    // 用户名称
    private String userName;
    // 真实姓名
    private String realName;
    // 用户角色
    private Integer roleId;
    // 密码
    private String password;
    // 头像
    private String avatar;
    // 加入的班级名称
    private String gradeName;
    // 班级ID
    private Integer gradeId;
    // 班级创建人Id
    private Integer userId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private String status;
    
    // 审核状态：0-待审核，1-审核通过，2-审核拒绝
    private Integer auditStatus;
    
    // 审核备注
    private String auditRemark;
    
    // 审核时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
    // 审核人ID
    private Integer auditorId;
    
    // 管理员等级：0-非管理员，1-系统管理员，2-一级管理员，3-二级管理员
    private Integer adminLevel;
    
    // 最大可加入班级数量：0-无限制，1-一个班级，99-多个班级
    private Integer maxGradeCount;
    
    // 是否可以添加班级：0-否，1-是
    private Integer canAddGrade;
    
    // 是否可删除：0-否，1-是
    private Integer canDelete;
}