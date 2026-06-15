package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.token.TokenOrderVO;

public interface IAlipayService {

    Result<String> createPayment(Integer packageId);

    Result<String> handleNotify(java.util.Map<String, String> params);

    Result<TokenOrderVO> queryOrderStatus(String orderNo);
}
