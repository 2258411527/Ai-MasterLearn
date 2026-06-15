package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.StudyMaterial;
import cn.org.alan.exam.model.vo.study.StudyMaterialVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学习资料Mapper接口
 *
 * @author AI Assistant
 * @since 2024
 */
public interface StudyMaterialMapper extends BaseMapper<StudyMaterial> {
    
    /**
     * 根据用户ID查询学习资料
     *
     * @param userId 用户ID
     * @return 学习资料列表
     */
    List<StudyMaterialVO> selectByUserId(@Param("userId") Integer userId);
    
    /**
     * 分页查询用户学习资料
     *
     * @param page 分页对象
     * @param userId 用户ID
     * @param fileName 文件名（模糊查询）
     * @return 分页结果
     */
    Page<StudyMaterialVO> selectStudyMaterialPage(Page<StudyMaterialVO> page, 
                                                  @Param("userId") Integer userId,
                                                  @Param("fileName") String fileName);
    
    /**
     * 根据ID和用户ID查询学习资料
     *
     * @param id 资料ID
     * @param userId 用户ID
     * @return 学习资料
     */
    StudyMaterialVO selectByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);
    
    /**
     * 分页查询所有用户的学习资料（电子展馆）
     *
     * @param page 分页对象
     * @param fileName 文件名（模糊查询）
     * @param uploaderName 上传者姓名（模糊查询）
     * @return 分页结果
     */
    Page<StudyMaterialVO> selectGalleryMaterialsPage(Page<StudyMaterialVO> page, 
                                                     @Param("fileName") String fileName,
                                                     @Param("uploaderName") String uploaderName);
}