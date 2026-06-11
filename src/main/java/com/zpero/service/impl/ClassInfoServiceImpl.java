package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpero.common.exception.BusinessException;
import com.zpero.common.result.PageResult;
import com.zpero.dto.classes.ClassInfoDTO;
import com.zpero.dto.classes.ClassInfoQueryDTO;
import com.zpero.entity.ClassInfo;
import com.zpero.entity.SysUser;
import com.zpero.mapper.ClassInfoMapper;
import com.zpero.mapper.SysUserMapper;
import com.zpero.security.SecurityUtil;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.ClassInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ClassInfoServiceImpl implements ClassInfoService {
    private final ClassInfoMapper classInfoMapper;
    private final DataScopeProvider dataScopeProvider;
    private  final SysUserMapper sysUserMapper;

    @Override
    public ClassInfo getById(Long id) {
        ClassInfo classInfo = classInfoMapper.selectById(id);
        if (classInfo == null) {
            throw new BusinessException(404, "班级不存在");
        }
        dataScopeProvider.assertCollegeAndCounselorAccess(
                classInfo.getCollegeId(),
                classInfo.getCounselorId()
                );
        return classInfo;
    }

    @Override
    public PageResult<ClassInfo> queryPage(ClassInfoQueryDTO queryDTO) {
        LambdaQueryWrapper<ClassInfo> queryWrapper = new LambdaQueryWrapper<>();
        dataScopeProvider.applyCollegeAndCounselorScope(
                queryWrapper,
                ClassInfo::getCollegeId,
                ClassInfo::getCounselorId);

        queryWrapper.like(StringUtils.hasText(queryDTO.getName()),
                ClassInfo::getName,
                queryDTO.getName());
        queryWrapper.eq(queryDTO.getCollegeId() != null,
                ClassInfo::getCollegeId,
                queryDTO.getCollegeId());
        queryWrapper.eq(StringUtils.hasText(queryDTO.getGrade()),
                ClassInfo::getGrade,
                queryDTO.getGrade());
        Page<ClassInfo> result = classInfoMapper.selectPage(
                new Page<>(queryDTO.getPage(), queryDTO.getSize()),
                queryWrapper
        );

        return PageResult.of(result);
    }

    @Override
    public Long createClassInfo(ClassInfoDTO dto) {

        if (dto == null) {
            throw new BusinessException(400, "学生信息不能为空");
        }
        dataScopeProvider.assertCanManageCollege(dto.getCollegeId());

        ClassInfo classInfo = new ClassInfo();
        classInfo.setName(dto.getName());
        classInfo.setCollegeId(dto.getCollegeId());
        // 使用前端下拉选择的辅导员ID
        SysUser counselor = getValidCounselor(dto.getCounselorId(), dto.getCollegeId());
        classInfo.setCounselorId(counselor.getId());
        classInfo.setGrade(dto.getGrade());
        classInfoMapper.insert(classInfo);
        return classInfo.getId();
    }

    @Override
    public void deleteClassInfo(Long id) {
        ClassInfo classInfo = getById(id);
        if (classInfo == null) {
            throw new BusinessException(404, "班级不存在");
        }
        dataScopeProvider.assertCanManageCollege(classInfo.getCollegeId());
        classInfoMapper.deleteById(id);
    }

    @Override
    public void updateClassInfo(Long id, ClassInfoDTO dto) {
        ClassInfo classInfo = getById(id);
        if(classInfo == null){
            throw new BusinessException(404, "班级不存在");
        }
        if(dto == null) {
            throw new BusinessException(400, "班级信息不能为空");
        }
        if(dto.getCollegeId() == null || !dto.getCollegeId().equals(classInfo.getCollegeId())) {
            throw new BusinessException(400, "班级所属学院不可修改");
        }


        dataScopeProvider.assertCanManageCollege(classInfo.getCollegeId());

        SysUser counselor = getValidCounselor(dto.getCounselorId(), dto.getCollegeId());

        classInfo.setName(dto.getName());
        classInfo.setCounselorId(counselor.getId());
        classInfo.setGrade(dto.getGrade());
        classInfoMapper.updateById(classInfo);
    }



    /**
     *    校验辅导员是否有效
     * */
 private SysUser getValidCounselor(Long counselorId, Long collegeId) {
    if (counselorId == null) {
        throw new BusinessException(400, "辅导员不能为空");
    }

    SysUser counselor = sysUserMapper.selectById(counselorId);

    if (counselor == null) {
        throw new BusinessException(400, "辅导员不存在");
    }

    if (!Long.valueOf(3L).equals(counselor.getRoleId())) {
        throw new BusinessException(400, "指定用户不是辅导员");
    }

    if (!collegeId.equals(counselor.getCollegeId())) {
        throw new BusinessException(400, "辅导员不属于该学院");
    }

    return counselor;
}
}
