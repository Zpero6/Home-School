package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.cadre.StudentCadreDTO;
import com.zpero.entity.StudentCadre;
import com.zpero.mapper.StudentCadreMapper;
import com.zpero.security.dataScope.StudentAccessProvider;
import com.zpero.service.StudentCadreService;
import com.zpero.vo.cadre.StudentCadreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentCadreServiceImpl implements StudentCadreService {

    private final StudentCadreMapper studentCadreMapper;
    private final StudentAccessProvider studentAccessProvider;

    @Override
    public List<StudentCadreVO> listByStudentId(Long studentId) {
        studentAccessProvider.getAccessibleStudent(studentId);

        return studentCadreMapper.selectList(
                        new LambdaQueryWrapper<StudentCadre>()
                                .eq(StudentCadre::getStudentId, studentId)
                                .orderByDesc(StudentCadre::getStartTime)
                                .orderByDesc(StudentCadre::getId)
                )
                .stream()
                .map(StudentCadreVO::new)
                .toList();
    }

    @Override
    public Long createCadre(Long studentId, StudentCadreDTO dto) {
        studentAccessProvider.getAccessibleStudent(studentId);
        validateCadreDTO(dto);

        StudentCadre cadre = new StudentCadre();
        cadre.setStudentId(studentId);
        cadre.setPositionName(dto.getPositionName());
        cadre.setStartTime(dto.getStartTime());
        cadre.setEndTime(dto.getEndTime());

        studentCadreMapper.insert(cadre);
        return cadre.getId();
    }

    @Override
    public void updateCadre(Long id, StudentCadreDTO dto) {
        validateCadreDTO(dto);

        StudentCadre cadre = getAccessibleCadre(id);
        cadre.setPositionName(dto.getPositionName());
        cadre.setStartTime(dto.getStartTime());
        cadre.setEndTime(dto.getEndTime());

        studentCadreMapper.updateById(cadre);
    }

    @Override
    public void deleteCadre(Long id) {
        StudentCadre cadre = getAccessibleCadre(id);
        studentCadreMapper.deleteById(cadre.getId());
    }

    private StudentCadre getAccessibleCadre(Long id) {
        if (id == null) {
            throw new BusinessException(400, "班干部记录不能为空");
        }

        StudentCadre cadre = studentCadreMapper.selectById(id);
        if (cadre == null) {
            throw new BusinessException(404, "班干部记录不存在");
        }

        studentAccessProvider.getAccessibleStudent(cadre.getStudentId());
        return cadre;
    }

    private void validateCadreDTO(StudentCadreDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "班干部信息不能为空");
        }
        if (!StringUtils.hasText(dto.getPositionName())) {
            throw new BusinessException(400, "职务名称不能为空");
        }
        if (dto.getStartTime() != null
                && dto.getEndTime() != null
                && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException(400, "结束时间不能早于开始时间");
        }
    }
}
