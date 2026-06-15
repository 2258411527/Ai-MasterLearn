package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.UserConverter;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.user.UserForm;
import cn.org.alan.exam.model.vo.user.UserVO;
import cn.org.alan.exam.service.IFileService;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.service.IUserService;
import cn.org.alan.exam.utils.DateTimeUtil;
import cn.org.alan.exam.utils.SecurityUtil;
import cn.org.alan.exam.utils.excel.ExcelUtils;
import cn.org.alan.exam.utils.file.FileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * 用户服务实现类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private HttpServletRequest request;
    @Resource
    private UserConverter userConverter;
    @Resource
    private GradeMapper gradeMapper;
    @Resource
    private UserGradeMapper userGradeMapper;
    @Resource
    private IFileService fileService;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private UserBookMapper userBookMapper;
    @Resource
    private UserExerciseRecordMapper userExerciseRecordMapper;
    @Resource
    private ExerciseRecordMapper exerciseRecordMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private StudyMaterialMapper studyMaterialMapper;
    @Resource
    private DiscussionMapper discussionMapper;
    @Resource
    private ReplyMapper replyMapper;
    @Resource
    private RagKnowledgeMapper ragKnowledgeMapper;
    @Resource
    private FriendRelationMapper friendRelationMapper;
    @Resource
    private ChatMessageMapper chatMessageMapper;
    @Resource
    private AiChatHistoryMapper aiChatHistoryMapper;
    @Resource
    private UserDailyLoginDurationMapper userDailyLoginDurationMapper;


    /**
     * 创建用户，教师只能创建学生，管理员可以创建教师和学生
     *
     * @param userForm
     * @return
     */
    @Override
    public Result<String> createUser(UserForm userForm) {
        // 设置默认密码
        userForm.setPassword("123456");
        // 获取角色
        Integer roleCode = SecurityUtil.getRoleCode();
        // 教师只能创建学生
        if (roleCode == 2) {
            userForm.setRoleId(1);
        }
        if(userForm.getRoleId()==2&&userForm.getGradeId()!=null){
            throw new ServiceRuntimeException("教师无法设置单一班级");
        }
        // 避免管理员创建用户不传递角色
        if (userForm.getRoleId() == null || userForm.getRoleId() == 0) {
            throw new ServiceRuntimeException("未选择用户角色");
        }
        User user = userConverter.fromToEntity(userForm);
        // 调用Mapper插入用户
        userMapper.insert(user);
        return Result.success("用户创建成功");

    }

    @Override
    public Result<String> updatePassword(UserForm userForm) {
        Integer userId = SecurityUtil.getUserId();
        if (!userForm.getNewPassword().equals(userForm.getCheckedPassword())) {
            throw new ServiceRuntimeException("两次密码不一致");
        }
        if (!userForm.getOriginPassword().equals(userMapper.selectById(userId).getPassword())) {
            throw new ServiceRuntimeException("旧密码错误");
        }
        // 设置新密码
        userForm.setPassword(userForm.getNewPassword());
        userForm.setId(userId);
        // 转还为User实体
        User user = userConverter.fromToEntity(userForm);
        // 调用mapper更新用户密码
        int updated = userMapper.updateById(user);
        // 密码修改成功清除redis的token，让用户重新登录
        if (updated > 0) {
            stringRedisTemplate.delete("token:" + request.getSession().getId());
            return Result.success("修改成功，请重新登录");
        }
        throw new ServiceRuntimeException("旧密码错误");

    }

    @Override
    @Transactional
    public Result<String> deleteBatchByIds(String ids) {
        List<Integer> userIds = Arrays.stream(ids.split(",")).map(Integer::parseInt).collect(java.util.stream.Collectors.toList());
        
        // 获取当前登录用户ID
        Integer currentUserId = SecurityUtil.getUserId();
        User currentUser = userMapper.selectById(currentUserId);
        
        if (currentUser == null) {
            throw new ServiceRuntimeException("当前用户不存在");
        }
        
        // 检查当前用户是否是管理员
        if (currentUser.getRoleId() != 3) {
            throw new ServiceRuntimeException("只有管理员可以删除用户");
        }
        
        // 检查当前用户的管理员等级
        Integer currentAdminLevel = currentUser.getAdminLevel();
        if (currentAdminLevel == null || currentAdminLevel == 0) {
            throw new ServiceRuntimeException("当前用户不是管理员");
        }
        
        // 检查是否包含系统管理员（adminLevel = 1）
        for (Integer userId : userIds) {
            User user = userMapper.selectById(userId);
            if (user != null && user.getAdminLevel() != null && user.getAdminLevel() == 1) {
                throw new ServiceRuntimeException("无法删除系统管理员");
            }
            
            // 检查删除权限
            if (user != null && user.getCanDelete() != null && user.getCanDelete() == 0) {
                // 如果当前用户不是系统管理员，不能删除不可删除的用户
                if (currentAdminLevel != 1) {
                    throw new ServiceRuntimeException("您没有权限删除该用户");
                }
            }
            
            // 普通管理员（adminLevel = 2或3）权限相同，可以删除其他普通管理员和学生
            if (currentAdminLevel == 2 || currentAdminLevel == 3) {
                // 普通管理员不能删除系统管理员（adminLevel = 1）
                if (user.getRoleId() == 3 && user.getAdminLevel() != null && user.getAdminLevel() == 1) {
                    throw new ServiceRuntimeException("普通管理员不能删除系统管理员");
                }
            }
        }
        
        if (userIds.isEmpty()) {
            throw new ServiceRuntimeException("删除数据库时未传入用户Id");
        }

        for (Integer userId : userIds) {
            userGradeMapper.delete(new LambdaQueryWrapper<UserGrade>().eq(UserGrade::getUId, userId));
            userExamsScoreMapper.delete(new LambdaQueryWrapper<UserExamsScore>().eq(UserExamsScore::getUserId, userId));
            userBookMapper.delete(new LambdaQueryWrapper<UserBook>().eq(UserBook::getUserId, userId));
            userExerciseRecordMapper.delete(new LambdaQueryWrapper<UserExerciseRecord>().eq(UserExerciseRecord::getUserId, userId));
            exerciseRecordMapper.delete(new LambdaQueryWrapper<ExerciseRecord>().eq(ExerciseRecord::getUserId, userId));
            examQuAnswerMapper.delete(new LambdaQueryWrapper<ExamQuAnswer>().eq(ExamQuAnswer::getUserId, userId));
            studyMaterialMapper.delete(new LambdaQueryWrapper<StudyMaterial>().eq(StudyMaterial::getUserId, userId));
            discussionMapper.delete(new LambdaQueryWrapper<Discussion>().eq(Discussion::getUserId, userId));
            replyMapper.delete(new LambdaQueryWrapper<Reply>().eq(Reply::getUserId, userId));
            ragKnowledgeMapper.delete(new LambdaQueryWrapper<RagKnowledge>().eq(RagKnowledge::getUserId, userId));
            friendRelationMapper.delete(new LambdaQueryWrapper<FriendRelation>().eq(FriendRelation::getUserId, userId));
            chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getFromUserId, userId));
            aiChatHistoryMapper.delete(new LambdaQueryWrapper<AiChatHistory>().eq(AiChatHistory::getUserId, userId));
            userDailyLoginDurationMapper.delete(new LambdaQueryWrapper<UserDailyLoginDuration>().eq(UserDailyLoginDuration::getUserId, userId));
        }

        Integer row = userMapper.deleteBatchIds(userIds);
        if (row < 1) {
            throw new ServiceRuntimeException("删除数据库时失败，条数<1");
        }
        return Result.success("删除成功");
    }

    @SneakyThrows(Exception.class)
    @Override
    @Transactional
    public Result<String> importUsers(MultipartFile file) {
        // 文件类型判断
        if (!ExcelUtils.isExcel(Objects.requireNonNull(file.getOriginalFilename()))) {
            throw new ServiceRuntimeException("文件类型必须是xls或xlsx");
        }
        // 读取文件
        List<UserForm> list = ExcelUtils.readMultipartFile(file, UserForm.class);
        // 参数补充
        list.forEach(userForm -> {
            // 设置默认密码
            userForm.setPassword("123456");
            userForm.setCreateTime(DateTimeUtil.getDateTime());
            if (userForm.getRoleId() == null) {
                // 没有设置角色默认为学生
                userForm.setRoleId(1);
            }
        });
        // 判断条数是否过多
        if (list.size() > 300) {
            throw new ServiceRuntimeException("表中最多存放300条数据");
        }
        userMapper.insertBatchUser(userConverter.listFromToEntity(list));
        return Result.success("用户导入成功");
    }

    /**
     * 获取用户个人信息
     *
     * @return
     */
    @Override
    public Result<UserVO> info() {
        // 获取用户信息
        Integer userId = SecurityUtil.getUserId();
        UserVO userVo = userMapper.info(userId);
        // 将密码去除
        userVo.setPassword(null);
        return Result.success("获取用户信息成功", userVo);
    }

    /**
     * 用户加入班级，只有学生才能加入班级
     *
     * @param code
     * @return
     */
    @Override
    public Result<String> joinGrade(String code) {
        // 获取班级信息
        Integer userId = SecurityUtil.getUserId();
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>().eq(Grade::getCode, code);
        Grade grade = gradeMapper.selectOne(wrapper);
        if (Objects.isNull(grade)) {
            throw new ServiceRuntimeException("班级口令不存在");
        }
        User user = new User();
        user.setId(userId);
        user.setGradeId(grade.getId());
        int updated = userMapper.updateById(user);
        if (updated > 0) {
            return Result.success("加入班级：" + grade.getGradeName() + "成功");
        }
        throw new ServiceRuntimeException("加入班级失败,加入数据库时失败");
    }

    @Override
    public Result<String> adminJoinGrade(String code) {
        // 获取当前用户信息
        Integer userId = SecurityUtil.getUserId();
        User currentUser = userMapper.selectById(userId);
        
        if (currentUser == null) {
            throw new ServiceRuntimeException("当前用户不存在");
        }
        
        // 检查是否是管理员
        if (currentUser.getRoleId() != 3) {
            throw new ServiceRuntimeException("只有管理员可以加入班级");
        }
        
        // 检查管理员等级
        Integer adminLevel = currentUser.getAdminLevel();
        if (adminLevel == null || adminLevel == 0) {
            throw new ServiceRuntimeException("当前用户不是管理员");
        }
        
        // 系统管理员可以加入多个班级
        if (adminLevel == 1) {
            // 系统管理员不受班级权限控制，可以加入任意班级
            LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>().eq(Grade::getCode, code);
            Grade grade = gradeMapper.selectOne(wrapper);
            if (Objects.isNull(grade)) {
                throw new ServiceRuntimeException("班级口令不存在");
            }
            User user = new User();
            user.setId(userId);
            user.setGradeId(grade.getId());
            int updated = userMapper.updateById(user);
            if (updated > 0) {
                return Result.success("加入班级：" + grade.getGradeName() + "成功");
            }
            throw new ServiceRuntimeException("加入班级失败");
        }
        
        // 普通管理员（adminLevel = 2或3）权限相同，可以加入多个班级
        if (adminLevel == 2 || adminLevel == 3) {
            // 普通管理员可以加入多个班级
            LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>().eq(Grade::getCode, code);
            Grade grade = gradeMapper.selectOne(wrapper);
            if (Objects.isNull(grade)) {
                throw new ServiceRuntimeException("班级口令不存在");
            }
            User user = new User();
            user.setId(userId);
            user.setGradeId(grade.getId());
            int updated = userMapper.updateById(user);
            if (updated > 0) {
                return Result.success("加入班级：" + grade.getGradeName() + "成功");
            }
            throw new ServiceRuntimeException("加入班级失败");
        }
        
        throw new ServiceRuntimeException("管理员等级错误");
    }

    @Override
    public Result<IPage<UserVO>> pagingUser(Integer pageNum, Integer pageSize, Integer gradeId, String realName, Integer auditStatus) {
        IPage<UserVO> page = new Page<>(pageNum, pageSize);
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        
        // 如果是管理员，需要查询审核状态
        if (roleCode == 3 && auditStatus != null) {
            // 管理员查询特定审核状态的用户
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getIsDeleted, 0);
            
            if (auditStatus != null) {
                wrapper.eq(User::getAuditStatus, auditStatus);
            }
            
            if (gradeId != null && gradeId != 0) {
                wrapper.eq(User::getGradeId, gradeId);
            }
            
            if (realName != null && !realName.isEmpty()) {
                wrapper.like(User::getRealName, realName);
            }
            
            wrapper.orderByDesc(User::getCreateTime);
            
            // 创建正确的分页对象类型用于查询
            IPage<User> userPageParam = new Page<>(pageNum, pageSize);
            IPage<User> userPage = this.page(userPageParam, wrapper);
            
            // 转换为UserVO
            List<UserVO> userVOs = userPage.getRecords().stream().map(user -> {
                UserVO userVO = new UserVO();
                userVO.setId(user.getId());
                userVO.setUserName(user.getUserName());
                userVO.setRealName(user.getRealName());
                userVO.setRoleId(user.getRoleId());
                userVO.setCreateTime(user.getCreateTime());
                userVO.setAuditStatus(user.getAuditStatus());
                userVO.setAuditRemark(user.getAuditRemark());
                
                // 查询班级名称
                if (user.getGradeId() != null) {
                    Grade grade = gradeMapper.selectById(user.getGradeId());
                    if (grade != null) {
                        userVO.setGradeName(grade.getGradeName());
                    }
                }
                
                return userVO;
            }).collect(Collectors.toList());
            
            IPage<UserVO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
            resultPage.setRecords(userVOs);
            return Result.success("分页获取用户信息成功", resultPage);
        }
        
        // 原有逻辑保持不变（教师和没有审核状态筛选的管理员）
        if (roleCode == 2) {
            // 如果是教师，要先查询教师加入了那些班级，根据教师到所有班级，查询所有班级下的用户
            List<Integer> gradeIdList = userGradeMapper.getGradeIdListByUserId(userId);
            if(gradeIdList.isEmpty()){
                throw new ServiceRuntimeException("教师还没加入班级暂无数据");
            }
            page = userMapper.pagingUser(page, gradeId, realName, userId, 1, gradeIdList);
        } else {
            // 管理员直接查询所有用户
            page = userMapper.pagingUser(page, gradeId, realName, userId, null, null);
        }
        
        // 为管理员添加审核状态信息
        if (roleCode == 3 && page.getRecords() != null) {
            for (UserVO userVO : page.getRecords()) {
                User user = this.getById(userVO.getId());
                if (user != null) {
                    userVO.setAuditStatus(user.getAuditStatus());
                    userVO.setAuditRemark(user.getAuditRemark());
                }
            }
        }
        
        return Result.success("分页获取用户信息成功", page);
    }

    @Transactional
    @Override
    public Result<String> uploadAvatar(MultipartFile file) {
        Integer userId = SecurityUtil.getUserId();
        log.info("用户 {} 开始上传头像，文件名：{}，文件大小：{}字节", userId, file.getOriginalFilename(), file.getSize());
        
        try {
            // 1.上传图片
            Result<String> result = fileService.uploadImage(file);
            if (result.getCode() == 0) {
                log.error("用户 {} 图片上传失败，返回码为0", userId);
                throw new ServiceRuntimeException("图片上传失败,上传图片代码code为0");
            }
            
            // 2.设置数据库头像地址
            String url = result.getData();
            log.info("用户 {} 图片上传成功，URL：{}", userId, url);
            
            User user = new User();
            user.setId(userId);
            user.setAvatar(url);
            Integer row = userMapper.updateById(user);
            
            if (row > 0) {
                log.info("用户 {} 头像更新成功，数据库记录已更新", userId);
                return Result.success("上传成功", url);
            } else {
                log.error("用户 {} 头像更新失败，数据库更新行数为0", userId);
                throw new ServiceRuntimeException("图片上传失败,修改用户表头像地址条数<=0");
            }
        } catch (Exception e) {
            log.error("用户 " + userId + " 头像上传异常", e);
            throw new ServiceRuntimeException("头像上传失败：" + e.getMessage());
        }
    }


}