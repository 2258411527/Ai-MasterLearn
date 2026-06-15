package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.RagParseTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * RAG解析任务Mapper接口
 *
 * @author AI Assistant
 * @since 2026
 */
public interface RagParseTaskMapper extends BaseMapper<RagParseTask> {
    
    /**
     * 根据用户ID查询解析任务列表
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    @Select("SELECT * FROM rag_parse_task WHERE user_id = #{userId} ORDER BY created_time DESC")
    List<RagParseTask> selectByUserId(@Param("userId") Integer userId);
    
    /**
     * 根据学习资料ID查询最新的解析任务
     *
     * @param studyMaterialId 学习资料ID
     * @return 最新任务
     */
    @Select("SELECT * FROM rag_parse_task WHERE study_material_id = #{studyMaterialId} ORDER BY created_time DESC LIMIT 1")
    RagParseTask selectLatestByMaterialId(@Param("studyMaterialId") Integer studyMaterialId);
    
    /**
     * 更新任务进度
     *
     * @param taskId 任务ID
     * @param progress 进度百分比
     * @param processedPages 已处理页数
     * @return 更新条数
     */
    @Update("UPDATE rag_parse_task SET progress = #{progress}, processed_pages = #{processedPages}, updated_time = NOW() WHERE id = #{taskId}")
    int updateProgress(@Param("taskId") Integer taskId, 
                      @Param("progress") Integer progress,
                      @Param("processedPages") Integer processedPages);
    
    /**
     * 更新任务状态为完成
     *
     * @param taskId 任务ID
     * @param totalChunks 总分块数
     * @return 更新条数
     */
    @Update("UPDATE rag_parse_task SET task_status = 2, progress = 100, total_chunks = #{totalChunks}, end_time = NOW(), updated_time = NOW() WHERE id = #{taskId}")
    int updateToCompleted(@Param("taskId") Integer taskId, @Param("totalChunks") Integer totalChunks);
    
    /**
     * 更新任务状态为失败
     *
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     * @return 更新条数
     */
    @Update("UPDATE rag_parse_task SET task_status = 3, error_message = #{errorMessage}, end_time = NOW(), updated_time = NOW() WHERE id = #{taskId}")
    int updateToFailed(@Param("taskId") Integer taskId, @Param("errorMessage") String errorMessage);
    
    /**
     * 统计用户的任务数量
     *
     * @param userId 用户ID
     * @return 任务数量
     */
    @Select("SELECT COUNT(*) FROM rag_parse_task WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Integer userId);
}
