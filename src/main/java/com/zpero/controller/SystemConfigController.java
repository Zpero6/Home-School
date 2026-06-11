package com.zpero.controller;

import com.zpero.common.result.Result;
import com.zpero.dto.system.SystemConfigDTO;
import com.zpero.dto.system.SystemConfigQueryDTO;
import com.zpero.service.SystemConfigService;
import com.zpero.vo.system.SystemConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system-configs")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<List<SystemConfigVO>> list(SystemConfigQueryDTO queryDTO) {
        return Result.success(systemConfigService.list(queryDTO));
    }

    @GetMapping("/{configKey}")
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<SystemConfigVO> getByKey(@PathVariable String configKey) {
        return Result.success(systemConfigService.getByKey(configKey));
    }

    @PutMapping("/{configKey}")
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<Void> saveOrUpdate(@PathVariable String configKey,
                                     @RequestBody SystemConfigDTO dto) {
        systemConfigService.saveOrUpdate(configKey, dto);
        return Result.success();
    }

    @DeleteMapping("/{configKey}")
    @PreAuthorize("hasRole('SCHOOL')")
    public Result<Void> delete(@PathVariable String configKey) {
        systemConfigService.delete(configKey);
        return Result.success();
    }
}
