package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.mapper.UserNotificationMapper;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.entity.UserNotification;
import cn.org.alan.exam.service.IUserNotificationService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserNotificationServiceImpl extends ServiceImpl<UserNotificationMapper, UserNotification> implements IUserNotificationService {

    @Resource
    private UserMapper userMapper;

    @Override
    public Result<IPage<UserNotification>> getMyNotifications(Integer pageNum, Integer pageSize, String type) {
        Integer userId = SecurityUtil.getUserId();
        Page<UserNotification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserNotification> wrapper = new LambdaQueryWrapper<UserNotification>()
                .eq(UserNotification::getUserId, userId)
                .eq(type != null && !type.isEmpty(), UserNotification::getType, type)
                .orderByDesc(UserNotification::getIsMustRead)
                .orderByDesc(UserNotification::getCreateTime);
        page = (Page<UserNotification>) this.page(page, wrapper);
        return Result.success("查询成功", page);
    }

    @Override
    public Result<Integer> getUnreadCount() {
        Integer userId = SecurityUtil.getUserId();
        int count = baseMapper.countUnread(userId);
        return Result.success("查询成功", count);
    }

    @Override
    public Result<Integer> getMustReadUnreadCount() {
        Integer userId = SecurityUtil.getUserId();
        int count = baseMapper.countMustReadUnread(userId);
        return Result.success("查询成功", count);
    }

    @Override
    public Result<String> markAsRead(Integer id) {
        Integer userId = SecurityUtil.getUserId();
        int rows = baseMapper.markAsRead(userId, id);
        if (rows > 0) {
            return Result.success("标记已读成功");
        }
        return Result.failed("标记已读失败");
    }

    @Override
    public Result<String> markAllAsRead() {
        Integer userId = SecurityUtil.getUserId();
        baseMapper.markAllAsRead(userId);
        return Result.success("全部标记已读成功");
    }

    @Override
    public Result<String> createNotification(Integer userId, String type, Integer relatedId, String title, String content, Integer isMustRead) {
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsMustRead(isMustRead != null ? isMustRead : 0);
        notification.setIsRead(0);
        this.save(notification);
        return Result.success("创建通知成功");
    }

    @Override
    public Result<String> createNotificationWithSender(Integer userId, String type, Integer relatedId, Integer senderId, String senderName, String senderAvatar, String title, String content, Integer isMustRead) {
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setSenderId(senderId);
        notification.setSenderName(senderName);
        notification.setSenderAvatar(senderAvatar);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsMustRead(isMustRead != null ? isMustRead : 0);
        notification.setIsRead(0);
        this.save(notification);
        return Result.success("创建通知成功");
    }

    @Override
    public Result<String> broadcastNotification(String type, String title, String content, Integer isMustRead) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getIsDeleted, 0)
                .eq(User::getRoleId, 1);
        List<User> students = userMapper.selectList(wrapper);
        for (User student : students) {
            UserNotification notification = new UserNotification();
            notification.setUserId(student.getId());
            notification.setType(type);
            notification.setRelatedId(null);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setIsMustRead(isMustRead != null ? isMustRead : 0);
            notification.setIsRead(0);
            this.save(notification);
        }
        return Result.success("广播通知成功，已通知" + students.size() + "名学生");
    }

    @Override
    public Result<Integer> getUnreadCountByType(String type) {
        Integer userId = SecurityUtil.getUserId();
        int count = baseMapper.countUnreadByType(userId, type);
        return Result.success("查询成功", count);
    }

    @Override
    public Result<String> markAllAsReadByType(String type) {
        Integer userId = SecurityUtil.getUserId();
        baseMapper.markAllAsReadByType(userId, type);
        return Result.success("标记已读成功");
    }
}
