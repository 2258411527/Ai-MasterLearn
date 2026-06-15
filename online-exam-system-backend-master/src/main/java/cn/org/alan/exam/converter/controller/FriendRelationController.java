package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.FriendRelation;
import cn.org.alan.exam.service.IFriendRelationService;
import cn.org.alan.exam.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 好友关系控制器
 */
@RestController
@RequestMapping("/friend")
@Api(tags = "好友关系接口")
public class FriendRelationController {

    @Resource
    private IFriendRelationService friendRelationService;

    /**
     * 获取好友列表
     */
    @GetMapping("/list")
    @ApiOperation("获取好友列表")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<List<FriendRelation>> getFriendList() {
        Integer userId = SecurityUtil.getUserId();
        return friendRelationService.getFriendList(userId);
    }

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    @ApiOperation("搜索用户")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<Object> searchUser(@RequestParam String keyword) {
        return friendRelationService.searchUser(keyword);
    }

    /**
     * 发送好友请求
     */
    @PostMapping("/request/{friendId}")
    @ApiOperation("发送好友请求")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> sendFriendRequest(@PathVariable("friendId") Integer friendId) {
        Integer userId = SecurityUtil.getUserId();
        return friendRelationService.sendFriendRequest(userId, friendId);
    }

    /**
     * 处理好友请求
     */
    @PostMapping("/request/{requestId}/handle")
    @ApiOperation("处理好友请求")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> handleFriendRequest(@PathVariable("requestId") Integer requestId, 
                                             @RequestParam Integer status) {
        return friendRelationService.handleFriendRequest(requestId, status);
    }

    /**
     * 删除好友
     */
    @DeleteMapping("/{friendId}")
    @ApiOperation("删除好友")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> deleteFriend(@PathVariable("friendId") Integer friendId) {
        Integer userId = SecurityUtil.getUserId();
        return friendRelationService.deleteFriend(userId, friendId);
    }

    /**
     * 获取待处理的好友请求
     */
    @GetMapping("/requests/pending")
    @ApiOperation("获取待处理的好友请求")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<List<FriendRelation>> getPendingRequests() {
        Integer userId = SecurityUtil.getUserId();
        return friendRelationService.getPendingRequests(userId);
    }

    /**
     * 修改好友备注
     */
    @PutMapping("/{friendId}/remark")
    @ApiOperation("修改好友备注")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> updateFriendRemark(@PathVariable("friendId") Integer friendId,
                                            @RequestParam String remark) {
        Integer userId = SecurityUtil.getUserId();
        return friendRelationService.updateFriendRemark(userId, friendId, remark);
    }
}