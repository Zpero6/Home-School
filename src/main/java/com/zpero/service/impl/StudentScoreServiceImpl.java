package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.score.StudentScoreDTO;
import com.zpero.entity.StudentScore;
import com.zpero.mapper.StudentScoreMapper;
import com.zpero.security.dataScope.StudentAccessProvider;
import com.zpero.service.StudentScoreService;
import com.zpero.vo.score.StudentScoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentScoreServiceImpl implements StudentScoreService {

    private final StudentScoreMapper studentScoreMapper;
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
