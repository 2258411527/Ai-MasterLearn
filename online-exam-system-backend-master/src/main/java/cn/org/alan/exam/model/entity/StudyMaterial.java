package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 学习资料实体类
 *
 * @author AI Assistant
 * @since 2024
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_study_material")
public class StudyMaterial {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Integer userId;
    
    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;
    
    /**
     * 原始文件名
     */
    @TableField("original_name")
    private String originalName;
    
    /**
     * 文件存储路径
     */
    @TableField("file_path")
    private String filePath;
    
    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;
    
    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;
    
    /**
     * 上传时间
     */
    @TableField(value = "upload_time", fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;
    
    /**
     * 是否删除(0-否,1-是)
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;
    
    /**
     * 是否在电子展馆中展示(0-否,1-是)
     */
    @TableField("show_in_gallery")
    private Boolean showInGallery;

}