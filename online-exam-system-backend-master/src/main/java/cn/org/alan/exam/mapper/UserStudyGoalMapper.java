package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.UserStudyGoal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户备考目标Mapper接口
 */
@Mapper
public interface UserStudyGoalMapper extends BaseMapper<UserStudyGoal> {
}