package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.AlipayConfigEntity;
import cn.org.alan.exam.model.form.alipay.AlipayConfigForm;
import cn.org.alan.exam.model.vo.alipay.AlipayConfigVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IAlipayConfigService extends IService<AlipayConfigEntity> {

    Result<IPage<AlipayConfigVO>> getConfigPage(Integer pageNum, Integer pageSize, String configName);

    Result<String> addConfig(AlipayConfigForm form);

    Result<String> updateConfig(Integer id, AlipayConfigForm form);

    Result<String> deleteConfig(Integer id);

    Result<String> activateConfig(Integer id);

    Result<AlipayConfigEntity> getActiveConfig();
}
