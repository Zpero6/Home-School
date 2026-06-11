package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.entity.ClassInfo;
import com.zpero.entity.College;
import com.zpero.mapper.ClassInfoMapper;
import com.zpero.mapper.CollegeMapper;
import com.zpero.security.SecurityUtil;
import com.zpero.service.BasicDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BasicDataServiceImpl implements BasicDataService {
    private final CollegeMapper collegeMapper;
    private final ClassInfoMapper classInfoMapper;
    @Override
    public List<College> listCollege() {
        String roleCollege = SecurityUtil.getCurrentUserRoleCode();
        if("ROLE_SCHOOL".equals(roleCollege)){
            return collegeMapper.selectList(null);
        }
        if("ROLE_COLLEGE".equals(roleCollege)){
            Long collegeId = SecurityUtil.getCurrentUserCollegeId();
            return collegeMapper.selectList(null);
        }

        return List.of();
    }

    @Override
    public List<ClassInfo> listClassInfo(Long collegeId) {
        String roleCode = SecurityUtil.getCurrentUserRoleCode();
        LambdaQueryWrapper<ClassInfo> queryWrapper = new LambdaQueryWrapper<>();
        if("ROLE_SCHOOL".equals(roleCode)){
            if(collegeId != null){
                queryWrapper.eq(ClassInfo::getCollegeId, collegeId);
            }
        }else if("ROLE_COLLEGE".equals(roleCode) || "ROLE_COUNSELOR".equals(roleCode)){
            queryWrapper.eq(ClassInfo::getCollegeId, SecurityUtil.getCurrentUserCollegeId());
        }else {
            return List.of();
        }

        return classInfoMapper.selectList(queryWrapper);
    }
}
