package cn.org.alan.exam.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.UserConverter;
import cn.org.alan.exam.mapper.RoleMapper;
import cn.org.alan.exam.mapper.UserDailyLoginDurationMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.mapper.UserTokenMapper;
import cn.org.alan.exam.model.entity.Log;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.entity.UserDailyLoginDuration;
import cn.org.alan.exam.model.entity.UserToken;
import cn.org.alan.exam.model.form.auth.LoginForm;
import cn.org.alan.exam.model.form.user.UserForm;
import cn.org.alan.exam.service.IAuthService;
import cn.org.alan.exam.service.ILogService;
import cn.org.alan.exam.utils.*;
import cn.org.alan.exam.utils.security.SysUserDetails;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 权限管理服务实现类
 *
 * @Author Alan
 * @Version
 * @Date 2024/3/28 1:33 PM
 */
@Service
public class AuthServiceImpl implements IAuthService {
    private static final String HEARTBEAT_KEY_PREFIX = "user:heartbeat:";
    private static final long HEARTBEAT_INTERVAL_MILLIS = 10 * 60 * 1000; // 10分钟

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private UserConverter userConverter;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private UserDailyLoginDurationMapper userDailyLoginDurationMapper;
    @Value("${online-exam.login.captcha.enabled}")
    private boolean captchaEnabled;
    @Value("${register.gift-tokens:0}")
    private Integer giftTokens;
    @Value("${register.first-purchase-discount:1.0}")
    private Double firstPurchaseDiscount;
    @Value("${register.welcome-message:}")
    private String welcomeMessage;
    @Resource
    private UserTokenMapper userTokenMapper;
    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    private ILogService logService;

    /**
     * 登录
     *
     * @param request
     * @param loginForm 入参
     * @return 响应
     */
    @SneakyThrows(JsonProcessingException.class)
    @Override
    public Result<String> login(HttpServletRequest request, LoginForm loginForm) {
        // 先判断用户是否通过校验
        String s = stringRedisTemplate.opsForValue().get("isVerifyCode" + request.getSession().getId());
        if (StringUtils.isBlank(s) && captchaEnabled) {
            throw new ServiceRuntimeException("请先验证验证码");
        }
        // 根据用户名获取用户信息
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, loginForm.getUsername());
        User user = userMapper.selectOne(wrapper);
        // 判读用户名是否存在
        if (Objects.isNull(user)) {
            throw new ServiceRuntimeException("该用户不存在");
        }
        if (user.getIsDeleted() == 1) {
            throw new ServiceRuntimeException("该用户已注销");
        }
        
        // 检查审核状态
        if (user.getAuditStatus() != null && user.getAuditStatus() == 0) {
            throw new ServiceRuntimeException("账号正在审核中，请等待管理员审核");
        }
        if (user.getAuditStatus() != null && user.getAuditStatus() == 2) {
            throw new ServiceRuntimeException("账号审核未通过，请联系管理员");
        }
        
        // 使用BCrypt进行密码验证（支持明文和加密密码兼容）
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean passwordMatches = passwordEncoder.matches(loginForm.getPassword(), user.getPassword());
        
        // 兼容旧数据：如果BCrypt不匹配，尝试明文比较（用于迁移过渡）
        if (!passwordMatches && !loginForm.getPassword().equals(user.getPassword())) {
            throw new ServiceRuntimeException("密码错误");
        }
        user.setPassword(null);
        // 根据用户角色代码
        List<String> permissions = roleMapper.selectCodeById(user.getRoleId());

