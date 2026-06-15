package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.common.group.UserGroup;
import cn.org.alan.exam.model.form.user.UserForm;
import cn.org.alan.exam.model.vo.user.UserVO;
import cn.org.alan.exam.service.IUserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理
 *
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/25 15:50
 */
@Api(tags = "用户管理相关接口")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private IUserService iUserService;

    /**
     * 获取用户个人信息
     *
     * @return
     */
    @ApiOperation("获取用户个人信息")
    @GetMapping("/info")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<UserVO> info() {
        return iUserService.info();
    }


    /**
     * 创建用户，教师只能创建学生，管理员可以创建教师和学生
     *
     * @param userForm
     * @return
     */
    @ApiOperation("创建用户")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> createUser(@Validated(UserGroup.CreateUserGroup.class) @RequestBody UserForm userForm) {
        return iUserService.createUser(userForm);
    }

    /**
     * 用户修改密码
     *
     * @param userForm
     * @return
     */
    @ApiOperation("用户修改密码")
    @PutMapping
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> updatePassword(@Validated(UserGroup.UpdatePasswordGroup.class) @RequestBody UserForm userForm) {
        return iUserService.updatePassword(userForm);
    }

    /**
     * 批量删除用户
     *
     * @param ids 删除用户ID
     * @return
     */
    @ApiOperation("批量删除用户")
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteBatchByIds(@PathVariable("ids") String ids) {
        return iUserService.deleteBatchByIds(ids);
    }

    /**
     * Excel导入用户数据
     *
     * @param file EXCEL文件
     * @return
     */
    @ApiOperation("Excel导入用户数据")
    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> importUsers(@RequestParam("file") MultipartFile file) {
        return iUserService.importUsers(file);
    }


    /**
     * 用户加入班级，只有学生才能加入班级
     *
     * @param code 班级代码
     * @return
     */
    @ApiOperation("用户加入班级")
    @PutMapping("/grade/join")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> joinGrade(@RequestParam("code") String code) {
        return iUserService.joinGrade(code);
    }

    /**
     * 管理员加入班级
     *
     * @param code 班级代码
     * @return
     */
    @ApiOperation("管理员加入班级")
    @PutMapping("/admin/grade/join")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> adminJoinGrade(@RequestParam("code") String code) {
        return iUserService.adminJoinGrade(code);
    }

    /**
     * 教师和管理员 用户管理 分页获取用户信息
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param gradeId  班级ID
     * @param realName 真实姓名
     * @param auditStatus 审核状态
     * @return
     */
    @ApiOperation("分页获取用户信息")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<UserVO>> pagingUser(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                            @RequestParam(value = "gradeId", required = false) Integer gradeId,
                                            @RequestParam(value = "realName", required = false) String realName,
                                            @RequestParam(value = "auditStatus", required = false) Integer auditStatus) {
        return iUserService.pagingUser(pageNum, pageSize, gradeId, realName, auditStatus);
    }

    /**
     * 用户上传头像
     *
     * @param file 头像文件
     * @return 返回头像地址
     */
    @ApiOperation("用户上传头像")
    @PutMapping("/uploadAvatar")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile file) {
        log.info("接收到头像上传请求，文件名：{}，文件大小：{}字节", file.getOriginalFilename(), file.getSize());
        try {
            Result<String> result = iUserService.uploadAvatar(file);
            log.info("头像上传处理完成，结果码：{}，消息：{}", result.getCode(), result.getMsg());
            return result;
        } catch (Exception e) {
            log.error("头像上传异常", e);
            throw e;
        }
    }
}