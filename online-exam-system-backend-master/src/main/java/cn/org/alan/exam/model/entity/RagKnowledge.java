package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * RAG知识库实体类
 *
 * @author AI Assistant
 * @since 2026
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("rag_knowledge")
public class RagKnowledge {
    
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
     * 文本块内容
     */
    @TableField("chunk_content")
    private String chunkContent;
    
    /**
     * 文本块索引
     */
    @TableField("chunk_index")
    private Integer chunkIndex;
    
    /**
     * 向量嵌入（JSON格式存储）
     */
    @TableField("embedding_vector")
    private String embeddingVector;
    
    /**
     * 元数据（JSON格式）
     */
    @TableField("metadata")
    private String metadata;

    /**
     * 知识库类型: study=学习知识库 grading=阅卷参考库
     */
    @TableField("knowledge_type")
    private String knowledgeType;
    
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