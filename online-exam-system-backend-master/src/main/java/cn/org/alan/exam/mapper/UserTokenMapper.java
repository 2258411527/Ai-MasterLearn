package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.UserToken;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface UserTokenMapper extends BaseMapper<UserToken> {

    @Update("UPDATE t_user_token SET balance = balance - #{amount}, total_consumed = total_consumed + #{amount}, update_time = NOW() WHERE user_id = #{userId} AND balance >= #{amount}")
    int deductBalance(@Param("userId") Integer userId, @Param("amount") Integer amount);

    @Update("UPDATE t_user_token SET balance = balance + #{amount}, total_purchased = total_purchased + #{amount}, update_time = NOW() WHERE user_id = #{userId}")
    int addBalance(@Param("userId") Integer userId, @Param("amount") Integer amount);
}
