package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpero.common.exception.BusinessException;
import com.zpero.common.result.PageResult;
import com.zpero.dto.student.StudentDTO;
import com.zpero.dto.student.StudentQueryDTO;
import com.zpero.entity.Student;
import com.zpero.mapper.StudentMapper;
import com.zpero.security.SecurityUtil;
import com.zpero.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;

    @Override
    public PageResult<Student> queryPage(StudentQueryDTO queryDTO) {

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        String roleCode = SecurityUtil.getCurrentUserRoleCode();
        if (("ROLE_SCHOOL").equals(roleCode)) {
            //  无限制
        } else if (("ROLE_COLLEGE").equals(roleCode)) {
            wrapper.eq(Student::getCollegeId, SecurityUtil.getCurrentUserCollegeId());
        } else if (("ROLE_COUNSELOR").equals(roleCode)) {
            wrapper.eq(Student::getCounselorId, SecurityUtil.getCurrentUserId());
        } else {
            throw new BusinessException(403, "权限不足");
        }

        // 通过字段查询
        wrapper.like(StringUtils.hasText(queryDTO.getName()),
                Student::getName,
                queryDTO.getName());
        wrapper.eq(StringUtils.hasText(queryDTO.getStudentNo()),
                Student::getStudentNo,
                queryDTO.getStudentNo()
        );
        wrapper.eq(queryDTO.getCollegeId() != null,
                Student::getCollegeId,
                queryDTO.getCollegeId()
        );
        wrapper.eq(queryDTO.getClassId() != null,
                Student::getClassId,
                queryDTO.getClassId()
        );
        wrapper.eq(StringUtils.hasText(queryDTO.getStatus()),
                Student::getStatus,
                queryDTO.getStatus()
        );

        Page<Student> result = studentMapper.selectPage(
                new Page<>(queryDTO.getPage(), queryDTO.getSize()),
                wrapper
        );

        return PageResult.of(result);
    }

    @Override
    public Student getStudentById(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
        String roleCode = SecurityUtil.getCurrentUserRoleCode();

        if (("ROLE_SCHOOL").equals(roleCode)) {
            return student;
        }
        if (("ROLE_COLLEGE").equals(roleCode)) {
            if (!SecurityUtil.getCurrentUserCollegeId().equals(student.getCollegeId())) {
                throw new BusinessException(403, "权限不足, 该学生不属于该学院");
            }
            return student;
        }
        if (("ROLE_COUNSELOR").equals(roleCode)) {
            if (!SecurityUtil.getCurrentUserId().equals(student.getCounselorId())) {
                throw new BusinessException(403, "权限不足, 该学生不属于该辅导员");
            }
            return student;
        }
        throw new BusinessException(403, "权限不足");
    }

    @Override
    public Long createStudent(StudentDTO studentDTO) {
        String roleCode = SecurityUtil.getCurrentUserRoleCode();

        if (studentDTO == null) {
            throw new BusinessException(400, "学生信息不能为空");
        }
        if (("ROLE_COUNSELOR").equals(roleCode)) {
            throw new BusinessException(403, "辅导员不能创建学生");
        }
        if (("ROLE_COLLEGE").equals(roleCode)) {
            Long currentCollegeId = SecurityUtil.getCurrentUserCollegeId();
            if (!studentDTO.getCollegeId().equals(currentCollegeId)) {
                throw new BusinessException(403, "不可新增其他学院学生");
            }
        } else if (!("ROLE_SCHOOL").equals(roleCode)) {
            throw new BusinessException(403, "权限不足");
        }
        //insert function
        Student student = new Student();
        student.setStudentNo(studentDTO.getStudentNo());
        student.setName(studentDTO.getName());
        student.setIdCard(studentDTO.getIdCard());
        student.setCollegeId(studentDTO.getCollegeId());
        student.setClassId(studentDTO.getClassId());
        student.setCounselorId(SecurityUtil.getCurrentUserId());
        student.setEnrollmentYear(studentDTO.getEnrollmentYear());
        student.setStatus(studentDTO.getStatus());
        studentMapper.insert(student);
        return student.getId();
    }

    @Override
    public void updateStudent(Long id, StudentDTO studentDTO) {
        Student student = getStudentById(id);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
        if (studentDTO == null) {
            throw new BusinessException(400, "学生信息不能为空");
        }

        if (studentDTO.getStudentNo() == null
                || !student.getStudentNo().equals(studentDTO.getStudentNo())) {
            throw new BusinessException(400, "学号不允许修改");
        }

        if (studentDTO.getIdCard() == null
                || !student.getIdCard().equals(studentDTO.getIdCard())) {
            throw new BusinessException(400, "身份证号不允许修改");
        }
        String roleCode = SecurityUtil.getCurrentUserRoleCode();
        if (("ROLE_COLLEGE").equals(roleCode)) {
            Long currentCollegeId = SecurityUtil.getCurrentUserCollegeId();
            if (!currentCollegeId.equals(student.getCollegeId())) {
                throw new BusinessException(403, "无权修改该学生");
            }
            if (studentDTO.getCollegeId() != null && !currentCollegeId.equals(studentDTO.getCollegeId())) {
                throw new BusinessException(403, "不可修改其他学院学生");
            }
            if (studentDTO.getCounselorId() != null
                    && !student.getCounselorId().equals(studentDTO.getCounselorId())) {
                throw new BusinessException(403, "暂不允许修改辅导员");
            }
            studentDTO.setCollegeId(currentCollegeId);
        } else if (!("ROLE_SCHOOL").equals(roleCode)) {
            throw new BusinessException(403, "权限不足");
        }

        student.setName(studentDTO.getName());
        student.setCollegeId(studentDTO.getCollegeId());
        student.setClassId(studentDTO.getClassId());
        student.setCounselorId(studentDTO.getCounselorId());
        student.setEnrollmentYear(studentDTO.getEnrollmentYear());
        student.setStatus(studentDTO.getStatus());
        studentMapper.updateById(student);
    }

    @Override
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
        String roleCode = SecurityUtil.getCurrentUserRoleCode();
        if (("ROLE_COUNSELOR").equals(roleCode)) {
            throw new BusinessException(403, "辅导员不能删除学生");
        }
        if (("ROLE_COLLEGE").equals(roleCode)) {
            Long currentCollegeId = SecurityUtil.getCurrentUserCollegeId();
            if (!currentCollegeId.equals(student.getCollegeId())) {
                throw new BusinessException(403, "无权删除该学生");
            }
        } else if (!("ROLE_SCHOOL").equals(roleCode)) {
            throw new BusinessException(403, "权限不足");
        }
        studentMapper.deleteById(id);
    }
}
