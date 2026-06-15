package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.home.PersonalizedHomeVO;

public interface IPersonalizedHomeService {
    
    Result<PersonalizedHomeVO> getPersonalizedHomeData(Integer userId);
    
    Result<String> refreshCache(Integer userId);
    
    Result<String> clearCache(Integer userId);
}
