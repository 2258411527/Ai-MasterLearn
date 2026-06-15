package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.study.StudyMaterialVO;
import cn.org.alan.exam.service.IStudyMaterialService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 学习资料控制器
 *
 * @author AI Assistant
 * @since 2024
 */
@Api(tags = "学习资料相关接口")
@RestController
@RequestMapping("/study-material")
public class StudyMaterialController {

    @Resource
    private IStudyMaterialService studyMaterialService;

    /**
     * 上传学习资料
     *
     * @param file 文件
     * @return 上传结果
     */
    @ApiOperation("上传学习资料")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public Result<String> uploadMaterial(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.failed("请选择要上传的文件");
        }
        Integer userId = SecurityUtil.getUserId();
        return studyMaterialService.uploadMaterial(file, userId);
    }

    /**
     * 获取当前用户的学习资料列表
     *
     * @return 学习资料列表
     */
    @ApiOperation("获取当前用户的学习资料列表")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public Result<List<StudyMaterialVO>> getMaterials() {
        Integer userId = SecurityUtil.getUserId();
        return studyMaterialService.getMaterialsByUserId(userId);
    }

    /**
     * 分页查询当前用户的学习资料
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param fileName 文件名
     * @return 分页结果
     */
    @ApiOperation("分页查询当前用户的学习资料")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public Result<IPage<StudyMaterialVO>> getMaterialPage(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "fileName", required = false) String fileName) {
        Integer userId = SecurityUtil.getUserId();
        return studyMaterialService.getMaterialPage(pageNum, pageSize, userId, fileName);
    }

    /**
     * 下载学习资料
     *
     * @param id 资料ID
     * @param response HTTP响应
     */
    @ApiOperation("下载学习资料")
    @GetMapping("/download/{id}")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public void downloadMaterial(@PathVariable("id") Integer id, HttpServletResponse response) {
        Integer userId = SecurityUtil.getUserId();
        studyMaterialService.downloadMaterial(id, userId, response);
    }

    /**
     * 预览学习资料
     *
     * @param id 资料ID
     * @param response HTTP响应
     */
    @ApiOperation("预览学习资料")
    @GetMapping("/preview/{id}")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public void previewMaterial(@PathVariable("id") Integer id, HttpServletResponse response) {
        Integer userId = SecurityUtil.getUserId();
        studyMaterialService.previewMaterial(id, userId, response);
    }

    /**
     * 删除学习资料
     *
     * @param id 资料ID
     * @return 删除结果
     */
    @ApiOperation("删除学习资料")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public Result<String> deleteMaterial(@PathVariable("id") Integer id) {
        Integer userId = SecurityUtil.getUserId();
        return studyMaterialService.deleteMaterial(id, userId);
    }

    /**
     * 获取所有用户的学习资料（电子展馆）
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param fileName 文件名
     * @param uploaderName 上传者姓名
     * @return 分页结果
     */
    @ApiOperation("获取所有用户的学习资料（电子展馆）")
    @GetMapping("/gallery")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public Result<IPage<StudyMaterialVO>> getGalleryMaterials(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "12") Integer pageSize,
            @RequestParam(value = "fileName", required = false) String fileName,
            @RequestParam(value = "uploaderName", required = false) String uploaderName) {
        return studyMaterialService.getGalleryMaterials(pageNum, pageSize, fileName, uploaderName);
    }

    /**
     * 将文件添加到个人资料库
     *
     * @param id 资料ID
     * @return 添加结果
     */
    @ApiOperation("将文件添加到个人资料库")
    @PostMapping("/add-to-library/{id}")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> addToLibrary(@PathVariable("id") Integer id) {
        Integer userId = SecurityUtil.getUserId();
        return studyMaterialService.addToLibrary(id, userId);
    }

    /**
     * 从个人资料库移除文件
     *
     * @param id 资料ID
     * @return 移除结果
     */
    @ApiOperation("从个人资料库移除文件")
    @DeleteMapping("/remove-from-library/{id}")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> removeFromLibrary(@PathVariable("id") Integer id) {
        Integer userId = SecurityUtil.getUserId();
        return studyMaterialService.removeFromLibrary(id, userId);
    }

    /**
     * 设置文件在电子展馆中的展示权限
     *
     * @param id 资料ID
     * @param showInGallery 是否在电子展馆中展示
     * @return 设置结果
     */
    @ApiOperation("设置文件在电子展馆中的展示权限")
    @PutMapping("/set-gallery-permission/{id}")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> setGalleryPermission(@PathVariable("id") Integer id, 
                                              @RequestParam("showInGallery") Boolean showInGallery) {
        Integer userId = SecurityUtil.getUserId();
        return studyMaterialService.setGalleryPermission(id, userId, showInGallery);
    }
}