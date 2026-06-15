package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.UserKaogongInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考公专属信息Mapper接口
 */
@Mapper
public interface UserKaogongInfoMapper extends BaseMapper<UserKaogongInfo> {
}