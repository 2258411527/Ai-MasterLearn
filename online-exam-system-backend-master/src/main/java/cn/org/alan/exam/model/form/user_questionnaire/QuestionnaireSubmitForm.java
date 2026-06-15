package cn.org.alan.exam.model.form.user_questionnaire;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 问卷提交表单
 */
@Data
public class QuestionnaireSubmitForm {
    
    // 通用必答信息
    @NotNull(message = "考试类型不能为空")
    private String examType;
    
    @NotNull(message = "目标年份不能为空")
    private Integer targetYear;
    
    @NotNull(message = "备考身份不能为空")
    private String studyIdentity;
    
    @NotNull(message = "每日学习时长不能为空")
    private String dailyStudyHours;
    
    @NotNull(message = "当前备考进度不能为空")
    private String currentProgress;
    
    // 考研专属信息
    private String attemptCount;
    private String degreeType;
    private String majorType;
    private String targetUniversityLevel;
    private String englishSubject;
    private String mathSubject;
    private String majorCourseName;
    private String englishLevel;
    private String mathLevel;
    private String majorLevel;
    private String weakModules;
    private String studyNeeds;
    
    // 考公专属信息
    private String examCategory;
    private String targetRegion;
    private String positionCategory;
    private String preparationExperience;
    private String xingceLevel;
    private String shenlunLevel;
    private String kaogongWeakModules;
    private String kaogongStudyNeeds;
}