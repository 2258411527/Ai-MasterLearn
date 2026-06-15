package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.token.TokenAdjustForm;
import cn.org.alan.exam.model.form.token.TokenPackageForm;
import cn.org.alan.exam.model.form.token.TokenPurchaseForm;
import cn.org.alan.exam.model.vo.token.AdminUserTokenVO;
import cn.org.alan.exam.model.vo.token.TokenPackageVO;
import cn.org.alan.exam.model.vo.token.TokenTransactionVO;
import cn.org.alan.exam.model.vo.token.UserTokenVO;
import cn.org.alan.exam.service.IUserTokenService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/token")
@Api(tags = "Token管理相关接口")
public class UserTokenController {

    @Autowired
    private IUserTokenService userTokenService;

    @GetMapping("/balance")
    @ApiOperation("查询当前用户Token余额")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public Result<UserTokenVO> getBalance() {
        return userTokenService.getBalance();
    }

    @PostMapping("/purchase")
    @ApiOperation("购买Token套餐")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public Result<String> purchase(@RequestBody @Validated TokenPurchaseForm form) {
        return userTokenService.purchase(form);
    }

    @GetMapping("/packages")
    @ApiOperation("获取可购买的Token套餐列表")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public Result<List<TokenPackageVO>> getAvailablePackages() {
        return userTokenService.getAvailablePackages();
    }

    @GetMapping("/transactions")
    @ApiOperation("查询Token交易记录")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public Result<IPage<TokenTransactionVO>> getTransactionHistory(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return userTokenService.getTransactionHistory(pageNum, pageSize);
    }

    @GetMapping("/admin/packages")
    @ApiOperation("管理员分页查询Token套餐")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<IPage<TokenPackageVO>> adminGetPackages(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return userTokenService.adminGetPackages(pageNum, pageSize);
    }

    @PostMapping("/admin/packages")
    @ApiOperation("管理员添加Token套餐")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> adminAddPackage(@RequestBody @Validated TokenPackageForm form) {
        return userTokenService.adminAddPackage(form);
    }

    @PutMapping("/admin/packages/{id}")
    @ApiOperation("管理员修改Token套餐")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> adminUpdatePackage(@PathVariable Integer id,
                                              @RequestBody @Validated TokenPackageForm form) {
        return userTokenService.adminUpdatePackage(id, form);
    }

    @DeleteMapping("/admin/packages/{id}")
    @ApiOperation("管理员删除Token套餐")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> adminDeletePackage(@PathVariable Integer id) {
        return userTokenService.adminDeletePackage(id);
    }

    @GetMapping("/admin/users")
    @ApiOperation("管理员查询用户Token余额列表")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<IPage<AdminUserTokenVO>> adminGetUserTokens(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "realName", required = false) String realName) {
        return userTokenService.adminGetUserTokens(pageNum, pageSize, realName);
    }

    @PostMapping("/admin/adjust")
    @ApiOperation("管理员调整用户Token")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> adminAdjust(@RequestBody @Validated TokenAdjustForm form) {
        return userTokenService.adminAdjust(form);
    }
}
