package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天消息Mapper
 */
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 查询两个用户之间的聊天记录
     */
    @Select("SELECT cm.*, u1.real_name as from_user_name, u1.avatar as from_user_avatar, " +
            "u2.real_name as to_user_name, u2.avatar as to_user_avatar " +
            "FROM chat_message cm " +
            "LEFT JOIN t_user u1 ON cm.from_user_id = u1.id " +
            "LEFT JOIN t_user u2 ON cm.to_user_id = u2.id " +
            "WHERE ((cm.from_user_id = #{userId1} AND cm.to_user_id = #{userId2}) OR " +
            "(cm.from_user_id = #{userId2} AND cm.to_user_id = #{userId1})) " +
            "AND cm.is_deleted = 0 " +
            "ORDER BY cm.create_time ASC " +
            "LIMIT #{limit}")
    List<ChatMessage> selectChatHistory(@Param("userId1") Integer userId1, 
                                       @Param("userId2") Integer userId2, 
                                       @Param("limit") Integer limit);

    /**
     * 更新消息为已读状态
     */
    @Select("UPDATE chat_message SET is_read = 1, update_time = NOW() " +
            "WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId} AND is_read = 0")
    void markMessagesAsRead(@Param("fromUserId") Integer fromUserId, 
                           @Param("toUserId") Integer toUserId);

    /**
     * 查询未读消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_message " +
            "WHERE to_user_id = #{userId} AND is_read = 0 AND is_deleted = 0")
    Integer countUnreadMessages(@Param("userId") Integer userId);
}