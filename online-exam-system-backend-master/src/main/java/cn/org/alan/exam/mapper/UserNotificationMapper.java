package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.UserNotification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserNotificationMapper extends BaseMapper<UserNotification> {

    @Select("SELECT COUNT(*) FROM t_user_notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM t_user_notification WHERE user_id = #{userId} AND is_read = 0 AND is_must_read = 1")
    int countMustReadUnread(@Param("userId") Integer userId);

    @Update("UPDATE t_user_notification SET is_read = 1 WHERE user_id = #{userId} AND id = #{id}")
    int markAsRead(@Param("userId") Integer userId, @Param("id") Integer id);

    @Update("UPDATE t_user_notification SET is_read = 1 WHERE user_id = #{userId}")
    int markAllAsRead(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM t_user_notification WHERE user_id = #{userId} AND is_read = 0 AND type = #{type}")
    int countUnreadByType(@Param("userId") Integer userId, @Param("type") String type);

    @Update("UPDATE t_user_notification SET is_read = 1 WHERE user_id = #{userId} AND type = #{type}")
    int markAllAsReadByType(@Param("userId") Integer userId, @Param("type") String type);
}
