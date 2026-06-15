package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.UserToken;
import cn.org.alan.exam.model.form.token.TokenAdjustForm;
import cn.org.alan.exam.model.form.token.TokenPackageForm;
import cn.org.alan.exam.model.form.token.TokenPurchaseForm;
import cn.org.alan.exam.model.vo.token.AdminUserTokenVO;
import cn.org.alan.exam.model.vo.token.TokenPackageVO;
import cn.org.alan.exam.model.vo.token.TokenTransactionVO;
import cn.org.alan.exam.model.vo.token.UserTokenVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IUserTokenService extends IService<UserToken> {

    Result<UserTokenVO> getBalance();

    Result<String> purchase(TokenPurchaseForm form);

    Result<String> consume(Integer userId, Integer amount, String aiServiceType, String description);

    Result<String> adminAdjust(TokenAdjustForm form);

    Result<IPage<TokenTransactionVO>> getTransactionHistory(Integer pageNum, Integer pageSize);

    Result<List<TokenPackageVO>> getAvailablePackages();

    Result<Boolean> checkTokenSufficient(Integer userId, Integer amount);

    Result<IPage<TokenPackageVO>> adminGetPackages(Integer pageNum, Integer pageSize);

    Result<String> adminAddPackage(TokenPackageForm form);

    Result<String> adminUpdatePackage(Integer id, TokenPackageForm form);

    Result<String> adminDeletePackage(Integer id);

    Result<IPage<AdminUserTokenVO>> adminGetUserTokens(Integer pageNum, Integer pageSize, String realName);
}
