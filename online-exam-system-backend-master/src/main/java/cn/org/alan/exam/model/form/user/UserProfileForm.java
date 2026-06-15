package cn.org.alan.exam.model.form.user;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 用户备考画像问卷表单
 */
@Data
public class UserProfileForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private String targetExamType; // POSTGRADUATE, CIVIL_SERVICE, BOTH
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
    private String selfAssessment;

    // 考公专属
    private String civilExamType;
    private String targetRegion;
    private String positionCategory;
    private String prepExperience;

    // 通用（前端传数组，后端转为JSON字符串存储）
    private List<String> weakModules;
    private List<String> needs;
}
