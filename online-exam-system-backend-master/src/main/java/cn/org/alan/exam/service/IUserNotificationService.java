package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.UserNotification;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IUserNotificationService extends IService<UserNotification> {

    Result<IPage<UserNotification>> getMyNotifications(Integer pageNum, Integer pageSize, String type);

    Result<Integer> getUnreadCount();

    Result<Integer> getMustReadUnreadCount();

    Result<String> markAsRead(Integer id);

    Result<String> markAllAsRead();

    Result<String> createNotification(Integer userId, String type, Integer relatedId, String title, String content, Integer isMustRead);

    Result<String> createNotificationWithSender(Integer userId, String type, Integer relatedId, Integer senderId, String senderName, String senderAvatar, String title, String content, Integer isMustRead);

    Result<String> broadcastNotification(String type, String title, String content, Integer isMustRead);

    Result<Integer> getUnreadCountByType(String type);

    Result<String> markAllAsReadByType(String type);
}