        // 数据库获取的权限是字符串springSecurity需要实现GrantedAuthority接口类型，所有这里做一个类型转换
        List<SimpleGrantedAuthority> userPermissions = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority("role_" + permission)).collect(java.util.stream.Collectors.toList());

        // 创建一个sysUserDetails对象，该类实现了UserDetails接口
        SysUserDetails sysUserDetails = new SysUserDetails(user);
        // 把转型后的权限放进sysUserDetails对象
        sysUserDetails.setPermissions(userPermissions);
        // 将用户序列化 转为字符串
        String userInfo = objectMapper.writeValueAsString(user);
        // 创建token
        String token = jwtUtil.createJwt(userInfo, userPermissions.stream().map(String::valueOf).collect(java.util.stream.Collectors.toList()));
        // 把token放到redis中
        stringRedisTemplate.opsForValue().set("token:" + request.getSession().getId(), token, 30, TimeUnit.MINUTES);

        // 封装用户的身份信息，为后续的身份验证和授权操作提供必要的输入
        // 创建UsernamePasswordAuthenticationToken  参数：用户信息，密码，权限列表
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(sysUserDetails, user.getPassword(), userPermissions);

        // 可选，添加Web认证细节
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 用户信息存放进上下文
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        // 用户信息放入
        // 清除redis通过校验表示
        stringRedisTemplate.delete("isVerifyCode" + request.getSession().getId());

        // 记录日志
        String device = httpServletRequest.getHeader("User-Agent");
        String ipRegion = Optional.ofNullable(IPUtils.getIPRegion(httpServletRequest)).orElse("暂无信息");
        Log log = Log.builder()
                .place(ipRegion)
                .device(extractDeviceType(device))
                .behavior("设备登录")
                .userId(user.getId()).build();
        logService.add(log);
        return Result.success("登录成功", token);
    }


    /**
     * 获取设备名称
     * @param userAgent
     * @return
     */
    public static String extractDeviceType(String userAgent) {
        // 定义正则表达式模式
        String pattern = "\\((.*?);";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(userAgent);
        if (m.find()) {
            // 返回匹配到的设备类型
            return m.group(1);
        }
        return null;
    }

    /**
     * 注销
     *
     * @param request request对象，需要清除session里面的内容
     * @return
     */
    @Override
    public Result<String> logout(HttpServletRequest request) {
        // 清除session
        HttpSession session = request.getSession(false);
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(token) && session != null) {
            // 记录日志
            String device = httpServletRequest.getHeader("User-Agent");
            String ipRegion = Optional.ofNullable(IPUtils.getIPRegion(httpServletRequest)).orElse("暂无信息");
            Log log = Log.builder()
                    .place(ipRegion)
                    .device(extractDeviceType(device))
                    .behavior("设备登出")
                    .userId(SecurityUtil.getUserId()).build();
            logService.add(log);
            token = token.substring(7);
            stringRedisTemplate.delete("token:" + request.getSession().getId());
            session.invalidate();
        }
        return Result.success("退出成功");
    }

    /**
     * 获取图形验证码
     *
     * @param request  request对象，获取sessionId
     * @param response response对象，响应图片
     */
    @SneakyThrows(IOException.class)
    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        // 生成线性图形验证码的静态方法，参数：图片宽，图片高，验证码字符个数 干扰个数
        LineCaptcha captcha = CaptchaUtil
                .createLineCaptcha(200, 100, 4, 300);

        // 把验证码存放进redis
        // 获取验证码
        String code = captcha.getCode();
        String key = "code" + request.getSession().getId();
        stringRedisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        // 把图片响应到输出流
        response.setContentType("image/jpeg");
        ServletOutputStream os = response.getOutputStream();
        captcha.write(os);
        os.close();
    }

    /**
     * 验证验证码
     *
     * @param request request对象获取sessionId
     * @param code    用户输入的验证码
     * @return
     */
    @Override
    public Result<String> verifyCode(HttpServletRequest request, String code) {
        String key = "code" + request.getSession().getId();
        String rightCode = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(rightCode)) {
            throw new ServiceRuntimeException("验证码已过期");
        }
        if (!rightCode.equalsIgnoreCase(code)) {
            throw new ServiceRuntimeException("验证码错误");
        }
        // 验证码校验后redis清除验证码，避免重复使用
        stringRedisTemplate.delete(key);
        // 验证码校验后redis存入校验成功，避免用户登录和注册时不验证验证码直接提交
        stringRedisTemplate.opsForValue().set("isVerifyCode" + request.getSession().getId(), "1", 5, TimeUnit.MINUTES);
        return Result.success("验证码校验成功");
    }

    /**
     * 注册用户
     *
     * @param request  request对象，用于获取sessionId
     * @param userForm 用户信息
     * @return
     */
    @Override
    public Result<String> register(HttpServletRequest request, UserForm userForm) {
        // 判断验证码
        String s = stringRedisTemplate.opsForValue().get("isVerifyCode" + request.getSession().getId());
        if (StringUtils.isBlank(s)) {
            throw new ServiceRuntimeException("请先验证验证码");
        }
        // 判断两次密码是否一致
        if (!userForm.getPassword().equals(userForm.getCheckedPassword())) {
            throw new ServiceRuntimeException("两次密码不一致");
        }
        
        User user = userConverter.fromToEntity(userForm);
        user.setPassword(user.getPassword());
        
        // 根据角色类型设置角色ID和审核状态
        String roleType = userForm.getRoleType();
        if (roleType == null || roleType.isEmpty()) {
            throw new ServiceRuntimeException("请选择身份");
        }
        
        switch (roleType) {
            case "student":
                user.setRoleId(1); // 学生角色
                user.setAuditStatus(1); // 学生自动审核通过
                break;
            case "teacher":
                user.setRoleId(2); // 老师角色
                user.setAuditStatus(0); // 老师需要管理员审核
                break;
            case "admin":
                user.setRoleId(3); // 管理员角色
                user.setAuditStatus(0); // 管理员需要管理员审核
                break;
            default:
                throw new ServiceRuntimeException("无效的身份类型");
        }
        
        userMapper.insert(user);
        
        // 新用户注册赠送tokens
        String giftMessage = "";
        if (giftTokens != null && giftTokens > 0 && user.getAuditStatus() == 1) {
            // 创建用户token记录并赠送tokens
            UserToken userToken = new UserToken();
            userToken.setUserId(user.getId());
            userToken.setBalance(giftTokens);
            userToken.setTotalPurchased(giftTokens);
            userToken.setTotalConsumed(0);
            userTokenMapper.insert(userToken);
            
            giftMessage = "，系统已赠送您" + giftTokens + "个Tokens";
            if (firstPurchaseDiscount != null && firstPurchaseDiscount < 1.0) {
                int discountPercent = (int)(firstPurchaseDiscount * 10);
                giftMessage += "，首充享" + discountPercent + "折优惠！";
            }
        }
        
        // 注册成功把redis的是否通过校验验证码删除，防止用户注册后立马登录，还可以使用
        stringRedisTemplate.delete("isVerifyCode" + request.getSession().getId());
        
        // 根据审核状态返回不同的消息
        if (user.getAuditStatus() == 1) {
            return Result.success("注册成功，请登录" + giftMessage);
        } else {
            return Result.success("注册申请已提交，请等待管理员审核");
        }
    }
    


    /**
     * 用户发送心跳，更新最后活跃时间。
     *
     * @return
     */
    @Override
    public Result<String> sendHeartbeat(HttpServletRequest request) {
        // 创建Redis键
        String key = HEARTBEAT_KEY_PREFIX + SecurityUtil.getUserId();
        if (SecurityUtil.getRoleCode() == 1) {
            // 删除该键值并获取上一次的心跳时间
            String lastHeartbeatStr = stringRedisTemplate.opsForValue().get(key);
            // 获取当前时间
            LocalDateTime utcTime = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime now = utcTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
            // 设置新的时间
            stringRedisTemplate.opsForValue().set(key, now.toString());
            LocalDateTime lastHeartbeat = null;
            // 将上次时间字符串转换为时间对象
            if (lastHeartbeatStr == null) {
                lastHeartbeat = now;
            } else {
                lastHeartbeat = LocalDateTime.parse(lastHeartbeatStr);
            }
            // 计算上次和现在过了多久 连个时间的时间差
            Duration durationSinceLastHeartbeat = Duration.between(lastHeartbeat, now);
            // 获取今天日期
            LocalDate date = DateTimeUtil.getDate();
            // 实现累加逻辑，比如更新数据库中的记录
            // 获取当前用户的今天的记录
            Integer userId = SecurityUtil.getUserId();
            UserDailyLoginDuration userDailyLogin = userDailyLoginDurationMapper.getTodayRecord(userId, date);
            // 如果记录为空
            if (Objects.isNull(userDailyLogin)) {
                // 如果没记录
                UserDailyLoginDuration userDailyLoginDuration = new UserDailyLoginDuration();
                // 设置用户id
                userDailyLoginDuration.setUserId(userId);
                // 设置今天日期
                userDailyLoginDuration.setLoginDate(date);
                // 存入秒数
                userDailyLoginDuration.setTotalSeconds((int) durationSinceLastHeartbeat.getSeconds());
                userDailyLoginDurationMapper.insert(userDailyLoginDuration);
            } else {
                // 如果有记录
                // 累加今天的时长
                userDailyLogin.setTotalSeconds(userDailyLogin.getTotalSeconds()
                        + (int) durationSinceLastHeartbeat.getSeconds());
                userDailyLoginDurationMapper.updateById(userDailyLogin);
            }
        }
        return Result.success("请求成功");
    }
}