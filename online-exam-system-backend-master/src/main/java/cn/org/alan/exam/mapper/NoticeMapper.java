package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Notice;
import cn.org.alan.exam.model.form.notice.NoticeForm;
import cn.org.alan.exam.model.vo.notice.NoticeVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告表 Mapper 接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface NoticeMapper extends BaseMapper<Notice> {

    /**
     * 获取公告是否公开
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    Integer getIsPublic(Integer noticeId);

    /**
     * 更新公告
     *
     * @param noticeId   公告ID
     * @param noticeForm 公告表单
     * @return 更新记录数
     */
    Integer updateNotice(@Param("noticeId") Integer noticeId, @Param("noticeForm") NoticeForm noticeForm);

    /**
     * 获取最新公告
     *
     * @param page         分页对象
     * @param teachIdList  老师ID列表
     * @param noticeIdList 公告ID列表
     * @param adminIdList  管理员ID列表
     * @return 分页结果
     */
    Page<NoticeVO> getNewNotice(Page<NoticeVO> page, @Param("teachIdList") List<Integer> teachIdList, @Param("noticeIdList") List<Integer> noticeIdList, @Param("adminIdList") List<Integer> adminIdList, @Param("userId") Integer userId);

    /**
     * 教师管理员获取公告
     *
     * @param userId 用户ID
     * @param title  公告标题
     * @return 结果集
     */
    List<NoticeVO> getNotice(@Param("userId") Integer userId, @Param("title") String title);

}
