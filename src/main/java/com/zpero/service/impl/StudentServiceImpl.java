package com.zpero.service.impl;

import com.alibaba.excel.EasyExcel;
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
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.StudentService;
import com.zpero.vo.student.export.StudentExportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;
    private final DataScopeProvider dataScopeProvider;

    @Override
    public PageResult<Student> queryPage(StudentQueryDTO queryDTO) {
        StudentQueryDTO query = queryDTO == null ? new StudentQueryDTO() : queryDTO;
        LambdaQueryWrapper<Student> wrapper = buildQueryWrapper(query);


        Page<Student> result = studentMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
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

    @Override
    public void exportStudents(StudentQueryDTO queryDTO, HttpServletResponse response) {
        StudentQueryDTO query = queryDTO == null ? new StudentQueryDTO() : queryDTO;
        LambdaQueryWrapper<Student> wrapper = buildQueryWrapper(query);
        List<StudentExportVO> exportList = studentMapper.selectList(wrapper)
                .stream()
                .map(StudentExportVO::new)
                .toList();

        String filename = URLEncoder.encode("students.xlsx", StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        try {
            EasyExcel.write(response.getOutputStream(), StudentExportVO.class)
                    .sheet("学生列表")
                    .doWrite(exportList);
        } catch (IOException e) {
            throw new BusinessException(500, "导出学生列表失败");
        }
    }

    private LambdaQueryWrapper<Student> buildQueryWrapper(StudentQueryDTO queryDTO) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        dataScopeProvider.applyCollegeAndCounselorScope(wrapper,
                Student::getCollegeId,
                Student::getCounselorId);

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
        wrapper.orderByDesc(Student::getCreateTime)
                .orderByDesc(Student::getId);
        return wrapper;
    }
}
