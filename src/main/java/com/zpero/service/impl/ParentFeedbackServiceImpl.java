package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpero.common.exception.BusinessException;
import com.zpero.common.result.PageResult;
import com.zpero.dto.feedback.ParentFeedbackQueryDTO;
import com.zpero.entity.ParentFeedback;
import com.zpero.entity.Student;
import com.zpero.entity.StudentParent;
import com.zpero.mapper.ParentFeedbackMapper;
import com.zpero.mapper.StudentMapper;
import com.zpero.mapper.StudentParentMapper;
import com.zpero.security.dataScope.DataScopeContext;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.ParentFeedbackService;
import com.zpero.vo.feedback.ParentFeedbackVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentFeedbackServiceImpl implements ParentFeedbackService {

    private final ParentFeedbackMapper parentFeedbackMapper;
    private final StudentMapper studentMapper;
    private final StudentParentMapper studentParentMapper;
    private final DataScopeProvider dataScopeProvider;

    @Override
    public PageResult<ParentFeedbackVO> queryPage(ParentFeedbackQueryDTO queryDTO) {
        ParentFeedbackQueryDTO query = queryDTO == null ? new ParentFeedbackQueryDTO() : queryDTO;

        LambdaQueryWrapper<ParentFeedback> wrapper = new LambdaQueryWrapper<>();
        applyFeedbackDataScope(wrapper);
        wrapper.eq(query.getStudentId() != null,
                        ParentFeedback::getStudentId,
                        query.getStudentId())
                .eq(query.getLetterId() != null,
                        ParentFeedback::getLetterId,
                        query.getLetterId())
                .like(StringUtils.hasText(query.getContent()),
                        ParentFeedback::getContent,
                        query.getContent())
                .orderByDesc(ParentFeedback::getCreateTime)
                .orderByDesc(ParentFeedback::getId);

        Page<ParentFeedback> page = parentFeedbackMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );

        PageResult<ParentFeedbackVO> result = new PageResult<>();
        result.setPage(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords()
                .stream()
                .map(this::toVO)
                .toList());
        return result;
    }

    private void applyFeedbackDataScope(LambdaQueryWrapper<ParentFeedback> wrapper) {
        DataScopeContext context = dataScopeProvider.current();
        if (context.isAll()) {
            return;
        }

        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        if (context.hasCounselorScope()) {
            studentWrapper.eq(Student::getCounselorId, context.getCounselorId());
        } else if (context.hasCollegeScope()) {
            studentWrapper.eq(Student::getCollegeId, context.getCollegeId());
        } else {
            throw new BusinessException(403, "用户没有数据权限");
        }

        List<Long> studentIds = studentMapper.selectList(studentWrapper)
                .stream()
                .map(Student::getId)
                .toList();

        if (studentIds.isEmpty()) {
            wrapper.eq(ParentFeedback::getId, -1L);
            return;
        }
        wrapper.in(ParentFeedback::getStudentId, studentIds);
    }

    private ParentFeedbackVO toVO(ParentFeedback feedback) {
        ParentFeedbackVO vo = new ParentFeedbackVO(feedback);

        Student student = studentMapper.selectById(feedback.getStudentId());
        if (student != null) {
            vo.setStudentName(student.getName());
            vo.setCollegeId(student.getCollegeId());
            vo.setCounselorId(student.getCounselorId());
        }

        StudentParent parent = studentParentMapper.selectById(feedback.getParentId());
        if (parent != null) {
            vo.setParentName(parent.getName());
        }

        return vo;
    }
}
