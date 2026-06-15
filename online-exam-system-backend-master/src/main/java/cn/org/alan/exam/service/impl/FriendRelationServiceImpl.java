package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.FriendRelationMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.FriendRelation;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.service.IFriendRelationService;
import cn.org.alan.exam.service.IUserNotificationService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 好友关系服务实现
 */
@Service
@Slf4j
public class FriendRelationServiceImpl extends ServiceImpl<FriendRelationMapper, FriendRelation> implements IFriendRelationService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private IUserNotificationService notificationService;

    @Override
    public Result<List<FriendRelation>> getFriendList(Integer userId) {
        try {
            List<FriendRelation> friendList = baseMapper.selectFriendListByUserId(userId);
            
            // 处理显示名称：优先使用备注，其次使用好友的真实姓名
            friendList.forEach(friend -> {
                if (friend.getRemark() != null && !friend.getRemark().trim().isEmpty()) {
                    friend.setDisplayName(friend.getRemark());
                } else if (friend.getFriendName() != null && !friend.getFriendName().trim().isEmpty()) {
                    friend.setDisplayName(friend.getFriendName());
                } else {
                    friend.setDisplayName("好友");
                }
            });
            
            return Result.success("获取好友列表成功", friendList);
        } catch (Exception e) {
            log.error("获取好友列表失败", e);
            return Result.failed("获取好友列表失败");
        }
    }

    @Override
    public Result<Object> searchUser(String keyword) {
        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.and(w -> w.like(User::getUserName, keyword)
                    .or().like(User::getRealName, keyword))
                    .eq(User::getIsDeleted, 0);
            
            List<User> users = userMapper.selectList(wrapper);
            
            if (users.isEmpty()) {
                return Result.failed("未找到相关用户");
            }
            
            // 只返回第一个匹配的用户（简化实现）
            User user = users.get(0);
            
            // 检查是否是当前用户自己
            Integer currentUserId = SecurityUtil.getUserId();
            if (user.getId().equals(currentUserId)) {
                return Result.failed("不能添加自己为好友");
            }
            
            return Result.success("搜索成功", user);
        } catch (Exception e) {
            return Result.failed("搜索用户失败");
        }
    }

    @Override
    @Transactional
    public Result<String> sendFriendRequest(Integer userId, Integer friendId) {
        try {
            log.info("发送好友请求: userId={}, friendId={}", userId, friendId);
            
            // 检查用户是否存在
            User friend = userMapper.selectById(friendId);
            if (friend == null) {
                log.warn("用户不存在: friendId={}", friendId);
                return Result.failed("用户不存在");
            }
            
            // 检查是否是当前用户自己
            if (userId.equals(friendId)) {
                log.warn("不能添加自己为好友: userId={}", userId);
                return Result.failed("不能添加自己为好友");
            }
            
            // 检查是否已经是好友（双向检查）
            Integer exists = baseMapper.checkFriendRelationExists(userId, friendId);
            if (exists > 0) {
                log.info("已经是好友关系: userId={}, friendId={}", userId, friendId);
                return Result.failed("已经是好友关系");
            }
            
            // 检查是否已经发送过请求（双向检查）
            LambdaQueryWrapper<FriendRelation> wrapper = new LambdaQueryWrapper<>();
            wrapper.and(w -> w.and(w1 -> w1.eq(FriendRelation::getUserId, userId)
                                        .eq(FriendRelation::getFriendId, friendId))
                            .or(w2 -> w2.eq(FriendRelation::getUserId, friendId)
                                        .eq(FriendRelation::getFriendId, userId)))
                   .eq(FriendRelation::getStatus, 0)
                   .eq(FriendRelation::getIsDeleted, 0);
            
            FriendRelation existingRequest = baseMapper.selectOne(wrapper);
            if (existingRequest != null) {
                log.info("已存在待处理请求: existingRequest={}", existingRequest);
                // 如果是对方发送的请求，直接同意
                if (existingRequest.getUserId().equals(friendId)) {
                    log.info("自动同意对方的好友请求: userId={}, friendId={}", userId, friendId);
                    existingRequest.setStatus(1); // 设置为已同意
                    existingRequest.setUpdateTime(LocalDateTime.now());
                    updateById(existingRequest);
                    
                    // 创建反向的好友关系
                    FriendRelation reverseRelation = new FriendRelation();
                    reverseRelation.setUserId(userId);
                    reverseRelation.setFriendId(friendId);
                    reverseRelation.setStatus(1); // 已同意
                    reverseRelation.setCreateTime(LocalDateTime.now());
                    reverseRelation.setUpdateTime(LocalDateTime.now());
                    save(reverseRelation);
                    
                    log.info("好友关系建立成功: userId={}, friendId={}", userId, friendId);
                    return Result.success("好友请求已自动同意");
                } else {
                    log.warn("已经发送过好友请求，等待对方处理: userId={}, friendId={}", userId, friendId);
                    return Result.failed("已经发送过好友请求，请等待对方处理");
                }
            }
            
            // 创建好友请求
            FriendRelation friendRelation = new FriendRelation();
            friendRelation.setUserId(userId);
            friendRelation.setFriendId(friendId);
            friendRelation.setStatus(0); // 待审核
            friendRelation.setCreateTime(LocalDateTime.now());
            friendRelation.setUpdateTime(LocalDateTime.now());
            
            boolean success = save(friendRelation);
            if (success) {
                log.info("好友请求发送成功: userId={}, friendId={}, requestId={}", userId, friendId, friendRelation.getId());
                try {
                    User sender = userMapper.selectById(userId);
                    String senderName = sender != null ? sender.getRealName() : "用户";
                    String senderAvatar = sender != null ? sender.getAvatar() : null;
                    notificationService.createNotificationWithSender(
                            friendId,
                            "friend_request",
                            friendRelation.getId(),
                            userId,
                            senderName,
                            senderAvatar,
                            "好友请求",
                            senderName + " 请求添加你为好友",
                            0
                    );
                } catch (Exception notifyEx) {
                    log.error("创建好友请求通知失败", notifyEx);
                }
                return Result.success("好友请求发送成功");
            } else {
                log.error("保存好友请求失败: userId={}, friendId={}", userId, friendId);
                return Result.failed("发送好友请求失败");
            }
        } catch (Exception e) {
            log.error("发送好友请求异常: userId={}, friendId={}", userId, friendId, e);
            return Result.failed("发送好友请求失败");
        }
    }

    @Override
    @Transactional
    public Result<String> handleFriendRequest(Integer requestId, Integer status) {
        try {
            FriendRelation request = baseMapper.selectById(requestId);
            if (request == null) {
                return Result.failed("好友请求不存在");
            }
            
            // 检查权限（只能处理发送给自己的请求）
            Integer currentUserId = SecurityUtil.getUserId();
            if (!request.getFriendId().equals(currentUserId)) {
                return Result.failed("无权处理此请求");
            }
            
            request.setStatus(status);
            request.setUpdateTime(LocalDateTime.now());
            
            boolean success = updateById(request);
            if (success) {
                // 如果同意，创建反向的好友关系
                if (status == 1) {
                    FriendRelation reverseRelation = new FriendRelation();
                    reverseRelation.setUserId(request.getFriendId());
                    reverseRelation.setFriendId(request.getUserId());
                    reverseRelation.setStatus(1); // 已同意
                    reverseRelation.setCreateTime(LocalDateTime.now());
                    reverseRelation.setUpdateTime(LocalDateTime.now());
                    save(reverseRelation);
                }
                
                String message = status == 1 ? "已同意好友请求" : "已拒绝好友请求";
                return Result.success(message);
            } else {
                return Result.failed("处理好友请求失败");
            }
        } catch (Exception e) {
            return Result.failed("处理好友请求失败");
        }
    }

    @Override
    @Transactional
    public Result<String> deleteFriend(Integer userId, Integer friendId) {
        try {
            // 删除双向的好友关系
            LambdaQueryWrapper<FriendRelation> wrapper = new LambdaQueryWrapper<>();
            wrapper.and(w -> w.and(w1 -> w1.eq(FriendRelation::getUserId, userId)
                                        .eq(FriendRelation::getFriendId, friendId))
                            .or(w2 -> w2.eq(FriendRelation::getUserId, friendId)
                                        .eq(FriendRelation::getFriendId, userId)))
                   .eq(FriendRelation::getIsDeleted, 0);
            
            List<FriendRelation> relations = baseMapper.selectList(wrapper);
            
            for (FriendRelation relation : relations) {
                relation.setIsDeleted(1);
                relation.setUpdateTime(LocalDateTime.now());
                baseMapper.updateById(relation);
            }
            
            return Result.success("删除好友成功");
        } catch (Exception e) {
            return Result.failed("删除好友失败");
        }
    }

    @Override
    public Result<List<FriendRelation>> getPendingRequests(Integer userId) {
        try {
            List<FriendRelation> pendingRequests = baseMapper.selectPendingFriendRequests(userId);
            return Result.success("获取待处理请求成功", pendingRequests);
        } catch (Exception e) {
            log.error("获取待处理请求失败", e);
            return Result.failed("获取待处理请求失败");
        }
    }

    @Override
    @Transactional
    public Result<String> updateFriendRemark(Integer userId, Integer friendId, String remark) {
        try {
            // 查找当前用户对该好友的关系记录
            LambdaQueryWrapper<FriendRelation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FriendRelation::getUserId, userId)
                   .eq(FriendRelation::getFriendId, friendId)
                   .eq(FriendRelation::getStatus, 1)
                   .eq(FriendRelation::getIsDeleted, 0);
            
            FriendRelation relation = baseMapper.selectOne(wrapper);
            if (relation == null) {
                return Result.failed("好友关系不存在");
            }
            
            // 更新备注
            relation.setRemark(remark);
            relation.setUpdateTime(LocalDateTime.now());
            
            boolean success = updateById(relation);
            if (success) {
                log.info("修改好友备注成功: userId={}, friendId={}, remark={}", userId, friendId, remark);
                return Result.success("修改备注成功");
            } else {
                return Result.failed("修改备注失败");
            }
        } catch (Exception e) {
            log.error("修改好友备注失败: userId={}, friendId={}", userId, friendId, e);
            return Result.failed("修改备注失败");
        }
    }
}