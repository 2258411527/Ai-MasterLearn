package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.home.PersonalizedHomeVO;
import cn.org.alan.exam.service.IPersonalizedHomeService;
import cn.org.alan.exam.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "个性化首页")
@RestController
@RequestMapping("/api/home")
public class PersonalizedHomeController {

    @Autowired
    private IPersonalizedHomeService homeService;

    @ApiOperation("获取个性化首页数据")
    @GetMapping("/personalized")
    public Result<PersonalizedHomeVO> getHomeData() {
        Integer userId = SecurityUtil.getUserId();
        return homeService.getPersonalizedHomeData(userId);
    }

    @ApiOperation("刷新首页缓存")
    @PostMapping("/refresh")
    public Result<String> refreshCache() {
        Integer userId = SecurityUtil.getUserId();
        return homeService.refreshCache(userId);
    }

    @ApiOperation("清除首页缓存")
    @DeleteMapping("/cache")
    public Result<String> clearCache() {
        Integer userId = SecurityUtil.getUserId();
        return homeService.clearCache(userId);
    }
}
