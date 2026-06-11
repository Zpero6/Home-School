package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.system.SystemConfigDTO;
import com.zpero.dto.system.SystemConfigQueryDTO;
import com.zpero.entity.SystemConfig;
import com.zpero.mapper.SystemConfigMapper;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.SystemConfigService;
import com.zpero.vo.system.SystemConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;
    private final DataScopeProvider dataScopeProvider;

    @Override
    public List<SystemConfigVO> list(SystemConfigQueryDTO queryDTO) {
        dataScopeProvider.assertSchool();
        SystemConfigQueryDTO query = queryDTO == null ? new SystemConfigQueryDTO() : queryDTO;

        return systemConfigMapper.selectList(
                        new LambdaQueryWrapper<SystemConfig>()
                                .like(StringUtils.hasText(query.getConfigKey()),
                                        SystemConfig::getConfigKey,
                                        query.getConfigKey())
                                .orderByAsc(SystemConfig::getConfigKey)
                )
                .stream()
                .map(SystemConfigVO::new)
                .toList();
    }

    @Override
    public SystemConfigVO getByKey(String configKey) {
        dataScopeProvider.assertSchool();
        return new SystemConfigVO(getConfigByKey(configKey));
    }

    @Override
    public String getValue(String configKey, String defaultValue) {
        if (!StringUtils.hasText(configKey)) {
            return defaultValue;
        }

        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, configKey)
        );
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return defaultValue;
        }
        return config.getConfigValue();
    }

    @Override
    public void saveOrUpdate(String configKey, SystemConfigDTO dto) {
        dataScopeProvider.assertSchool();
        validate(configKey, dto);

        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, configKey)
        );
        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(dto.getConfigValue());
            config.setDescription(dto.getDescription());
            systemConfigMapper.insert(config);
            return;
        }

        config.setConfigValue(dto.getConfigValue());
        config.setDescription(dto.getDescription());
        systemConfigMapper.updateById(config);
    }

    @Override
    public void delete(String configKey) {
        dataScopeProvider.assertSchool();
        SystemConfig config = getConfigByKey(configKey);
        systemConfigMapper.deleteById(config.getId());
    }

    private SystemConfig getConfigByKey(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            throw new BusinessException(400, "配置键不能为空");
        }

        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, configKey)
        );
        if (config == null) {
            throw new BusinessException(404, "配置不存在");
        }
        return config;
    }

    private void validate(String configKey, SystemConfigDTO dto) {
        if (!StringUtils.hasText(configKey)) {
            throw new BusinessException(400, "配置键不能为空");
        }
        if (dto == null || !StringUtils.hasText(dto.getConfigValue())) {
            throw new BusinessException(400, "配置值不能为空");
        }
    }
}
