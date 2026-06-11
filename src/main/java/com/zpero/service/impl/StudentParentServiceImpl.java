package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.parent.StudentParentDTO;
import com.zpero.entity.Student;
import com.zpero.entity.StudentParent;
import com.zpero.mapper.StudentMapper;
import com.zpero.mapper.StudentParentMapper;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.StudentParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentParentServiceImpl implements StudentParentService {

    private static final int DEFAULT_PARENT = 1;
    private static final int NOT_DEFAULT_PARENT = 0;
    private static final String DEFAULT_SOURCE_TYPE = "MANUAL";

    private final StudentParentMapper studentParentMapper;
    private final StudentMapper studentMapper;
    private final DataScopeProvider dataScopeProvider;

    @Override
    public List<StudentParent> listByStudentId(Long studentId) {
        getAccessibleStudent(studentId);

        return studentParentMapper.selectList(
                new LambdaQueryWrapper<StudentParent>()
                        .eq(StudentParent::getStudentId, studentId)
        );
    }

    @Override
    public Long createStudentParent(Long studentId, StudentParentDTO dto) {
        getAccessibleStudent(studentId);
        validateStudentParentDTO(dto);

        StudentParent parent = new StudentParent();
        parent.setStudentId(studentId);
        parent.setName(dto.getName());
        parent.setRelation(dto.getRelation());
        parent.setPhone(dto.getPhone());
        parent.setIsDefault(dto.getIsDefault() == null ? NOT_DEFAULT_PARENT : dto.getIsDefault());
        parent.setSourceType(StringUtils.hasText(dto.getSourceType())
                ? dto.getSourceType()
                : DEFAULT_SOURCE_TYPE);

        if (DEFAULT_PARENT == parent.getIsDefault()) {
            clearDefaultParent(studentId);
        }

        studentParentMapper.insert(parent);
        return parent.getId();
    }

    @Override
    public void updateStudentParent(Long id, StudentParentDTO dto) {
        validateStudentParentDTO(dto);

        StudentParent parent = getAccessibleParent(id);

        parent.setName(dto.getName());
        parent.setRelation(dto.getRelation());
        parent.setPhone(dto.getPhone());
        parent.setSourceType(StringUtils.hasText(dto.getSourceType())
                ? dto.getSourceType()
                : parent.getSourceType());

        if (dto.getIsDefault() != null) {
            if (DEFAULT_PARENT == dto.getIsDefault()) {
                clearDefaultParent(parent.getStudentId());
            }
            parent.setIsDefault(dto.getIsDefault());
        }

        studentParentMapper.updateById(parent);
    }

    @Override
    public void deleteStudentParent(Long id) {
        StudentParent parent = getAccessibleParent(id);
        dataScopeProvider.assertCanManageCollege(
                getAccessibleStudent(parent.getStudentId()).getCollegeId()
        );

        studentParentMapper.deleteById(id);
    }

    @Override
    public void setDefaultParent(Long id) {
        StudentParent parent = getAccessibleParent(id);

        clearDefaultParent(parent.getStudentId());
        parent.setIsDefault(DEFAULT_PARENT);
        studentParentMapper.updateById(parent);
    }

    private StudentParent getAccessibleParent(Long id) {
        StudentParent parent = studentParentMapper.selectById(id);

        if (parent == null) {
            throw new BusinessException(404, "家长联系人不存在");
        }

        getAccessibleStudent(parent.getStudentId());

        return parent;
    }

    private Student getAccessibleStudent(Long studentId) {
        if (studentId == null) {
            throw new BusinessException(400, "学生不能为空");
        }

        Student student = studentMapper.selectById(studentId);

        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        dataScopeProvider.assertCollegeAndCounselorAccess(
                student.getCollegeId(),
                student.getCounselorId()
        );

        return student;
    }

    private void validateStudentParentDTO(StudentParentDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "家长联系人信息不能为空");
        }

        if (!StringUtils.hasText(dto.getName())) {
            throw new BusinessException(400, "家长姓名不能为空");
        }

        if (!StringUtils.hasText(dto.getRelation())) {
            throw new BusinessException(400, "家长关系不能为空");
        }
    }

    private void clearDefaultParent(Long studentId) {
        studentParentMapper.update(
                null,
                new LambdaUpdateWrapper<StudentParent>()
                        .eq(StudentParent::getStudentId, studentId)
                        .set(StudentParent::getIsDefault, NOT_DEFAULT_PARENT)
        );
    }
}
