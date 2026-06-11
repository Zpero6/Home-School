package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpero.common.exception.BusinessException;
import com.zpero.common.result.PageResult;
import com.zpero.dto.student.StudentDTO;
import com.zpero.dto.student.StudentQueryDTO;
import com.zpero.entity.ClassInfo;
import com.zpero.entity.Student;
import com.zpero.mapper.ClassInfoMapper;
import com.zpero.mapper.StudentMapper;
import com.zpero.security.SecurityUtil;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;
    private final DataScopeProvider dataScopeProvider;

    @Override
    public PageResult<Student> queryPage(StudentQueryDTO queryDTO) {

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        String roleCode = SecurityUtil.getCurrentUserRoleCode();
        dataScopeProvider.applyCollegeAndCounselorScope(wrapper,
                Student::getCollegeId,
                Student::getCounselorId);

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
        dataScopeProvider.assertCollegeAndCounselorAccess(
                student.getCollegeId(),
                student.getCounselorId()
        );
        return student;
    }

    @Override
    public Long createStudent(StudentDTO studentDTO) {

        // 检查班级是否存在
        ClassInfo classInfo = classInfoMapper.selectById(studentDTO.getClassId());
        if (classInfo == null) {
            throw new BusinessException(400, "班级不存在");
        }
        // 让当前班级 所属的学院 号和 用户的学院号比对 , 匹配则 允许操作
        dataScopeProvider.assertCanManageCollege(classInfo.getCollegeId());


        Student student = new Student();
        student.setStudentNo(studentDTO.getStudentNo());
        student.setName(studentDTO.getName());
        student.setIdCard(studentDTO.getIdCard());
        student.setCollegeId(classInfo.getCollegeId());
        student.setClassId(classInfo.getId());
        student.setCounselorId(classInfo.getCounselorId());
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

        ClassInfo classInfo = classInfoMapper.selectById(studentDTO.getClassId());
        if (classInfo == null) {
            throw new BusinessException(400, "班级不存在");
        }

        // 让当前班级 所属的学院 号和 用户的学院号比对 , 匹配则 允许操作
        dataScopeProvider.assertCanManageCollege(student.getCollegeId());
        dataScopeProvider.assertCanManageCollege(classInfo.getCollegeId());

        student.setName(studentDTO.getName());
        student.setCollegeId(classInfo.getCollegeId());
        student.setClassId(classInfo.getId());
        student.setCounselorId(classInfo.getCounselorId());
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
        dataScopeProvider.assertCanManageCollege(student.getCollegeId());
        studentMapper.deleteById(id);
    }
}
