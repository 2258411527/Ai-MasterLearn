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
 * 考公专属信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_user_kaogong_info")
@ApiModel(value = "UserKaogongInfo对象", description = "考公专属信息")
public class UserKaogongInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("考试类型：国考/省考/选调生/事业单位")
    private String examCategory;

    @ApiModelProperty("意向报考地区：本省/全国不限/特定省份")
    private String targetRegion;

    @ApiModelProperty("意向岗位类别：综合文职/行政执法/基层乡镇/不限")
    private String positionCategory;

    @ApiModelProperty("备考经历：首次备考/曾参加过笔试/进过面试")
    private String preparationExperience;

    @ApiModelProperty("行测基础：零基础/初学刷题/有实战基础")
    private String xingceLevel;

    @ApiModelProperty("申论基础：从未动笔/偶尔练习/经常写大作文")
    private String shenlunLevel;

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