package com.zpero.security.dataScope;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.zpero.common.exception.BusinessException;
import com.zpero.security.SecurityUtil;
import org.springframework.stereotype.Component;

@Component
public class DataScopeProvider {

    public DataScopeContext current() {

        String roleCode = SecurityUtil.getCurrentUserRoleCode();

        DataScopeContext context = new DataScopeContext();

        if ("ROLE_SCHOOL".equals(roleCode)) {
            context.setAll(true);
            return context;
        }

        if ("ROLE_COLLEGE".equals(roleCode)) {
            context.setCollegeId(SecurityUtil.getCurrentUserCollegeId());
            return context;
        }

        if ("ROLE_COUNSELOR".equals(roleCode)) {
            context.setCollegeId(SecurityUtil.getCurrentUserCollegeId());
            context.setCounselorId(SecurityUtil.getCurrentUserId());
            return context;
        }
        throw new BusinessException(403, "用户没有数据权限");
    }

    // 允许 学院用户操作自己数据 , 允许 学校用户查询所有学院数据
    public <T> void applyCollegeScope(
            LambdaQueryWrapper<T> wrapper,
            SFunction<T, Long> collegeColumn) {

        DataScopeContext scopeContext = current();

        if (scopeContext.isAll()) {
            return;
        }

        if (scopeContext.hasCollegeScope()) {
            wrapper.eq(collegeColumn, scopeContext.getCollegeId());
            return;
        }
        throw new BusinessException(403, "用户没有数据权限");
    }


    //允许  辅导员和学院 人员  范围查询自己能查的数据
    public <T> void applyCollegeAndCounselorScope(
            LambdaQueryWrapper<T> wrapper,
            SFunction<T, Long> collegeColumn,
            SFunction<T, Long> counselorColumn
    ) {
        DataScopeContext scopeContext = current();

        if (scopeContext.isAll()) {
            return;
        }
        if (scopeContext.hasCounselorScope()) {
            wrapper.eq(counselorColumn, scopeContext.getCounselorId());
            return ;
        }
        if (scopeContext.hasCollegeScope()) {
            wrapper.eq(collegeColumn, scopeContext.getCollegeId());
            return;
        }
        throw new BusinessException(403, "用户没有数据权限");
    }

    //允许 辅导员和学院 人员 单个查询
    public void assertCollegeAndCounselorAccess(Long collegeId, Long counselorId) {
        DataScopeContext scopeContext = current();

        if (scopeContext.isAll()) {
            return;
        }
        if (scopeContext.hasCounselorScope() && scopeContext.getCounselorId().equals(counselorId)) {
            return;
        }
        if (scopeContext.hasCollegeScope() && scopeContext.getCollegeId().equals(collegeId)) {
            return;
        }
        throw new BusinessException(403, "用户没有数据权限");
    }

    //只允许 学院人员 操作对应 学院的数据

    /**
     * @Param collegeId  被操作数据的学院ID
     * */
    public void assertCanManageCollege(Long collegeId) {
        DataScopeContext scopeContext = current();

        if (scopeContext.isAll()) {
            return;
        }
        if (scopeContext.hasCounselorScope()) {
            throw new BusinessException(403, "辅导员无权限管理学院");
        }

        if (scopeContext.hasCollegeScope() && scopeContext.getCollegeId().equals(collegeId)) {
            return;
        }
        throw new BusinessException(403, "无权操作学院数据");
    }


}

