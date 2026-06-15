package cn.org.alan.exam.aspect;

import cn.org.alan.exam.annotation.RequireToken;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.service.IUserTokenService;
import cn.org.alan.exam.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(1)
@Slf4j
public class TokenCheckAspect {

    @Autowired
    private IUserTokenService userTokenService;

    @Around("@annotation(cn.org.alan.exam.annotation.RequireToken)")
    public Object checkAndConsumeToken(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireToken requireToken = method.getAnnotation(RequireToken.class);

        int cost = requireToken.value();
        String serviceType = requireToken.serviceType();

        Integer userId = SecurityUtil.getUserId();

        Result<Boolean> checkResult = userTokenService.checkTokenSufficient(userId, cost);
        if (checkResult.getData() == null || !checkResult.getData()) {
            log.warn("用户{}Token余额不足, 需要: {}, 服务: {}", userId, cost, serviceType);
            return Result.failed("Token余额不足，请先充值后再使用AI服务");
        }

        String description = "AI服务消费: " + serviceType;
        Result<String> consumeResult = userTokenService.consume(userId, cost, serviceType, description);
        if (consumeResult.getCode() != 1) {
            log.warn("用户{}Token扣减失败: {}", userId, consumeResult.getMsg());
            return Result.failed(consumeResult.getMsg());
        }

        log.info("用户{}Token扣减成功, 消费: {}, 服务: {}", userId, cost, serviceType);
        return joinPoint.proceed();
    }
}
