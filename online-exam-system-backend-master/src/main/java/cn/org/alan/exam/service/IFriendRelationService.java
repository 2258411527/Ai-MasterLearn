package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.FriendRelation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 好友关系服务接口
 */
public interface IFriendRelationService extends IService<FriendRelation> {

    /**
     * 获取好友列表
     */
    Result<List<FriendRelation>> getFriendList(Integer userId);

    /**
     * 搜索用户
     */
    Result<Object> searchUser(String keyword);

    /**
     * 发送好友请求
     */
    Result<String> sendFriendRequest(Integer userId, Integer friendId);

    /**
     * 处理好友请求
     */
    Result<String> handleFriendRequest(Integer requestId, Integer status);

    /**
     * 删除好友
     */
    Result<String> deleteFriend(Integer userId, Integer friendId);

    /**
     * 获取待处理的好友请求
     */
    Result<List<FriendRelation>> getPendingRequests(Integer userId);

    /**
     * 修改好友备注
     */
    Result<String> updateFriendRemark(Integer userId, Integer friendId, String remark);
}