package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpero.common.exception.BusinessException;
import com.zpero.common.result.PageResult;
import com.zpero.dto.letter.LetterQueryDTO;
import com.zpero.dto.letter.LetterResendDTO;
import com.zpero.dto.letter.LetterSendDTO;
import com.zpero.dto.letter.LetterUpdateDTO;
import com.zpero.entity.ParentAccount;
import com.zpero.entity.SmsRecord;
import com.zpero.entity.Student;
import com.zpero.entity.StudentAward;
import com.zpero.entity.StudentCadre;
import com.zpero.entity.StudentLetter;
import com.zpero.entity.StudentParent;
import com.zpero.entity.StudentScore;
import com.zpero.mapper.ParentAccountMapper;
import com.zpero.mapper.SmsRecordMapper;
import com.zpero.mapper.StudentAwardMapper;
import com.zpero.mapper.StudentCadreMapper;
import com.zpero.mapper.StudentLetterMapper;
import com.zpero.mapper.StudentMapper;
import com.zpero.mapper.StudentParentMapper;
import com.zpero.mapper.StudentScoreMapper;
import com.zpero.security.SecurityUtil;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.security.dataScope.DataScopeContext;
import com.zpero.security.dataScope.StudentAccessProvider;
import com.zpero.service.LetterTemplateService;
import com.zpero.service.StudentLetterService;
import com.zpero.vo.letter.LetterSendResultVO;
import com.zpero.vo.letter.StudentLetterVO;
import com.zpero.vo.template.LetterTemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentLetterServiceImpl implements StudentLetterService {

    private static final String STATUS_UNSEND = "UNSEND";
    private static final String STATUS_SENT = "SENT";
    private static final String SMS_STATUS_SUCCESS = "SUCCESS";
    private static final String SMS_STATUS_FAIL = "FAIL";
    private static final int DEFAULT_PARENT = 1;

    private final StudentLetterMapper studentLetterMapper;
    private final SmsRecordMapper smsRecordMapper;
    private final StudentParentMapper studentParentMapper;
    private final StudentMapper studentMapper;
    private final StudentScoreMapper studentScoreMapper;
    private final StudentAwardMapper studentAwardMapper;
    private final StudentCadreMapper studentCadreMapper;
    private final ParentAccountMapper parentAccountMapper;
    private final LetterTemplateService letterTemplateService;
    private final StudentAccessProvider studentAccessProvider;
    private final DataScopeProvider dataScopeProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<StudentLetterVO> queryPage(LetterQueryDTO queryDTO) {
        LetterQueryDTO query = queryDTO == null ? new LetterQueryDTO() : queryDTO;

        LambdaQueryWrapper<StudentLetter> wrapper = new LambdaQueryWrapper<>();
        applyLetterDataScope(wrapper);
        wrapper.eq(query.getStudentId() != null,
                        StudentLetter::getStudentId,
                        query.getStudentId())
                .eq(StringUtils.hasText(query.getStatus()),
                        StudentLetter::getStatus,
                        query.getStatus())
                .orderByDesc(StudentLetter::getSendTime)
                .orderByDesc(StudentLetter::getId);

        Page<StudentLetter> page = studentLetterMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );

        PageResult<StudentLetterVO> result = new PageResult<>();
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
    public LetterSendResultVO sendLetters(LetterSendDTO dto) {
        validateSendDTO(dto);

        LetterTemplateVO template = letterTemplateService.getById(dto.getTemplateId());
        LetterSendResultVO result = new LetterSendResultVO();
        result.setTotalCount(dto.getStudentIds().size());

        for (Long studentId : dto.getStudentIds()) {
            try {
                Student student = studentAccessProvider.getAccessibleStudent(studentId);
                StudentParent parent = selectRecipientParent(studentId);
                if (parent == null) {
                    result.addFail(student.getId(), student.getName(), "未维护家长联系人");
                    continue;
                }

                ensureParentAccount(student);

                String rawContent = getCustomOrTemplateContent(
                        dto.getCustomContents(),
                        student.getId(),
                        template.getContent()
                );
                String finalContent = renderTemplate(rawContent, student.getId());
                StudentLetter letter = saveLetterSnapshot(student, parent, template.getId(), finalContent);

                if (!StringUtils.hasText(parent.getPhone())) {
                    markLetterUnsend(letter);
                    saveSmsRecord(student, parent, finalContent, SMS_STATUS_FAIL, "家长联系电话为空");
                    result.addFail(student.getId(), student.getName(), "家长联系电话为空");
                    continue;
                }

                markLetterSent(letter);
                saveSmsRecord(student, parent, finalContent, SMS_STATUS_SUCCESS, null);
                result.addSuccess();
            } catch (BusinessException e) {
                result.addFail(studentId, null, e.getMessage());
            }
        }

        return result;
    }

    @Override
    public LetterSendResultVO resendLetters(LetterResendDTO dto) {
        if (dto == null || dto.getLetterIds() == null || dto.getLetterIds().isEmpty()) {
            throw new BusinessException(400, "重发信件不能为空");
        }

        LetterSendResultVO result = new LetterSendResultVO();
        result.setTotalCount(dto.getLetterIds().size());

        for (Long letterId : dto.getLetterIds()) {
            StudentLetter letter = getAccessibleLetter(letterId);
            Student student = studentAccessProvider.getAccessibleStudent(letter.getStudentId());
            StudentParent parent = studentParentMapper.selectById(letter.getParentId());
            if (parent == null) {
                result.addFail(student.getId(), student.getName(), "家长联系人不存在");
                continue;
            }
            if (!StringUtils.hasText(parent.getPhone())) {
                markLetterUnsend(letter);
                saveSmsRecord(student, parent, letter.getContent(), SMS_STATUS_FAIL, "家长联系电话为空");
                result.addFail(student.getId(), student.getName(), "家长联系电话为空");
                continue;
            }

            markLetterSent(letter);
            saveSmsRecord(student, parent, letter.getContent(), SMS_STATUS_SUCCESS, null);
            result.addSuccess();
        }

        return result;
    }

    @Override
    public void updateLetter(Long id, LetterUpdateDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getContent())) {
            throw new BusinessException(400, "信件内容不能为空");
        }

        StudentLetter letter = getAccessibleLetter(id);
        letter.setContent(dto.getContent());
        studentLetterMapper.updateById(letter);
    }

    private void validateSendDTO(LetterSendDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "发送信息不能为空");
        }
        if (dto.getTemplateId() == null) {
            throw new BusinessException(400, "模板不能为空");
        }
        if (dto.getStudentIds() == null || dto.getStudentIds().isEmpty()) {
            throw new BusinessException(400, "发送学生不能为空");
        }
    }

    private StudentLetter getAccessibleLetter(Long id) {
        if (id == null) {
            throw new BusinessException(400, "信件不能为空");
        }

        StudentLetter letter = studentLetterMapper.selectById(id);
        if (letter == null) {
            throw new BusinessException(404, "信件不存在");
        }

        studentAccessProvider.getAccessibleStudent(letter.getStudentId());
        return letter;
    }

    private void applyLetterDataScope(LambdaQueryWrapper<StudentLetter> wrapper) {
        DataScopeContext context = dataScopeProvider.current();
        if (context.isAll()) {
            return;
        }
        if (context.hasCounselorScope()) {
            wrapper.eq(StudentLetter::getCounselorId, context.getCounselorId());
            return;
        }
        if (context.hasCollegeScope()) {
            List<Long> studentIds = studentMapper.selectList(
                            new LambdaQueryWrapper<Student>()
                                    .eq(Student::getCollegeId, context.getCollegeId())
                    )
                    .stream()
                    .map(Student::getId)
                    .toList();
            if (studentIds.isEmpty()) {
                wrapper.eq(StudentLetter::getId, -1L);
            } else {
                wrapper.in(StudentLetter::getStudentId, studentIds);
            }
            return;
        }
        throw new BusinessException(403, "用户没有数据权限");
    }

    private StudentParent selectRecipientParent(Long studentId) {
        return studentParentMapper.selectList(
                        new LambdaQueryWrapper<StudentParent>()
                                .eq(StudentParent::getStudentId, studentId)
                )
                .stream()
                .min(Comparator
                        .comparingInt(this::parentPriority)
                        .thenComparing(StudentParent::getId))
                .orElse(null);
    }

    private int parentPriority(StudentParent parent) {
        if (parent.getIsDefault() != null && parent.getIsDefault() == DEFAULT_PARENT) {
            return 0;
        }
        if ("FATHER".equals(parent.getRelation())) {
            return 1;
        }
        if ("MOTHER".equals(parent.getRelation())) {
            return 2;
        }
        return 3;
    }

    private void ensureParentAccount(Student student) {
        ParentAccount account = parentAccountMapper.selectOne(
                new LambdaQueryWrapper<ParentAccount>()
                        .eq(ParentAccount::getStudentId, student.getId())
        );
        if (account != null) {
            return;
        }

        ParentAccount parentAccount = new ParentAccount();
        parentAccount.setStudentId(student.getId());
        parentAccount.setUsername(student.getIdCard());
        parentAccount.setPassword(passwordEncoder.encode(defaultParentPassword(student.getIdCard())));
        parentAccountMapper.insert(parentAccount);
    }

    private String defaultParentPassword(String idCard) {
        if (!StringUtils.hasText(idCard) || idCard.length() <= 6) {
            return idCard;
        }
        return idCard.substring(idCard.length() - 6);
    }

    private String getCustomOrTemplateContent(Map<String, String> customContents,
                                              Long studentId,
                                              String templateContent) {
        if (customContents == null || customContents.isEmpty()) {
            return templateContent;
        }
        String customContent = customContents.get(String.valueOf(studentId));
        return StringUtils.hasText(customContent) ? customContent : templateContent;
    }

    private StudentLetter saveLetterSnapshot(Student student,
                                             StudentParent parent,
                                             Long templateId,
                                             String content) {
        StudentLetter letter = studentLetterMapper.selectOne(
                new LambdaQueryWrapper<StudentLetter>()
                        .eq(StudentLetter::getStudentId, student.getId())
        );
        if (letter == null) {
            letter = new StudentLetter();
            letter.setStudentId(student.getId());
        }

        letter.setParentId(parent.getId());
        letter.setTemplateId(templateId);
        letter.setContent(content);
        letter.setCounselorId(SecurityUtil.getCurrentUserId());
        letter.setReadTime(null);

        if (letter.getId() == null) {
            letter.setStatus(STATUS_UNSEND);
            studentLetterMapper.insert(letter);
        } else {
            studentLetterMapper.updateById(letter);
        }
        return letter;
    }

    private void markLetterSent(StudentLetter letter) {
        letter.setStatus(STATUS_SENT);
        letter.setSendTime(LocalDateTime.now());
        studentLetterMapper.updateById(letter);
    }

    private void markLetterUnsend(StudentLetter letter) {
        letter.setStatus(STATUS_UNSEND);
        letter.setSendTime(null);
        studentLetterMapper.updateById(letter);
    }

    private void saveSmsRecord(Student student,
                               StudentParent parent,
                               String letterContent,
                               String status,
                               String failReason) {
        SmsRecord smsRecord = new SmsRecord();
        smsRecord.setStudentId(student.getId());
        smsRecord.setParentId(parent.getId());
        smsRecord.setPhone(StringUtils.hasText(parent.getPhone()) ? parent.getPhone() : "");
        smsRecord.setContent(buildSmsContent(student, parent));
        smsRecord.setStatus(status);
        smsRecord.setFailReason(failReason);
        smsRecord.setSendTime(LocalDateTime.now());
        smsRecord.setCreateTime(LocalDateTime.now());

        smsRecordMapper.insert(smsRecord);
    }

    private String buildSmsContent(Student student, StudentParent parent) {
        String parentName = StringUtils.hasText(parent.getName()) ? parent.getName() : "家长";
        return parentName + "您好：请查看学生" + student.getName()
                + "在校情况，账号为学生身份证号，初始密码为身份证后六位。";
    }

    private String renderTemplate(String content, Long studentId) {
        return content
                .replace("${score}", buildScoreHtml(studentId))
                .replace("${award}", buildAwardHtml(studentId))
                .replace("${cadre}", buildCadreHtml(studentId));
    }

    private String buildScoreHtml(Long studentId) {
        List<StudentScore> scores = studentScoreMapper.selectList(
                new LambdaQueryWrapper<StudentScore>()
                        .eq(StudentScore::getStudentId, studentId)
                        .orderByDesc(StudentScore::getAcademicYear)
                        .orderByDesc(StudentScore::getSemester)
                        .orderByAsc(StudentScore::getCourseName)
        );
        if (scores.isEmpty()) {
            return "<p>暂无成绩</p>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<table><thead><tr><th>学年</th><th>学期</th><th>课程</th><th>成绩</th></tr></thead><tbody>");
        for (StudentScore score : scores) {
            html.append("<tr>")
                    .append("<td>").append(escapeHtml(score.getAcademicYear())).append("</td>")
                    .append("<td>").append(score.getSemester()).append("</td>")
                    .append("<td>").append(escapeHtml(score.getCourseName())).append("</td>")
                    .append("<td>").append(score.getScore() == null ? "" : score.getScore()).append("</td>")
                    .append("</tr>");
        }
        html.append("</tbody></table>");
        return html.toString();
    }

    private String buildAwardHtml(Long studentId) {
        List<StudentAward> awards = studentAwardMapper.selectList(
                new LambdaQueryWrapper<StudentAward>()
                        .eq(StudentAward::getStudentId, studentId)
                        .orderByDesc(StudentAward::getAwardTime)
                        .orderByDesc(StudentAward::getId)
        );
        if (awards.isEmpty()) {
            return "<p>暂无获奖记录</p>";
        }

        StringBuilder html = new StringBuilder("<ul>");
        for (StudentAward award : awards) {
            html.append("<li>")
                    .append(escapeHtml(award.getAwardName()));
            if (StringUtils.hasText(award.getAwardLevel())) {
                html.append("（").append(escapeHtml(award.getAwardLevel())).append("）");
            }
            html.append("</li>");
        }
        html.append("</ul>");
        return html.toString();
    }

    private String buildCadreHtml(Long studentId) {
        List<StudentCadre> cadres = studentCadreMapper.selectList(
                new LambdaQueryWrapper<StudentCadre>()
                        .eq(StudentCadre::getStudentId, studentId)
                        .orderByDesc(StudentCadre::getStartTime)
                        .orderByDesc(StudentCadre::getId)
        );
        if (cadres.isEmpty()) {
            return "<p>暂无班干部记录</p>";
        }

        StringBuilder html = new StringBuilder("<ul>");
        for (StudentCadre cadre : cadres) {
            html.append("<li>")
                    .append(escapeHtml(cadre.getPositionName()))
                    .append("</li>");
        }
        html.append("</ul>");
        return html.toString();
    }

    private StudentLetterVO toVO(StudentLetter letter) {
        StudentLetterVO vo = new StudentLetterVO(letter);
        Student student = studentMapper.selectById(letter.getStudentId());
        if (student != null) {
            vo.setStudentName(student.getName());
        }
        StudentParent parent = studentParentMapper.selectById(letter.getParentId());
        if (parent != null) {
            vo.setParentName(parent.getName());
        }
        return vo;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
