package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpero.common.exception.BusinessException;
import com.zpero.common.result.PageResult;
import com.zpero.dto.college.CollegeDTO;
import com.zpero.dto.college.CollegeQueryDTO;
import com.zpero.entity.College;
import com.zpero.mapper.CollegeMapper;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.CollegeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CollegeServiceImpl implements CollegeService {

    private final CollegeMapper collegeMapper;
    private final DataScopeProvider dataScopeProvider;

    @Override
    public PageResult<College> queryPage(CollegeQueryDTO queryDTO) {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        dataScopeProvider.applyCollegeScope(
                wrapper, College::getId);

        wrapper.like(StringUtils.hasText(queryDTO.getName()),
                College::getName,
                queryDTO.getName());
        Page<College> page = collegeMapper.selectPage(
                new Page<College>(queryDTO.getPage(),
                        queryDTO.getSize()),
                wrapper);
        return PageResult.of(page);


    }

    @Override
    public College getById(Long id) {
        College college = collegeMapper.selectById(id);
        if (college == null) {
            throw new IllegalArgumentException("学院不存在");
        }
        dataScopeProvider.assertCanViewCollege(college.getId());

        return college;

    }

    @Override
    public Long createCollege(CollegeDTO collegeDTO) {
        dataScopeProvider.assertSchool();
        if (collegeDTO == null || !StringUtils.hasText(collegeDTO.getName())) {
            throw new BusinessException(400, "学院名称不能为空");
        }
        College college = new College();

        college.setName(collegeDTO.getName());

        collegeMapper.insert(college);
        return college.getId();
    }

    @Override
    public void updateCollege(Long id, CollegeDTO collegeDTO) {
        dataScopeProvider.assertSchool();

        College college = collegeMapper.selectById(id);
        if (college == null) {
            throw new BusinessException(404, "学院不存在");
        }
        if (collegeDTO == null || !StringUtils.hasText(collegeDTO.getName())) {
            throw new BusinessException(400, "学院名称不能为空");
        }

        college.setName(collegeDTO.getName());
        collegeMapper.updateById(college);
    }

    @Override
    public void deleteCollege(Long id) {
        dataScopeProvider.assertSchool();
        College college = collegeMapper.selectById(id);
        if (college == null) {
            throw new BusinessException(404, "学院不存在");
        }
        collegeMapper.deleteById(id);

    }
}
