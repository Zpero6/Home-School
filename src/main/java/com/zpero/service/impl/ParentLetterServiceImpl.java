package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.CurrentLoginUser;
import com.zpero.dto.parent.ParentFeedbackDTO;
import com.zpero.entity.LetterTemplate;
import com.zpero.entity.ParentFeedback;
import com.zpero.entity.Student;
import com.zpero.entity.StudentLetter;
import com.zpero.entity.StudentParent;
import com.zpero.mapper.LetterTemplateMapper;
import com.zpero.mapper.ParentFeedbackMapper;
import com.zpero.mapper.StudentLetterMapper;
import com.zpero.mapper.StudentMapper;
import com.zpero.mapper.StudentParentMapper;
import com.zpero.security.SecurityUtil;
import com.zpero.service.ParentLetterService;
import com.zpero.vo.parent.ParentLetterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ParentLetterServiceImpl implements ParentLetterService {

    private static final String STATUS_SENT = "SENT";
    private static final String STATUS_READ = "READ";

    private final StudentLetterMapper studentLetterMapper;
    private final StudentMapper studentMapper;
    private final StudentParentMapper studentParentMapper;
    private final LetterTemplateMapper letterTemplateMapper;
    private final ParentFeedbackMapper parentFeedbackMapper;

    @Override
    public ParentLetterVO getCurrentParentLetter() {
        Long studentId = getCurrentStudentId();
        StudentLetter letter = getCurrentStudentLetter(studentId);

        if (STATUS_SENT.equals(letter.getStatus())) {
            letter.setStatus(STATUS_READ);
            letter.setReadTime(LocalDateTime.now());
            studentLetterMapper.updateById(letter);
        }

        return toParentLetterVO(letter);
    }

    @Override
    public Long submitFeedback(ParentFeedbackDTO dto) {
        validateFeedbackDTO(dto);

        Long studentId = getCurrentStudentId();
        StudentLetter letter = studentLetterMapper.selectById(dto.getLetterId());
        if (letter == null || !studentId.equals(letter.getStudentId())) {
            throw new BusinessException(404, "信件不存在");
        }

        ParentFeedback feedback = new ParentFeedback();
        feedback.setLetterId(letter.getId());
        feedback.setStudentId(studentId);
        feedback.setParentId(letter.getParentId());
        feedback.setContent(dto.getContent());
        feedback.setImages(dto.getImages());
        feedback.setCreateTime(LocalDateTime.now());

        parentFeedbackMapper.insert(feedback);
        return feedback.getId();
    }

    private Long getCurrentStudentId() {
        CurrentLoginUser currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null || currentUser.getStudentId() == null) {
            throw new BusinessException(403, "当前家长账号未绑定学生");
        }
        return currentUser.getStudentId();
    }

    private StudentLetter getCurrentStudentLetter(Long studentId) {
        StudentLetter letter = studentLetterMapper.selectOne(
                new LambdaQueryWrapper<StudentLetter>()
                        .eq(StudentLetter::getStudentId, studentId)
        );
        if (letter == null) {
            throw new BusinessException(404, "暂无信件");
        }
        return letter;
    }

    private void validateFeedbackDTO(ParentFeedbackDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "反馈信息不能为空");
        }
        if (dto.getLetterId() == null) {
            throw new BusinessException(400, "信件不能为空");
        }
        if (!StringUtils.hasText(dto.getContent())) {
            throw new BusinessException(400, "反馈内容不能为空");
        }
    }

    private ParentLetterVO toParentLetterVO(StudentLetter letter) {
        ParentLetterVO vo = new ParentLetterVO();
        vo.setLetterId(letter.getId());
        vo.setStudentId(letter.getStudentId());
        vo.setParentId(letter.getParentId());
        vo.setTemplateId(letter.getTemplateId());
        vo.setContent(letter.getContent());
        vo.setStatus(letter.getStatus());
        vo.setSendTime(letter.getSendTime());
        vo.setReadTime(letter.getReadTime());

        Student student = studentMapper.selectById(letter.getStudentId());
        if (student != null) {
            vo.setStudentName(student.getName());
        }

        StudentParent parent = studentParentMapper.selectById(letter.getParentId());
        if (parent != null) {
            vo.setParentName(parent.getName());
        }

        if (letter.getTemplateId() != null) {
            LetterTemplate template = letterTemplateMapper.selectById(letter.getTemplateId());
            if (template != null) {
                vo.setBackgroundUrl(template.getBackgroundUrl());
                vo.setLogoUrl(template.getLogoUrl());
            }
        }

        return vo;
    }
}
