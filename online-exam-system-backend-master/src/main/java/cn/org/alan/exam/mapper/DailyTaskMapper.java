package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.DailyTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI定制学习系统 - 每日任务Mapper
 */
@Mapper
public interface DailyTaskMapper extends BaseMapper<DailyTask> {
}
