package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.count.ClassCountResult;
import cn.org.alan.exam.model.vo.user.UserVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

/**
 * 用户服务Mapper
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 移除教师和班级的关联
     *
     * @param userIds 用户列表
     * @return 返回修改条数
     */
    Integer removeUserGrade(@Param("userIds") List<Integer> userIds);

    /**
     * 批量添加用户
     *
     * @param list 用户id列表
     * @return 返回删除条数
     */
    Integer insertBatchUser(List<User> list);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 返回UserVO
     */
    UserVO info(Integer userId);

    /**
     * 分页获取用户信息
     *
     * @param page        分页信息
     * @param gradeId     班级Id
     * @param realName    真实姓名
     * @param roleId      角色Id
     * @param gradeIdList 班级Id集
     * @return 查询结果集
     */
    IPage<UserVO> pagingUser(IPage<UserVO> page, @Param("gradeId") Integer gradeId, @Param("realName") String realName, @Param("userId") Integer userId, @Param("roleId") Integer roleId, @Param("gradeIdList") List<Integer> gradeIdList);

    /**
     * 用户退出班级
     *
     * @param gradeId 班级ID
     * @param userId  用户ID
     * @return 返回更新条数
     */
    Integer userExitGrade(@Param("gradeId") Integer gradeId, @Param("userId") Integer userId);

    /**
     * 获得管理员ID列表
     *
     * @return 返回管理员id列表
     */
    List<Integer> getAdminList();

}
