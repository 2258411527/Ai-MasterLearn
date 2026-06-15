package cn.org.alan.exam.model.vo.study;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习资料视图对象
 *
 * @author AI Assistant
 * @since 2024
 */
@Data
public class StudyMaterialVO {
    
    /**
     * 主键ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 原始文件名
     */
    private String originalName;
    
    /**
     * 文件存储路径
     */
    private String filePath;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
    
    /**
     * 文件大小显示格式
     */
    private String fileSizeDisplay;
    
    /**
     * 文件图标
     */
    private String fileIcon;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 是否在电子展馆中展示
     */
    private Boolean showInGallery;
}