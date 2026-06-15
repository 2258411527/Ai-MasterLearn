package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.NoticeRead;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface NoticeReadMapper extends BaseMapper<NoticeRead> {

    @Insert("INSERT IGNORE INTO t_notice_read (notice_id, user_id, read_time) VALUES (#{noticeId}, #{userId}, NOW())")
    int markAsRead(@Param("noticeId") Integer noticeId, @Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM t_notice n " +
            "LEFT JOIN t_notice_read r ON n.id = r.notice_id AND r.user_id = #{userId} " +
            "WHERE n.is_deleted = 0 AND r.id IS NULL " +
            "AND (n.is_public = 1 OR n.id IN (SELECT notice_id FROM t_notice_grade WHERE grade_id = #{gradeId}))")
    int countUnread(@Param("userId") Integer userId, @Param("gradeId") Integer gradeId);
}
