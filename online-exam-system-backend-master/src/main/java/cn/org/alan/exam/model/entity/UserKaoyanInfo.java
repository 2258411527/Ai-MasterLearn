package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考研专属信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_user_kaoyan_info")
@ApiModel(value = "UserKaoyanInfo对象", description = "考研专属信息")
public class UserKaoyanInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("考研届数：一战/二战/三战及以上")
    private String attemptCount;

    @ApiModelProperty("报考类型：学硕/专硕")
    private String degreeType;

    @ApiModelProperty("报考方式：本专业报考/跨专业报考")
    private String majorType;

    @ApiModelProperty("目标院校层次：双非/省属重点/211/985")
    private String targetUniversityLevel;

    @ApiModelProperty("英语科目：英语一/英语二")
    private String englishSubject;

    @ApiModelProperty("数学科目：数学一/数学二/数学三/不考数学")
    private String mathSubject;

    @ApiModelProperty("专业课名称")
    private String majorCourseName;

    @ApiModelProperty("英语基础：零基础/一般/较好")
    private String englishLevel;

    @ApiModelProperty("数学基础：零基础/一般/较好")
    private String mathLevel;

    @ApiModelProperty("专业课基础：零基础/遗忘较多/基础扎实")
    private String majorLevel;

    @ApiModelProperty("薄弱模块（JSON格式存储）")
    private String weakModules;

    @ApiModelProperty("备考需求倾向（JSON格式存储）")
    private String studyNeeds;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}