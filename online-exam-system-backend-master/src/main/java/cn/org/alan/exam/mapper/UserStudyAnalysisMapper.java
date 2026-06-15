package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.UserStudyAnalysis;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户学习分析Mapper接口
 */
@Mapper
public interface UserStudyAnalysisMapper extends BaseMapper<UserStudyAnalysis> {
}