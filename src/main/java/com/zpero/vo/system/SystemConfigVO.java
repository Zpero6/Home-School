package com.zpero.vo.system;

import com.zpero.entity.SystemConfig;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemConfigVO {

    private Long id;

    private String configKey;

    private String configValue;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public SystemConfigVO(SystemConfig config) {
        this.id = config.getId();
        this.configKey = config.getConfigKey();
        this.configValue = config.getConfigValue();
        this.description = config.getDescription();
        this.createTime = config.getCreateTime();
        this.updateTime = config.getUpdateTime();
    }
}
