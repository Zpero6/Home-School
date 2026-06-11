package com.zpero.service;

import com.zpero.dto.system.SystemConfigDTO;
import com.zpero.dto.system.SystemConfigQueryDTO;
import com.zpero.vo.system.SystemConfigVO;

import java.util.List;

public interface SystemConfigService {

    List<SystemConfigVO> list(SystemConfigQueryDTO queryDTO);

    SystemConfigVO getByKey(String configKey);

    String getValue(String configKey, String defaultValue);

    void saveOrUpdate(String configKey, SystemConfigDTO dto);

    void delete(String configKey);
}
