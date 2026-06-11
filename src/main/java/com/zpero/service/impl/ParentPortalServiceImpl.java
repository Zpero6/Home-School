package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.CurrentLoginUser;
import com.zpero.dto.link.ExternalLinkQueryDTO;
import com.zpero.dto.parent.ParentPasswordDTO;
import com.zpero.entity.ParentAccount;
import com.zpero.entity.StudentScore;
import com.zpero.mapper.ParentAccountMapper;
import com.zpero.mapper.StudentScoreMapper;
import com.zpero.security.SecurityUtil;
import com.zpero.service.ExternalLinkService;
import com.zpero.service.ParentPortalService;
import com.zpero.vo.link.ExternalLinkVO;
import com.zpero.vo.score.StudentScoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentPortalServiceImpl implements ParentPortalService {

    private final StudentScoreMapper studentScoreMapper;
    private final ParentAccountMapper parentAccountMapper;
    private final ExternalLinkService externalLinkService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<StudentScoreVO> getCurrentStudentScores() {
        Long studentId = getCurrentStudentId();
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
    public void updatePassword(ParentPasswordDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "密码信息不能为空");
        }
        if (!StringUtils.hasText(dto.getOldPassword())) {
            throw new BusinessException(400, "原密码不能为空");
        }
        if (!StringUtils.hasText(dto.getNewPassword())) {
            throw new BusinessException(400, "新密码不能为空");
        }
        if (dto.getNewPassword().length() < 6) {
            throw new BusinessException(400, "新密码长度不能少于6位");
        }

        Long parentAccountId = getCurrentParentAccountId();
        ParentAccount parentAccount = parentAccountMapper.selectById(parentAccountId);
        if (parentAccount == null) {
            throw new BusinessException(404, "家长账号不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), parentAccount.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }

        parentAccount.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        parentAccountMapper.updateById(parentAccount);
    }

    @Override
    public List<ExternalLinkVO> listExternalLinks() {
        return externalLinkService.list(new ExternalLinkQueryDTO());
    }

    private Long getCurrentStudentId() {
        CurrentLoginUser currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null || currentUser.getStudentId() == null) {
            throw new BusinessException(403, "当前家长账号未绑定学生");
        }
        return currentUser.getStudentId();
    }

    private Long getCurrentParentAccountId() {
        CurrentLoginUser currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null || currentUser.getParentAccountId() == null) {
            throw new BusinessException(403, "当前家长账号不存在");
        }
        return currentUser.getParentAccountId();
    }
}
