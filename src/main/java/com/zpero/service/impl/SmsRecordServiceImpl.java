package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpero.common.exception.BusinessException;
import com.zpero.common.result.PageResult;
import com.zpero.dto.sms.SmsRecordQueryDTO;
import com.zpero.entity.SmsRecord;
import com.zpero.entity.Student;
import com.zpero.entity.StudentParent;
import com.zpero.mapper.SmsRecordMapper;
import com.zpero.mapper.StudentMapper;
import com.zpero.mapper.StudentParentMapper;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.SmsRecordService;
import com.zpero.vo.sms.SmsRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SmsRecordServiceImpl implements SmsRecordService {

    private final SmsRecordMapper smsRecordMapper;
    private final StudentMapper studentMapper;
    private final StudentParentMapper studentParentMapper;
    private final DataScopeProvider dataScopeProvider;

    @Override
    public PageResult<SmsRecordVO> queryPage(SmsRecordQueryDTO queryDTO) {
        dataScopeProvider.assertSchool();
        SmsRecordQueryDTO query = queryDTO == null ? new SmsRecordQueryDTO() : queryDTO;

        LambdaQueryWrapper<SmsRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getStudentId() != null,
                        SmsRecord::getStudentId,
                        query.getStudentId())
                .eq(query.getParentId() != null,
                        SmsRecord::getParentId,
                        query.getParentId())
                .eq(StringUtils.hasText(query.getStatus()),
                        SmsRecord::getStatus,
                        query.getStatus())
                .like(StringUtils.hasText(query.getPhone()),
                        SmsRecord::getPhone,
                        query.getPhone())
                .orderByDesc(SmsRecord::getSendTime)
                .orderByDesc(SmsRecord::getId);

        Page<SmsRecord> page = smsRecordMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );

        PageResult<SmsRecordVO> result = new PageResult<>();
        result.setPage(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords()
                .stream()
                .map(this::toVO)
                .toList());
        return result;
    }

    @Override
    public List<SmsRecordVO> listByStudentId(Long studentId) {
        dataScopeProvider.assertSchool();
        if (studentId == null) {
            throw new BusinessException(400, "学生不能为空");
        }
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        return smsRecordMapper.selectList(
                        new LambdaQueryWrapper<SmsRecord>()
                                .eq(SmsRecord::getStudentId, studentId)
                                .orderByDesc(SmsRecord::getSendTime)
                                .orderByDesc(SmsRecord::getId)
                )
                .stream()
                .map(this::toVO)
                .toList();
    }

    private SmsRecordVO toVO(SmsRecord record) {
        SmsRecordVO vo = new SmsRecordVO(record);

        Student student = studentMapper.selectById(record.getStudentId());
        if (student != null) {
            vo.setStudentNo(student.getStudentNo());
            vo.setStudentName(student.getName());
        }

        StudentParent parent = studentParentMapper.selectById(record.getParentId());
        if (parent != null) {
            vo.setParentName(parent.getName());
        }
        return vo;
    }
}
