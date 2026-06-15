package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.config.AlipayPayConfig;
import cn.org.alan.exam.mapper.TokenOrderMapper;
import cn.org.alan.exam.mapper.TokenPackageMapper;
import cn.org.alan.exam.mapper.TokenTransactionMapper;
import cn.org.alan.exam.mapper.UserTokenMapper;
import cn.org.alan.exam.model.entity.AlipayConfigEntity;
import cn.org.alan.exam.model.entity.TokenOrder;
import cn.org.alan.exam.model.entity.TokenPackage;
import cn.org.alan.exam.model.entity.TokenTransaction;
import cn.org.alan.exam.model.entity.UserToken;
import cn.org.alan.exam.model.vo.token.TokenOrderVO;
import cn.org.alan.exam.service.IAlipayConfigService;
import cn.org.alan.exam.service.IAlipayService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AlipayServiceImpl implements IAlipayService {

    @Autowired(required = false)
    private AlipayClient alipayClient;

    @Autowired
    private AlipayPayConfig alipayPayConfig;

    @Autowired
    private TokenOrderMapper tokenOrderMapper;

    @Autowired
    private TokenPackageMapper tokenPackageMapper;

    @Autowired
    private UserTokenMapper userTokenMapper;

    @Autowired
    private TokenTransactionMapper tokenTransactionMapper;

    @Autowired
    private IAlipayConfigService alipayConfigService;

    @Override
    public Result<String> createPayment(Integer packageId) {
        Integer userId = SecurityUtil.getUserId();

        TokenPackage tokenPackage = tokenPackageMapper.selectById(packageId);
        if (tokenPackage == null || !tokenPackage.getIsActive()) {
            return Result.failed("套餐不存在或已下架");
        }

        String orderNo = generateOrderNo();
        TokenOrder order = new TokenOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPackageId(packageId);
        order.setPackageName(tokenPackage.getName());
        order.setTokens(tokenPackage.getTokens());
        order.setAmount(tokenPackage.getPrice());
        order.setStatus(TokenOrder.STATUS_PENDING);
        tokenOrderMapper.insert(order);

        AlipayConfigEntity config = null;
        try {
            Result<AlipayConfigEntity> configResult = alipayConfigService.getActiveConfig();
            if (configResult.getCode() == 1) {
                config = configResult.getData();
            }
        } catch (Exception ignored) {}

        if (config != null && Boolean.TRUE.equals(config.getTestMode())) {
            log.info("测试模式：模拟支付成功，订单: {}", orderNo);
            processPaymentSuccess(order, "TEST_" + System.currentTimeMillis());
            return Result.success("测试模式：模拟支付成功", "TEST_MODE_SUCCESS:" + orderNo);
        }

        if (alipayClient == null) {
            tokenOrderMapper.deleteById(order.getId());
            return Result.failed("支付宝功能未配置，请联系管理员配置支付宝参数或开启测试模式");
        }

        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayPayConfig.getEffectiveNotifyUrl());
            request.setReturnUrl(alipayPayConfig.getEffectiveReturnUrl());

            request.setBizContent("{" +
                    "\"out_trade_no\":\"" + orderNo + "\"," +
                    "\"total_amount\":\"" + tokenPackage.getPrice().toPlainString() + "\"," +
                    "\"subject\":\"" + tokenPackage.getName() + " - Token充值\"," +
                    "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"" +
                    "}");

            String form = alipayClient.pageExecute(request, "POST").getBody();
            log.info("用户{}创建支付订单: {}, 金额: {}", userId, orderNo, tokenPackage.getPrice());
            return Result.success("创建支付订单成功", form);
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付订单失败: {}", e.getErrMsg(), e);
            return Result.failed("创建支付订单失败: " + e.getErrMsg());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> handleNotify(Map<String, String> params) {
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayPayConfig.getAlipayPublicKey(),
                    alipayPayConfig.getCharset(),
                    alipayPayConfig.getEffectiveSignType()
            );

            if (!signVerified) {
                log.warn("支付宝异步通知验签失败: {}", params);
                return Result.failed("验签失败");
            }

            String appId = params.get("app_id");
            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            String tradeNo = params.get("trade_no");
            String totalAmount = params.get("total_amount");

            log.info("收到支付宝异步通知: orderNo={}, tradeStatus={}, tradeNo={}", outTradeNo, tradeStatus, tradeNo);

            TokenOrder order = tokenOrderMapper.selectOne(
                    new LambdaQueryWrapper<TokenOrder>().eq(TokenOrder::getOrderNo, outTradeNo));

            if (order == null) {
                log.warn("订单不存在: {}", outTradeNo);
                return Result.failed("订单不存在");
            }

            if (order.getStatus() == TokenOrder.STATUS_PAID) {
                log.info("订单已处理，忽略重复通知: {}", outTradeNo);
                return Result.success("success");
            }

            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                log.info("交易状态非成功，忽略: {}", tradeStatus);
                return Result.success("ignored");
            }

            if (order.getAmount().compareTo(new BigDecimal(totalAmount)) != 0) {
                log.warn("订单金额不匹配: order={}, notify={}", order.getAmount(), totalAmount);
                return Result.failed("金额不匹配");
            }

            order.setStatus(TokenOrder.STATUS_PAID);
            order.setTradeNo(tradeNo);
            order.setPayTime(LocalDateTime.now());
            tokenOrderMapper.updateById(order);

            int rows = userTokenMapper.addBalance(order.getUserId(), order.getTokens());
            if (rows == 0) {
                UserToken userToken = new UserToken();
                userToken.setUserId(order.getUserId());
                userToken.setBalance(order.getTokens());
                userToken.setTotalPurchased(order.getTokens());
                userToken.setTotalConsumed(0);
                userTokenMapper.insert(userToken);
            } else {
                UserToken userToken = userTokenMapper.selectOne(
                        new LambdaQueryWrapper<UserToken>().eq(UserToken::getUserId, order.getUserId()));

                TokenTransaction transaction = new TokenTransaction();
                transaction.setUserId(order.getUserId());
                transaction.setType(TokenTransaction.TYPE_PURCHASE);
                transaction.setAmount(order.getTokens());
                transaction.setBalanceAfter(userToken.getBalance());
                transaction.setDescription("购买套餐: " + order.getPackageName() + " (支付宝支付)");
                tokenTransactionMapper.insert(transaction);
            }

            log.info("订单支付成功，用户{}充值{}Tokens: orderNo={}", order.getUserId(), order.getTokens(), outTradeNo);
            return Result.success("success");
        } catch (AlipayApiException e) {
            log.error("处理支付宝异步通知异常: {}", e.getErrMsg(), e);
            return Result.failed("处理失败");
        }
    }

    @Override
    public Result<TokenOrderVO> queryOrderStatus(String orderNo) {
        TokenOrder order = tokenOrderMapper.selectOne(
                new LambdaQueryWrapper<TokenOrder>().eq(TokenOrder::getOrderNo, orderNo));

        if (order == null) {
            return Result.failed("订单不存在");
        }

        TokenOrderVO vo = toOrderVO(order);

        if (order.getStatus() == TokenOrder.STATUS_PENDING) {
            try {
                AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();
                queryRequest.setBizContent("{" +
                        "\"out_trade_no\":\"" + orderNo + "\"" +
                        "}");
                AlipayTradeQueryResponse queryResponse = alipayClient.execute(queryRequest);
                if (queryResponse.isSuccess()) {
                    String tradeStatus = queryResponse.getTradeStatus();
                    if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                        order.setStatus(TokenOrder.STATUS_PAID);
                        order.setTradeNo(queryResponse.getTradeNo());
                        order.setPayTime(LocalDateTime.now());
                        tokenOrderMapper.updateById(order);
                        vo.setStatus(TokenOrder.STATUS_PAID);
                        vo.setStatusName("已支付");
                        vo.setTradeNo(queryResponse.getTradeNo());
                    }
                }
            } catch (AlipayApiException e) {
                log.warn("查询支付宝订单状态失败: {}", e.getErrMsg());
            }
        }

        return Result.success("查询成功", vo);
    }

    private String generateOrderNo() {
        return "TK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    private TokenOrderVO toOrderVO(TokenOrder order) {
        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(TokenOrder.STATUS_PENDING, "待支付");
        statusMap.put(TokenOrder.STATUS_PAID, "已支付");
        statusMap.put(TokenOrder.STATUS_CLOSED, "已关闭");
        statusMap.put(TokenOrder.STATUS_REFUND, "已退款");

        TokenOrderVO vo = new TokenOrderVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setPackageName(order.getPackageName());
        vo.setTokens(order.getTokens());
        vo.setAmount(order.getAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusName(statusMap.getOrDefault(order.getStatus(), "未知"));
        vo.setTradeNo(order.getTradeNo());
        vo.setPayTime(order.getPayTime());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void processPaymentSuccess(TokenOrder order, String tradeNo) {
        order.setStatus(TokenOrder.STATUS_PAID);
        order.setTradeNo(tradeNo);
        order.setPayTime(LocalDateTime.now());
        tokenOrderMapper.updateById(order);

        int rows = userTokenMapper.addBalance(order.getUserId(), order.getTokens());
        if (rows == 0) {
            UserToken userToken = new UserToken();
            userToken.setUserId(order.getUserId());
            userToken.setBalance(order.getTokens());
            userToken.setTotalPurchased(order.getTokens());
            userToken.setTotalConsumed(0);
            userTokenMapper.insert(userToken);
        } else {
            UserToken userToken = userTokenMapper.selectOne(
                    new LambdaQueryWrapper<UserToken>().eq(UserToken::getUserId, order.getUserId()));

            TokenTransaction transaction = new TokenTransaction();
            transaction.setUserId(order.getUserId());
            transaction.setType(TokenTransaction.TYPE_PURCHASE);
            transaction.setAmount(order.getTokens());
            transaction.setBalanceAfter(userToken.getBalance());
            transaction.setDescription("购买套餐: " + order.getPackageName() + " (测试模式)");
            tokenTransactionMapper.insert(transaction);
        }

        log.info("订单支付成功，用户{}充值{}Tokens: orderNo={}", order.getUserId(), order.getTokens(), order.getOrderNo());
    }
}