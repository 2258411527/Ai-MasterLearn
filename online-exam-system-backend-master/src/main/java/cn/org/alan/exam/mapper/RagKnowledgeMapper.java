package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.RagKnowledge;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * RAG知识库Mapper接口
 *
 * @author AI Assistant
 * @since 2026
 */
public interface RagKnowledgeMapper extends BaseMapper<RagKnowledge> {
    
    /**
     * 根据用户ID查询知识库数据
     *
     * @param userId 用户ID
     * @return 知识库列表
     */
    @Select("SELECT * FROM rag_knowledge WHERE user_id = #{userId} ORDER BY created_time DESC")
    List<RagKnowledge> selectByUserId(@Param("userId") Integer userId);
    
    /**
     * 根据用户ID删除知识库数据
     *
     * @param userId 用户ID
     * @return 删除条数
     */
    @Delete("DELETE FROM rag_knowledge WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Integer userId);
    
    /**
     * 根据学习资料ID删除知识库数据
     *
     * @param studyMaterialId 学习资料ID
     * @return 删除条数
     */
    @Delete("DELETE FROM rag_knowledge WHERE study_material_id = #{studyMaterialId}")
    int deleteByStudyMaterialId(@Param("studyMaterialId") Integer studyMaterialId);
    
    /**
     * 统计用户的知识块数量
     *
     * @param userId 用户ID
     * @return 知识块数量
     */
    @Select("SELECT COUNT(*) FROM rag_knowledge WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Integer userId);
    
    /**
     * 统计用户的知识资料数量
     *
     * @param userId 用户ID
     * @return 资料数量
     */
    @Select("SELECT COUNT(DISTINCT study_material_id) FROM rag_knowledge WHERE user_id = #{userId}")
    int countMaterialsByUserId(@Param("userId") Integer userId);
    
    /**
     * 统计特定学习资料的知识块数量
     *
     * @param studyMaterialId 学习资料ID
     * @return 知识块数量
     */
    @Select("SELECT COUNT(*) FROM rag_knowledge WHERE study_material_id = #{studyMaterialId}")
    int countByStudyMaterialId(@Param("studyMaterialId") Integer studyMaterialId);

    /**
     * 根据用户ID和知识库类型查询
     */
    @Select("SELECT * FROM rag_knowledge WHERE user_id = #{userId} AND knowledge_type = #{knowledgeType} ORDER BY created_time DESC")
    List<RagKnowledge> selectByUserIdAndType(@Param("userId") Integer userId, @Param("knowledgeType") String knowledgeType);

    /**
     * 根据用户ID和知识库类型统计数量
     */
    @Select("SELECT COUNT(*) FROM rag_knowledge WHERE user_id = #{userId} AND knowledge_type = #{knowledgeType}")
    int countByUserIdAndType(@Param("userId") Integer userId, @Param("knowledgeType") String knowledgeType);

    /**
     * 根据用户ID和知识库类型统计资料数量
     */
    @Select("SELECT COUNT(DISTINCT study_material_id) FROM rag_knowledge WHERE user_id = #{userId} AND knowledge_type = #{knowledgeType}")
    int countMaterialsByUserIdAndType(@Param("userId") Integer userId, @Param("knowledgeType") String knowledgeType);

    /**
     * 根据用户ID和知识库类型删除
     */
    @Delete("DELETE FROM rag_knowledge WHERE user_id = #{userId} AND knowledge_type = #{knowledgeType}")
    int deleteByUserIdAndType(@Param("userId") Integer userId, @Param("knowledgeType") String knowledgeType);

    @Select("SELECT * FROM rag_knowledge WHERE knowledge_type = #{knowledgeType} ORDER BY created_time DESC")
    List<RagKnowledge> selectByType(@Param("knowledgeType") String knowledgeType);

    @Select("SELECT COUNT(*) FROM rag_knowledge WHERE knowledge_type = #{knowledgeType}")
    int countByType(@Param("knowledgeType") String knowledgeType);

    @Select("SELECT COUNT(DISTINCT study_material_id) FROM rag_knowledge WHERE knowledge_type = #{knowledgeType}")
    int countMaterialsByType(@Param("knowledgeType") String knowledgeType);

    @Delete("DELETE FROM rag_knowledge WHERE knowledge_type = #{knowledgeType}")
    int deleteByType(@Param("knowledgeType") String knowledgeType);
}