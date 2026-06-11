package com.zpero.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpero.common.exception.BusinessException;
import com.zpero.common.result.PageResult;
import com.zpero.dto.parent.ParentAccountDTO;
import com.zpero.dto.parent.ParentAccountQueryDTO;
import com.zpero.entity.ParentAccount;
import com.zpero.entity.Student;
import com.zpero.mapper.ParentAccountMapper;
import com.zpero.mapper.StudentMapper;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.ParentAccountService;
import com.zpero.vo.parent.ParentAccountVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentAccountServiceImpl implements ParentAccountService {

    private final ParentAccountMapper parentAccountMapper;
    private final StudentMapper studentMapper;
    private final DataScopeProvider dataScopeProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<ParentAccountVO> queryPage(ParentAccountQueryDTO queryDTO) {
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        dataScopeProvider.applyCollegeAndCounselorScope(
                studentWrapper,
                Student::getCollegeId,
                Student::getCounselorId
        );

        List<Long> studentIds = studentMapper.selectList(studentWrapper)
                .stream()
                .map(Student::getId).toList();

        if (studentIds.isEmpty()) {
            PageResult<ParentAccountVO> emptyResult = new PageResult<>();
            emptyResult.setPage(queryDTO.getPage());
            emptyResult.setSize(queryDTO.getSize());
            emptyResult.setTotal(0L);
            emptyResult.setRecords(List.of());
            return emptyResult;
        }
        LambdaQueryWrapper<ParentAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ParentAccount::getStudentId, studentIds);
        wrapper.eq(queryDTO.getStudentId() != null,
                ParentAccount::getStudentId, queryDTO.getStudentId());
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()),
                ParentAccount::getUsername,
                queryDTO.getUsername());
        Page<ParentAccount> result = parentAccountMapper.selectPage(
                new Page<>(queryDTO.getPage(), queryDTO.getSize()),
                wrapper
        );

        PageResult<ParentAccountVO> pageResult = new PageResult<>();
        pageResult.setPage(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(result.getRecords()
                .stream()
                .map(ParentAccountVO::new)
                .toList());
        return pageResult;

    }

    @Override
    public ParentAccountVO getById(Long id) {
        return new ParentAccountVO(getAccessibleAccount(id));
    }

    @Override
    public Long createParentAccount(ParentAccountDTO parentAccountDTO) {
        if (parentAccountDTO == null) {
            throw new BusinessException(400, "家长信息不能为空");
        }
        Student student = getAccessibleStudent(parentAccountDTO.getStudentId());
        dataScopeProvider.assertCollegeAndCounselorAccess(
                student.getCollegeId(), student.getCounselorId()
        );
        if (!StringUtils.hasText(parentAccountDTO.getUsername())) {
            throw new BusinessException(400, "账号不能为空");
        }
        if (!StringUtils.hasText(parentAccountDTO.getPassword())) {
            throw new BusinessException(400, "密码不能为空");
        }

        Long count = parentAccountMapper.selectCount(
                new LambdaQueryWrapper<ParentAccount>()
                        .eq(ParentAccount::getStudentId, parentAccountDTO.getStudentId())
        );
        if (count > 0) {
            throw new BusinessException(400, "已存在家长账号");
        }
        Long usernameCount = parentAccountMapper.selectCount(
                new LambdaQueryWrapper<ParentAccount>()
                        .eq(ParentAccount::getUsername, parentAccountDTO.getUsername())
        );

        if (usernameCount > 0) {
            throw new BusinessException(400, "账号已存在");
        }

        ParentAccount parentAccount = new ParentAccount();
        parentAccount.setStudentId(parentAccountDTO.getStudentId());
        parentAccount.setUsername(parentAccountDTO.getUsername());
        parentAccount.setPassword(passwordEncoder.encode(parentAccountDTO.getPassword()));

        parentAccountMapper.insert(parentAccount);
        return parentAccount.getId();

    }


    @Override
    public void updateParentAccount(Long id, ParentAccountDTO parentAccountDTO) {

        if (parentAccountDTO == null) {
            throw new BusinessException(400, "家长账号信息不能为空");
        }
        ParentAccount account = getAccessibleAccount(id);

        if (parentAccountDTO.getStudentId() != null
                && !account.getStudentId().equals(parentAccountDTO.getStudentId())) {
            throw new BusinessException(400, "家长账号所绑定的学生不允许修改");
        }
        if (StringUtils.hasText(parentAccountDTO.getUsername())
                && !account.getUsername().equals(parentAccountDTO.getUsername())) {

            Long usernameCount = parentAccountMapper.selectCount(
                    new LambdaQueryWrapper<ParentAccount>()
                            .eq(ParentAccount::getUsername, parentAccountDTO.getUsername())
            );

            if (usernameCount > 0) {
                throw new BusinessException(400, "账号已经存在");
            }
            account.setUsername(parentAccountDTO.getUsername());
        }
        if (StringUtils.hasText(parentAccountDTO.getPassword())) {
            account.setPassword(passwordEncoder.encode(parentAccountDTO.getPassword()));
        }
        parentAccountMapper.updateById(account);

    }

    @Override
    public void deleteParentAccount(Long id) {
        ParentAccount account = getAccessibleAccount(id);
        Student student = getAccessibleStudent(account.getStudentId());

        dataScopeProvider.assertCanManageCollege(student.getCollegeId());
        parentAccountMapper.deleteById(id);
    }

    private ParentAccount getAccessibleAccount(Long id) {
        ParentAccount account = parentAccountMapper.selectById(id);
        if (account == null) {
            throw new BusinessException(404, "家长账号不存在");
        }

        getAccessibleStudent(account.getStudentId());

        return account;
    }

    private Student getAccessibleStudent(Long studentId) {
        if (studentId == null) {
            throw new BusinessException(400, "学生不能为空");
        }

        Student student = studentMapper.selectById(studentId);

        if (student == null) {
            throw new BusinessException(400, "学生不存在");
        }

        dataScopeProvider.assertCollegeAndCounselorAccess(
                student.getCollegeId(),
                student.getCounselorId()
        );

        return student;
    }
}
