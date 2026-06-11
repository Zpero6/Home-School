package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.CurrentLoginUser;
import com.zpero.dto.parent.ParentLoginDTO;
import com.zpero.entity.ParentAccount;
import com.zpero.entity.Student;
import com.zpero.mapper.ParentAccountMapper;
import com.zpero.mapper.StudentMapper;
import com.zpero.security.JwtUtil;
import com.zpero.service.JwtTokenService;
import com.zpero.service.ParentAuthService;
import com.zpero.vo.parent.ParentLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParentAuthServiceImpl implements ParentAuthService {

    private static final String PARENT_ROLE = "ROLE_PARENT";

    private final StudentMapper studentMapper;
    private final ParentAccountMapper parentAccountMapper;
    private final JwtUtil jwtUtil;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ParentLoginVO login(ParentLoginDTO loginDTO) {
        if (loginDTO == null
                || !StringUtils.hasText(loginDTO.getIdCard())
                || !StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException(400, "身份证号和密码不能为空");
        }

        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getIdCard, loginDTO.getIdCard())
        );

        if (student == null) {
            throw new BusinessException(401, "身份证号或密码错误");
        }

        ParentAccount parentAccount = parentAccountMapper.selectOne(
                new LambdaQueryWrapper<ParentAccount>()
                        .eq(ParentAccount::getStudentId, student.getId())
        );

        if (parentAccount == null) {
            throw new BusinessException(401, "身份证号或密码错误");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), parentAccount.getPassword())) {
            throw new BusinessException(401, "身份证号或密码错误");
        }

        String tokenId = UUID.randomUUID().toString();
        String token = jwtUtil.generateToken(
                parentAccount.getId(),
                parentAccount.getUsername(),
                PARENT_ROLE,
                tokenId
        );

        CurrentLoginUser currentLoginUser = CurrentLoginUser.builder()
                .userId(parentAccount.getId())
                .userName(parentAccount.getUsername())
                .realName(student.getName())
                .roleCode(PARENT_ROLE)
                .collegeId(student.getCollegeId())
                .parentAccountId(parentAccount.getId())
                .studentId(student.getId())
                .build();
        jwtTokenService.storeCurrentUser(parentAccount.getId(), tokenId, currentLoginUser);

        parentAccount.setLastLoginTime(LocalDateTime.now());
        parentAccountMapper.updateById(parentAccount);

        return ParentLoginVO.builder()
                .parentAccountId(parentAccount.getId())
                .studentId(student.getId())
                .studentName(student.getName())
                .token(token)
                .build();
    }
}
