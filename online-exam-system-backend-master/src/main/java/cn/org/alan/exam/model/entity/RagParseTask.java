package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * RAG解析任务实体类 - 用于跟踪异步解析进度
 *
 * @author AI Assistant
 * @since 2026
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("rag_parse_task")
public class RagParseTask {
    
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
     * 学习资料ID
     */
    @TableField("study_material_id")
    private Integer studyMaterialId;
    
    /**
     * 任务状态: 0-等待处理, 1-处理中, 2-已完成, 3-失败
     */
    @TableField("task_status")
    private Integer taskStatus;
    
    /**
     * 进度百分比(0-100)
     */
    @TableField("progress")
    private Integer progress;
    
    /**
     * 总页数(仅PDF)
     */
    @TableField("total_pages")
    private Integer totalPages;
    
    /**
     * 已处理页数
     */
    @TableField("processed_pages")
    private Integer processedPages;
    
    /**
     * 总分块数
     */
    @TableField("total_chunks")
    private Integer totalChunks;
    
    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;
    
    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
