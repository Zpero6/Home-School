package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.entity.ClassInfo;
import com.zpero.entity.College;
import com.zpero.entity.SysUser;
import com.zpero.mapper.ClassInfoMapper;
import com.zpero.mapper.CollegeMapper;
import com.zpero.mapper.SysUserMapper;
import com.zpero.security.SecurityUtil;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.BasicDataService;
import com.zpero.vo.Counselor.CounselorVo;
import com.zpero.vo.student.StatusVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicDataServiceImpl implements BasicDataService {
    private final CollegeMapper collegeMapper;
    private final ClassInfoMapper classInfoMapper;
    private final SysUserMapper sysUserMapper;
    private final DataScopeProvider dataScopeProvider;

    @Override
    public List<College> listCollege() {


        return collegeMapper.selectList(null);
    }

    @Override
    public List<ClassInfo> listClassInfo(Long collegeId) {
        LambdaQueryWrapper<ClassInfo> queryWrapper = new LambdaQueryWrapper<>();
        dataScopeProvider.applyCollegeAndCounselorScope(
                queryWrapper,
                ClassInfo::getCollegeId,
                ClassInfo::getCounselorId);
        if (collegeId != null) {
            queryWrapper.eq(ClassInfo::getCollegeId, collegeId);
        }
        return classInfoMapper.selectList(queryWrapper);
    }

    /**
     *    给前端返回辅导员的数据 , 前端下拉选择
     */
    @Override
    public List<CounselorVo> listCounselors(Long collegeId) {
        String roleCode = SecurityUtil.getCurrentUserRoleCode();
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getRoleId, 3L);
        if ("ROLE_SCHOOL".equals(roleCode)) {
            if (collegeId != null) {
                queryWrapper.eq(SysUser::getCollegeId, collegeId);
            }
        } else if ("ROLE_COLLEGE".equals(roleCode)) {
            queryWrapper.eq(SysUser::getCollegeId, SecurityUtil.getCurrentUserCollegeId());
        } else if ("ROLE_COUNSELOR".equals(roleCode)) {
            queryWrapper.eq(SysUser::getId, SecurityUtil.getCurrentUserId());
        } else {
            return List.of();
        }
        List<SysUser> sysUsers = sysUserMapper.selectList(queryWrapper);
        return sysUsers.stream().map(
                CounselorVo::new
        ).collect(Collectors.toList());
    }

    @Override
    public List<StatusVo> listStudentStatuses() {
        return List.of(
                new StatusVo("在校", "在校"),
                new StatusVo("休学", "休学"),
                new StatusVo("毕业", "毕业"),
                new StatusVo("退学", "退学")
        );
    }
}
