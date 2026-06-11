package com.zpero.service;

import com.zpero.common.result.PageResult;
import com.zpero.dto.classes.ClassInfoDTO;
import com.zpero.dto.classes.ClassInfoQueryDTO;
import com.zpero.entity.ClassInfo;

public interface ClassInfoService {
    PageResult<ClassInfo> queryPage(ClassInfoQueryDTO queryDTO);

    ClassInfo getById(Long id);

    Long createClassInfo(ClassInfoDTO dto);

    void updateClassInfo(Long id, ClassInfoDTO dto);

    void deleteClassInfo(Long id);
}
