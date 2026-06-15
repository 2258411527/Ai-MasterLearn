package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.alipay.AlipayConfigForm;
import cn.org.alan.exam.model.vo.alipay.AlipayConfigVO;
import cn.org.alan.exam.service.IAlipayConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "支付宝配置管理相关接口")
@RestController
@RequestMapping("/alipay-config")
public class AlipayConfigController {

    @Autowired
    private IAlipayConfigService alipayConfigService;

    @ApiOperation("分页查询支付宝配置")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<IPage<AlipayConfigVO>> getConfigPage(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "configName", required = false) String configName) {
        return alipayConfigService.getConfigPage(pageNum, pageSize, configName);
    }

    @ApiOperation("添加支付宝配置")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> addConfig(@RequestBody @Validated AlipayConfigForm form) {
        return alipayConfigService.addConfig(form);
    }

    @ApiOperation("修改支付宝配置")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> updateConfig(@PathVariable Integer id,
                                        @RequestBody @Validated AlipayConfigForm form) {
        return alipayConfigService.updateConfig(id, form);
    }

    @ApiOperation("删除支付宝配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> deleteConfig(@PathVariable Integer id) {
        return alipayConfigService.deleteConfig(id);
    }

    @ApiOperation("激活支付宝配置")
    @PutMapping("/activate/{id}")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> activateConfig(@PathVariable Integer id) {
        return alipayConfigService.activateConfig(id);
    }
}
