package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.UserKaoyanInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考研专属信息Mapper接口
 */
@Mapper
public interface UserKaoyanInfoMapper extends BaseMapper<UserKaoyanInfo> {
}