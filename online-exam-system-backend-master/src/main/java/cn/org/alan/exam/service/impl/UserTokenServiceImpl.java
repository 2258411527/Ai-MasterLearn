package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.TokenPackageMapper;
import cn.org.alan.exam.mapper.TokenTransactionMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.mapper.UserTokenMapper;
import cn.org.alan.exam.model.entity.TokenPackage;
import cn.org.alan.exam.model.entity.TokenTransaction;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.entity.UserToken;
import cn.org.alan.exam.model.form.token.TokenAdjustForm;
import cn.org.alan.exam.model.form.token.TokenPackageForm;
import cn.org.alan.exam.model.form.token.TokenPurchaseForm;
import cn.org.alan.exam.model.vo.token.AdminUserTokenVO;
import cn.org.alan.exam.model.vo.token.TokenPackageVO;
import cn.org.alan.exam.model.vo.token.TokenTransactionVO;
import cn.org.alan.exam.model.vo.token.UserTokenVO;
import cn.org.alan.exam.service.IUserTokenService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserTokenServiceImpl extends ServiceImpl<UserTokenMapper, UserToken> implements IUserTokenService {

    @Autowired
    private UserTokenMapper userTokenMapper;

    @Autowired
    private TokenTransactionMapper tokenTransactionMapper;

    @Autowired
    private TokenPackageMapper tokenPackageMapper;

    @Autowired
    private UserMapper userMapper;

    private static final Map<Integer, String> TYPE_NAME_MAP = new HashMap<>();

    static {
        TYPE_NAME_MAP.put(TokenTransaction.TYPE_PURCHASE, "充值");
        TYPE_NAME_MAP.put(TokenTransaction.TYPE_CONSUME, "消费");
        TYPE_NAME_MAP.put(TokenTransaction.TYPE_ADMIN_ADJUST, "管理员调整");
    }

    private static final Map<String, String> AI_SERVICE_NAME_MAP = new HashMap<>();

    static {
        AI_SERVICE_NAME_MAP.put("chat", "AI对话");
        AI_SERVICE_NAME_MAP.put("enhanced_chat", "AI增强对话");
        AI_SERVICE_NAME_MAP.put("question_chat", "AI题目问答");
        AI_SERVICE_NAME_MAP.put("analyze", "AI解析题目");
    }

    @Override
    public Result<UserTokenVO> getBalance() {
        Integer userId = SecurityUtil.getUserId();
        UserToken userToken = getOrCreateUserToken(userId);
        UserTokenVO vo = new UserTokenVO();
        vo.setUserId(userToken.getUserId());
        vo.setBalance(userToken.getBalance());
        vo.setTotalPurchased(userToken.getTotalPurchased());
        vo.setTotalConsumed(userToken.getTotalConsumed());
        return Result.success("查询成功", vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> purchase(TokenPurchaseForm form) {
        Integer userId = SecurityUtil.getUserId();
        TokenPackage tokenPackage = tokenPackageMapper.selectById(form.getPackageId());
        if (tokenPackage == null || !tokenPackage.getIsActive()) {
            return Result.failed("套餐不存在或已下架");
        }

        UserToken userToken = getOrCreateUserToken(userId);
        int rows = userTokenMapper.addBalance(userId, tokenPackage.getTokens());
        if (rows == 0) {
            return Result.failed("充值失败，请重试");
        }

        TokenTransaction transaction = new TokenTransaction();
        transaction.setUserId(userId);
        transaction.setType(TokenTransaction.TYPE_PURCHASE);
        transaction.setAmount(tokenPackage.getTokens());
        transaction.setBalanceAfter(userToken.getBalance() + tokenPackage.getTokens());
        transaction.setDescription("购买套餐: " + tokenPackage.getName());
        tokenTransactionMapper.insert(transaction);

        log.info("用户{}购买Token套餐: {}, 数量: {}", userId, tokenPackage.getName(), tokenPackage.getTokens());
        return Result.success("购买成功，已充值" + tokenPackage.getTokens() + " Tokens");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> consume(Integer userId, Integer amount, String aiServiceType, String description) {
        if (amount == null || amount <= 0) {
            return Result.failed("消费数量无效");
        }

        int rows = userTokenMapper.deductBalance(userId, amount);
        if (rows == 0) {
            return Result.failed("Token余额不足，请先充值");
        }

        UserToken userToken = userTokenMapper.selectOne(
                new LambdaQueryWrapper<UserToken>().eq(UserToken::getUserId, userId));

        TokenTransaction transaction = new TokenTransaction();
        transaction.setUserId(userId);
        transaction.setType(TokenTransaction.TYPE_CONSUME);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(userToken.getBalance());
        transaction.setAiServiceType(aiServiceType);
        transaction.setDescription(description);
        tokenTransactionMapper.insert(transaction);

        log.info("用户{}消费Token: {}, 服务类型: {}, 剩余: {}", userId, amount, aiServiceType, userToken.getBalance());
        return Result.success("消费成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> adminAdjust(TokenAdjustForm form) {
        Integer userId = form.getUserId();
        Integer amount = form.getAmount();
        if (amount == null || amount == 0) {
            return Result.failed("调整数量不能为0");
        }

        UserToken userToken = getOrCreateUserToken(userId);

        if (amount > 0) {
            userTokenMapper.addBalance(userId, amount);
        } else {
            if (userToken.getBalance() < Math.abs(amount)) {
                return Result.failed("用户余额不足，无法扣减");
            }
            userTokenMapper.deductBalance(userId, Math.abs(amount));
        }

        UserToken updated = userTokenMapper.selectOne(
                new LambdaQueryWrapper<UserToken>().eq(UserToken::getUserId, userId));

        TokenTransaction transaction = new TokenTransaction();
        transaction.setUserId(userId);
        transaction.setType(TokenTransaction.TYPE_ADMIN_ADJUST);
        transaction.setAmount(Math.abs(amount));
        transaction.setBalanceAfter(updated.getBalance());
        transaction.setDescription(form.getReason() != null ? form.getReason() : "管理员调整");
        tokenTransactionMapper.insert(transaction);

        log.info("管理员调整用户{}Token: {}, 原因: {}", userId, amount, form.getReason());
        return Result.success("调整成功");
    }

    @Override
    public Result<IPage<TokenTransactionVO>> getTransactionHistory(Integer pageNum, Integer pageSize) {
        Integer userId = SecurityUtil.getUserId();
        Page<TokenTransaction> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TokenTransaction> wrapper = new LambdaQueryWrapper<TokenTransaction>()
                .eq(TokenTransaction::getUserId, userId)
                .orderByDesc(TokenTransaction::getCreateTime);

        IPage<TokenTransaction> transactionPage = tokenTransactionMapper.selectPage(page, wrapper);

        IPage<TokenTransactionVO> voPage = transactionPage.convert(this::toTransactionVO);
        return Result.success("查询成功", voPage);
    }

    @Override
    public Result<List<TokenPackageVO>> getAvailablePackages() {
        LambdaQueryWrapper<TokenPackage> wrapper = new LambdaQueryWrapper<TokenPackage>()
                .eq(TokenPackage::getIsActive, true)
                .orderByAsc(TokenPackage::getSortOrder);
        List<TokenPackage> packages = tokenPackageMapper.selectList(wrapper);
        List<TokenPackageVO> voList = packages.stream().map(this::toPackageVO).collect(Collectors.toList());
        return Result.success("查询成功", voList);
    }

    @Override
    public Result<Boolean> checkTokenSufficient(Integer userId, Integer amount) {
        UserToken userToken = userTokenMapper.selectOne(
                new LambdaQueryWrapper<UserToken>().eq(UserToken::getUserId, userId));
        if (userToken == null || userToken.getBalance() < amount) {
            return Result.success("查询成功", false);
        }
        return Result.success("查询成功", true);
    }

    @Override
    public Result<IPage<TokenPackageVO>> adminGetPackages(Integer pageNum, Integer pageSize) {
        Page<TokenPackage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TokenPackage> wrapper = new LambdaQueryWrapper<TokenPackage>()
                .orderByAsc(TokenPackage::getSortOrder);
        IPage<TokenPackage> packagePage = tokenPackageMapper.selectPage(page, wrapper);
        IPage<TokenPackageVO> voPage = packagePage.convert(this::toPackageVO);
        return Result.success("查询成功", voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> adminAddPackage(TokenPackageForm form) {
        TokenPackage pkg = new TokenPackage();
        pkg.setName(form.getName());
        pkg.setTokens(form.getTokens());
        pkg.setPrice(form.getPrice());
        pkg.setDiscount(form.getDiscount());
        pkg.setOriginalPrice(form.getOriginalPrice());
        pkg.setDescription(form.getDescription());
        pkg.setIsActive(form.getIsActive() != null ? form.getIsActive() : true);
        pkg.setSortOrder(form.getSortOrder() != null ? form.getSortOrder() : 0);
        tokenPackageMapper.insert(pkg);
        log.info("管理员添加Token套餐: {}", form.getName());
        return Result.success("添加成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> adminUpdatePackage(Integer id, TokenPackageForm form) {
        TokenPackage pkg = tokenPackageMapper.selectById(id);
        if (pkg == null) {
            return Result.failed("套餐不存在");
        }
        pkg.setName(form.getName());
        pkg.setTokens(form.getTokens());
        pkg.setPrice(form.getPrice());
        pkg.setDiscount(form.getDiscount());
        pkg.setOriginalPrice(form.getOriginalPrice());
        pkg.setDescription(form.getDescription());
        if (form.getIsActive() != null) {
            pkg.setIsActive(form.getIsActive());
        }
        if (form.getSortOrder() != null) {
            pkg.setSortOrder(form.getSortOrder());
        }
        tokenPackageMapper.updateById(pkg);
        log.info("管理员更新Token套餐: id={}, name={}", id, form.getName());
        return Result.success("更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> adminDeletePackage(Integer id) {
        TokenPackage pkg = tokenPackageMapper.selectById(id);
        if (pkg == null) {
            return Result.failed("套餐不存在");
        }
        tokenPackageMapper.deleteById(id);
        log.info("管理员删除Token套餐: id={}, name={}", id, pkg.getName());
        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<AdminUserTokenVO>> adminGetUserTokens(Integer pageNum, Integer pageSize, String realName) {
        Page<UserToken> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserToken> wrapper = new LambdaQueryWrapper<UserToken>()
                .orderByDesc(UserToken::getUpdateTime);

        if (realName != null && !realName.trim().isEmpty()) {
            List<User> users = userMapper.selectList(
                    new LambdaQueryWrapper<User>().like(User::getRealName, realName.trim()));
            if (users.isEmpty()) {
                return Result.success("查询成功", new Page<>(pageNum, pageSize));
            }
            List<Integer> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            wrapper.in(UserToken::getUserId, userIds);
        }

        IPage<UserToken> tokenPage = userTokenMapper.selectPage(page, wrapper);

        List<Integer> userIds = tokenPage.getRecords().stream()
                .map(UserToken::getUserId).collect(Collectors.toList());
        Map<Integer, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        Map<Integer, User> finalUserMap = userMap;
        IPage<AdminUserTokenVO> voPage = tokenPage.convert(t -> {
            AdminUserTokenVO vo = new AdminUserTokenVO();
            vo.setId(t.getId());
            vo.setUserId(t.getUserId());
            vo.setBalance(t.getBalance());
            vo.setTotalPurchased(t.getTotalPurchased());
            vo.setTotalConsumed(t.getTotalConsumed());
            vo.setUpdateTime(t.getUpdateTime());
            User user = finalUserMap.get(t.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
                vo.setRealName(user.getRealName());
            }
            return vo;
        });

        return Result.success("查询成功", voPage);
    }

    private UserToken getOrCreateUserToken(Integer userId) {
        UserToken userToken = userTokenMapper.selectOne(
                new LambdaQueryWrapper<UserToken>().eq(UserToken::getUserId, userId));
        if (userToken == null) {
            userToken = new UserToken();
            userToken.setUserId(userId);
            userToken.setBalance(0);
            userToken.setTotalPurchased(0);
            userToken.setTotalConsumed(0);
            userTokenMapper.insert(userToken);
        }
        return userToken;
    }

    private TokenTransactionVO toTransactionVO(TokenTransaction t) {
        TokenTransactionVO vo = new TokenTransactionVO();
        vo.setId(t.getId());
        vo.setType(t.getType());
        vo.setTypeName(TYPE_NAME_MAP.getOrDefault(t.getType(), "未知"));
        vo.setAmount(t.getAmount());
        vo.setBalanceAfter(t.getBalanceAfter());
        vo.setDescription(t.getDescription());
        vo.setAiServiceType(t.getAiServiceType() != null
                ? AI_SERVICE_NAME_MAP.getOrDefault(t.getAiServiceType(), t.getAiServiceType())
                : null);
        vo.setCreateTime(t.getCreateTime());
        return vo;
    }

    private TokenPackageVO toPackageVO(TokenPackage p) {
        TokenPackageVO vo = new TokenPackageVO();
        vo.setId(p.getId());
        vo.setName(p.getName());
        vo.setTokens(p.getTokens());
        vo.setPrice(p.getPrice());
        vo.setDiscount(p.getDiscount());
        vo.setOriginalPrice(p.getOriginalPrice());
        if (p.getPrice() != null && p.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            vo.setUnitPrice(BigDecimal.valueOf(p.getTokens()).divide(p.getPrice(), 2, RoundingMode.HALF_UP));
        }
        vo.setDescription(p.getDescription());
        return vo;
    }
}
