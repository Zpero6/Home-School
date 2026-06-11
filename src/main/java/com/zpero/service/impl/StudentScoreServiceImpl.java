package com.zpero.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.score.StudentScoreImportDTO;
import com.zpero.dto.score.StudentScoreDTO;
import com.zpero.entity.Student;
import com.zpero.entity.StudentScore;
import com.zpero.mapper.StudentScoreMapper;
import com.zpero.mapper.StudentMapper;
import com.zpero.security.dataScope.StudentAccessProvider;
import com.zpero.service.StudentScoreService;
import com.zpero.vo.score.ScoreImportResultVO;
import com.zpero.vo.score.StudentScoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentScoreServiceImpl implements StudentScoreService {

    private final StudentScoreMapper studentScoreMapper;
    private final StudentMapper studentMapper;
    private final StudentAccessProvider studentAccessProvider;

    @Override
    public List<StudentScoreVO> listByStudentId(Long studentId) {
        studentAccessProvider.getAccessibleStudent(studentId);

        return studentScoreMapper.selectList(
                        new LambdaQueryWrapper<StudentScore>()
                                .eq(StudentScore::getStudentId, studentId)
                                .orderByDesc(StudentScore::getAcademicYear)
                                .orderByDesc(StudentScore::getSemester)
                                .orderByAsc(StudentScore::getCourseName)
                )
                .stream()
                .map(StudentScoreVO::new)
                .toList();
    }

    @Override
    public Long createScore(Long studentId, StudentScoreDTO dto) {
        studentAccessProvider.getAccessibleStudent(studentId);
        validateScoreDTO(dto);

        StudentScore studentScore = new StudentScore();
        studentScore.setStudentId(studentId);
        studentScore.setCourseName(dto.getCourseName());
        studentScore.setScore(dto.getScore());
        studentScore.setAcademicYear(dto.getAcademicYear());
        studentScore.setSemester(dto.getSemester());

        studentScoreMapper.insert(studentScore);
        return studentScore.getId();
    }

    @Override
    public void updateScore(Long id, StudentScoreDTO dto) {
        validateScoreDTO(dto);

        StudentScore studentScore = getAccessibleScore(id);
        studentScore.setCourseName(dto.getCourseName());
        studentScore.setScore(dto.getScore());
        studentScore.setAcademicYear(dto.getAcademicYear());
        studentScore.setSemester(dto.getSemester());

        studentScoreMapper.updateById(studentScore);
    }

    @Override
    public void deleteScore(Long id) {
        StudentScore studentScore = getAccessibleScore(id);
        studentScoreMapper.deleteById(studentScore.getId());
    }

    @Override
    public ScoreImportResultVO importScores(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "导入文件不能为空");
        }

        List<StudentScoreImportDTO> rows;
        try {
            List<Object> rawRows = EasyExcel.read(file.getInputStream())
                    .head(StudentScoreImportDTO.class)
                    .sheet()
                    .doReadSync();
            rows = rawRows.stream()
                    .map(StudentScoreImportDTO.class::cast)
                    .toList();
        } catch (Exception e) {
            throw new BusinessException(400, "读取成绩导入文件失败");
        }

        ScoreImportResultVO result = new ScoreImportResultVO();
        result.setTotalCount(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            StudentScoreImportDTO row = rows.get(i);
            int excelRowNum = i + 2;
            try {
                importOneScore(row);
                result.addSuccess();
            } catch (BusinessException e) {
                result.addFail(excelRowNum,
                        row == null ? null : row.getStudentNo(),
                        e.getMessage());
            }
        }
        return result;
    }

    private void importOneScore(StudentScoreImportDTO row) {
        if (row == null) {
            throw new BusinessException(400, "成绩信息不能为空");
        }
        if (!StringUtils.hasText(row.getStudentNo())) {
            throw new BusinessException(400, "学号不能为空");
        }

        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getStudentNo, row.getStudentNo())
        );
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        StudentScoreDTO dto = new StudentScoreDTO();
        dto.setCourseName(row.getCourseName());
        dto.setScore(row.getScore());
        dto.setAcademicYear(row.getAcademicYear());
        dto.setSemester(row.getSemester());
        createScore(student.getId(), dto);
    }

    private StudentScore getAccessibleScore(Long id) {
        if (id == null) {
            throw new BusinessException(400, "成绩不能为空");
        }

        StudentScore studentScore = studentScoreMapper.selectById(id);
        if (studentScore == null) {
            throw new BusinessException(404, "成绩不存在");
        }

        studentAccessProvider.getAccessibleStudent(studentScore.getStudentId());
        return studentScore;
    }

    private void validateScoreDTO(StudentScoreDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "成绩信息不能为空");
        }
        if (!StringUtils.hasText(dto.getCourseName())) {
            throw new BusinessException(400, "课程名称不能为空");
        }
        if (!StringUtils.hasText(dto.getAcademicYear())) {
            throw new BusinessException(400, "学年不能为空");
        }
        if (dto.getSemester() == null || (dto.getSemester() != 1 && dto.getSemester() != 2)) {
            throw new BusinessException(400, "学期只能是1或2");
        }
        if (dto.getScore() != null
                && (dto.getScore().compareTo(BigDecimal.ZERO) < 0
                || dto.getScore().compareTo(new BigDecimal("100")) > 0)) {
            throw new BusinessException(400, "分数范围必须在0到100之间");
        }
    }
}
