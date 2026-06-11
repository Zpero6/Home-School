package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.award.StudentAwardDTO;
import com.zpero.entity.StudentAward;
import com.zpero.mapper.StudentAwardMapper;
import com.zpero.security.dataScope.StudentAccessProvider;
import com.zpero.service.StudentAwardService;
import com.zpero.vo.award.StudentAwardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentAwardServiceImpl implements StudentAwardService {

    private final StudentAwardMapper studentAwardMapper;
    private final StudentAccessProvider studentAccessProvider;

    @Override
    public List<StudentAwardVO> listByStudentId(Long studentId) {
        studentAccessProvider.getAccessibleStudent(studentId);

        return studentAwardMapper.selectList(
                        new LambdaQueryWrapper<StudentAward>()
                                .eq(StudentAward::getStudentId, studentId)
                                .orderByDesc(StudentAward::getAwardTime)
                                .orderByDesc(StudentAward::getId)
                )
                .stream()
                .map(StudentAwardVO::new)
                .toList();
    }

    @Override
    public Long createAward(Long studentId, StudentAwardDTO dto) {
        studentAccessProvider.getAccessibleStudent(studentId);
        validateAwardDTO(dto);

        StudentAward award = new StudentAward();
        award.setStudentId(studentId);
        award.setAwardName(dto.getAwardName());
        award.setAwardLevel(dto.getAwardLevel());
        award.setAwardTime(dto.getAwardTime());

        studentAwardMapper.insert(award);
        return award.getId();
    }

    @Override
    public void updateAward(Long id, StudentAwardDTO dto) {
        validateAwardDTO(dto);

        StudentAward award = getAccessibleAward(id);
        award.setAwardName(dto.getAwardName());
        award.setAwardLevel(dto.getAwardLevel());
        award.setAwardTime(dto.getAwardTime());

        studentAwardMapper.updateById(award);
    }

    @Override
    public void deleteAward(Long id) {
        StudentAward award = getAccessibleAward(id);
        studentAwardMapper.deleteById(award.getId());
    }

    private StudentAward getAccessibleAward(Long id) {
        if (id == null) {
            throw new BusinessException(400, "获奖记录不能为空");
        }

        StudentAward award = studentAwardMapper.selectById(id);
        if (award == null) {
            throw new BusinessException(404, "获奖记录不存在");
        }

        studentAccessProvider.getAccessibleStudent(award.getStudentId());
        return award;
    }

    private void validateAwardDTO(StudentAwardDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "获奖信息不能为空");
        }
        if (!StringUtils.hasText(dto.getAwardName())) {
            throw new BusinessException(400, "奖项名称不能为空");
        }
    }
}
