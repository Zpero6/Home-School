package com.zpero.security.dataScope;

import com.zpero.common.exception.BusinessException;
import com.zpero.entity.Student;
import com.zpero.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentAccessProvider {

    private final StudentMapper studentMapper;
    private final DataScopeProvider dataScopeProvider;

    public Student getAccessibleStudent(Long studentId) {
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
}
