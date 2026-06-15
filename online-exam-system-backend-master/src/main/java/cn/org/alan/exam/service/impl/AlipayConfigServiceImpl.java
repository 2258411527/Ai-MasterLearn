package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.AlipayConfigMapper;
import cn.org.alan.exam.model.entity.AlipayConfigEntity;
import cn.org.alan.exam.model.form.alipay.AlipayConfigForm;
import cn.org.alan.exam.model.vo.alipay.AlipayConfigVO;
import cn.org.alan.exam.service.IAlipayConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AlipayConfigServiceImpl extends ServiceImpl<AlipayConfigMapper, AlipayConfigEntity> implements IAlipayConfigService {

    @Override
    public Result<IPage<AlipayConfigVO>> getConfigPage(Integer pageNum, Integer pageSize, String configName) {
        Page<AlipayConfigEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AlipayConfigEntity> wrapper = new LambdaQueryWrapper<AlipayConfigEntity>()
                .orderByDesc(AlipayConfigEntity::getIsActive)
                .orderByDesc(AlipayConfigEntity::getUpdateTime);

        if (configName != null && !configName.trim().isEmpty()) {
            wrapper.like(AlipayConfigEntity::getConfigName, configName.trim());
        }

        IPage<AlipayConfigEntity> configPage = this.page(page, wrapper);
        IPage<AlipayConfigVO> voPage = configPage.convert(this::toVO);
        return Result.success("查询成功", voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addConfig(AlipayConfigForm form) {
        AlipayConfigEntity entity = new AlipayConfigEntity();
        entity.setConfigName(form.getConfigName());
        entity.setAppId(form.getAppId());
        entity.setPrivateKey(form.getPrivateKey());
        entity.setAlipayPublicKey(form.getAlipayPublicKey());
        entity.setServerUrl(form.getServerUrl() != null ? form.getServerUrl() : "https://openapi.alipay.com/gateway.do");
        entity.setNotifyUrl(form.getNotifyUrl());
        entity.setReturnUrl(form.getReturnUrl());
        entity.setSignType(form.getSignType() != null ? form.getSignType() : "RSA2");
        entity.setTestMode(form.getTestMode() != null ? form.getTestMode() : false);
        entity.setIsActive(false);
        this.save(entity);
        log.info("添加支付宝配置: {}", form.getConfigName());
        return Result.success("添加成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateConfig(Integer id, AlipayConfigForm form) {
        AlipayConfigEntity entity = this.getById(id);
        if (entity == null) {
            return Result.failed("配置不存在");
        }
        entity.setConfigName(form.getConfigName());
        entity.setAppId(form.getAppId());
        if (form.getPrivateKey() != null && !form.getPrivateKey().isEmpty()
                && !form.getPrivateKey().contains("*")) {
            entity.setPrivateKey(form.getPrivateKey());
        }
        if (form.getAlipayPublicKey() != null && !form.getAlipayPublicKey().isEmpty()
                && !form.getAlipayPublicKey().contains("*")) {
            entity.setAlipayPublicKey(form.getAlipayPublicKey());
        }
        if (form.getServerUrl() != null) {
            entity.setServerUrl(form.getServerUrl());
        }
        if (form.getNotifyUrl() != null) {
            entity.setNotifyUrl(form.getNotifyUrl());
        }
        if (form.getReturnUrl() != null) {
            entity.setReturnUrl(form.getReturnUrl());
        }
        if (form.getSignType() != null) {
            entity.setSignType(form.getSignType());
        }
        if (form.getTestMode() != null) {
            entity.setTestMode(form.getTestMode());
        }
        this.updateById(entity);
        log.info("更新支付宝配置: id={}, name={}", id, form.getConfigName());
        return Result.success("更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteConfig(Integer id) {
        AlipayConfigEntity entity = this.getById(id);
        if (entity == null) {
            return Result.failed("配置不存在");
        }
        if (entity.getIsActive()) {
            return Result.failed("不能删除当前激活的配置，请先激活其他配置");
        }
        this.removeById(id);
        log.info("删除支付宝配置: id={}, name={}", id, entity.getConfigName());
        return Result.success("删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> activateConfig(Integer id) {
        AlipayConfigEntity entity = this.getById(id);
        if (entity == null) {
            return Result.failed("配置不存在");
        }

        List<AlipayConfigEntity> activeList = this.list(
                new LambdaQueryWrapper<AlipayConfigEntity>().eq(AlipayConfigEntity::getIsActive, true));
        for (AlipayConfigEntity active : activeList) {
            active.setIsActive(false);
            this.updateById(active);
        }

        entity.setIsActive(true);
        this.updateById(entity);
        log.info("激活支付宝配置: id={}, name={}", id, entity.getConfigName());
        return Result.success("激活成功");
    }

    @Override
    public Result<AlipayConfigEntity> getActiveConfig() {
        AlipayConfigEntity config = this.getOne(
                new LambdaQueryWrapper<AlipayConfigEntity>().eq(AlipayConfigEntity::getIsActive, true));
        if (config == null) {
            return Result.failed("未配置支付宝参数，请在管理后台配置");
        }
        return Result.success("查询成功", config);
    }

    private AlipayConfigVO toVO(AlipayConfigEntity entity) {
        AlipayConfigVO vo = new AlipayConfigVO();
        vo.setId(entity.getId());
        vo.setConfigName(entity.getConfigName());
        vo.setAppId(entity.getAppId());
        vo.setPrivateKeyMasked(maskKey(entity.getPrivateKey()));
        vo.setAlipayPublicKeyMasked(maskKey(entity.getAlipayPublicKey()));
        vo.setServerUrl(entity.getServerUrl());
        vo.setNotifyUrl(entity.getNotifyUrl());
        vo.setReturnUrl(entity.getReturnUrl());
        vo.setSignType(entity.getSignType());
        vo.setTestMode(entity.getTestMode());
        vo.setIsActive(entity.getIsActive());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private String maskKey(String key) {
        if (key == null || key.length() <= 20) {
            return "****";
        }
        return key.substring(0, 10) + "****" + key.substring(key.length() - 6);
    }
}
