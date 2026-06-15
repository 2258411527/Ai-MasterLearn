package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户备考画像实体类
 */
@Data
@TableName("t_user_profile")
public class UserProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private String targetExamType; // POSTGRADUATE, CIVIL_SERVICE
    private String identity;
    private String targetYear;
    private String dailyHours;
    private String currentProgress;

    // 考研专属
    private String examRound;
    private String degreeType;
    private String majorType;
    private String universityLevel;
    private String publicSubjects;
    private String professionalSubject;
    private String selfAssessment; // JSON

    // 考公专属
    private String civilExamType;
    private String targetRegion;
    private String positionCategory;
    private String prepExperience;

    // 通用
    private String weakModules; // JSON Array
    private String needs; // JSON Array
    private String aiPlan;
    private String aiSummary;
    private Integer planStatus; // 0-未生成, 1-已生成

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}