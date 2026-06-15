package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.StudyMaterial;
import cn.org.alan.exam.model.form.study.StudyMaterialForm;
import cn.org.alan.exam.model.vo.study.StudyMaterialVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 学习资料服务接口
 *
 * @author AI Assistant
 * @since 2024
 */
public interface IStudyMaterialService extends IService<StudyMaterial> {
    
    /**
     * 上传学习资料
     *
     * @param file 文件
     * @param userId 用户ID
     * @return 上传结果
     */
    Result<String> uploadMaterial(MultipartFile file, Integer userId);
    
    /**
     * 获取用户的学习资料列表
     *
     * @param userId 用户ID
     * @return 学习资料列表
     */
    Result<List<StudyMaterialVO>> getMaterialsByUserId(Integer userId);
    
    /**
     * 分页查询用户学习资料
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID
     * @param fileName 文件名
     * @return 分页结果
     */
    Result<IPage<StudyMaterialVO>> getMaterialPage(Integer pageNum, Integer pageSize, Integer userId, String fileName);
    
    /**
     * 下载学习资料
     *
     * @param id 资料ID
     * @param userId 用户ID
     * @param response HTTP响应
     */
    void downloadMaterial(Integer id, Integer userId, HttpServletResponse response);
    
    /**
     * 预览学习资料
     *
     * @param id 资料ID
     * @param userId 用户ID
     * @param response HTTP响应
     */
    void previewMaterial(Integer id, Integer userId, HttpServletResponse response);
    
    /**
     * 删除学习资料
     *
     * @param id 资料ID
     * @param userId 用户ID
     * @return 删除结果
     */
    Result<String> deleteMaterial(Integer id, Integer userId);
    
    /**
     * 获取所有用户的学习资料（电子展馆）
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param fileName 文件名
     * @param uploaderName 上传者姓名
     * @return 分页结果
     */
    Result<IPage<StudyMaterialVO>> getGalleryMaterials(Integer pageNum, Integer pageSize, String fileName, String uploaderName);
    
    /**
     * 将文件添加到个人资料库
     *
     * @param id 资料ID
     * @param userId 用户ID
     * @return 添加结果
     */
    Result<String> addToLibrary(Integer id, Integer userId);
    
    /**
     * 设置文件在电子展馆中的展示权限
     *
     * @param id 资料ID
     * @param userId 用户ID
     * @param showInGallery 是否在电子展馆中展示
     * @return 设置结果
     */
    Result<String> setGalleryPermission(Integer id, Integer userId, Boolean showInGallery);
    
    /**
     * 从个人资料库移除文件
     *
     * @param id 资料ID
     * @param userId 用户ID
     * @return 移除结果
     */
    Result<String> removeFromLibrary(Integer id, Integer userId);
}