package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.FriendRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友关系Mapper
 */
public interface FriendRelationMapper extends BaseMapper<FriendRelation> {

    /**
     * 根据用户ID查询好友列表
     */
    @Select("SELECT fr.*, u.real_name as friend_name, u.avatar as friend_avatar, u.role_id as friend_role " +
            "FROM friend_relation fr " +
            "LEFT JOIN t_user u ON fr.friend_id = u.id " +
            "WHERE fr.user_id = #{userId} AND fr.status = 1 AND fr.is_deleted = 0")
    List<FriendRelation> selectFriendListByUserId(@Param("userId") Integer userId);

    /**
     * 检查好友关系是否存在
     */
    @Select("SELECT COUNT(*) FROM friend_relation " +
            "WHERE ((user_id = #{userId} AND friend_id = #{friendId}) OR (user_id = #{friendId} AND friend_id = #{userId})) " +
            "AND status = 1 AND is_deleted = 0")
    Integer checkFriendRelationExists(@Param("userId") Integer userId, @Param("friendId") Integer friendId);

    /**
     * 查询待审核的好友请求
     */
    @Select("SELECT fr.*, u.real_name as user_name, u.avatar as user_avatar, u.role_id as user_role " +
            "FROM friend_relation fr " +
            "LEFT JOIN t_user u ON fr.user_id = u.id " +
            "WHERE fr.friend_id = #{userId} AND fr.status = 0 AND fr.is_deleted = 0")
    List<FriendRelation> selectPendingFriendRequests(@Param("userId") Integer userId);
}