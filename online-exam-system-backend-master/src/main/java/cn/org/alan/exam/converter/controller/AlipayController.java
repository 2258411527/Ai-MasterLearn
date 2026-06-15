package cn.org.alan.exam.converter.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.token.TokenOrderVO;
import cn.org.alan.exam.service.IAlipayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
@Api(tags = "支付宝支付相关接口")
@Slf4j
public class AlipayController {

    @Autowired
    private IAlipayService alipayService;

    @PostMapping("/pay")
    @ApiOperation("创建支付宝支付订单")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public Result<String> createPayment(@RequestParam Integer packageId) {
        return alipayService.createPayment(packageId);
    }

    @PostMapping("/notify")
    @ApiOperation("支付宝异步通知回调")
    public String handleNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append(i == values.length - 1 ? values[i] : values[i] + ",");
            }
            params.put(name, valueStr.toString());
        }

        log.info("收到支付宝异步通知: {}", params);
        Result<String> result = alipayService.handleNotify(params);
        if ("success".equals(result.getData())) {
            return "success";
        }
        return "failure";
    }

    @GetMapping("/query")
    @ApiOperation("查询订单支付状态")
    @PreAuthorize("hasAnyAuthority('role_student', 'role_teacher', 'role_admin')")
    public Result<TokenOrderVO> queryOrderStatus(@RequestParam String orderNo) {
        return alipayService.queryOrderStatus(orderNo);
    }
}
