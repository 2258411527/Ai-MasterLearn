package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.vo.token.record.ExerciseRecordVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mapstruct.Mapper;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/30 11:39 AM
 */
@Mapper(componentModel = "spring")
public interface RecordConverter {

    Page<ExerciseRecordVO> pageRepoEntityToVo(Page<Repo> page);

}
