package com.zpero.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.common.result.PageResult;
import com.zpero.dto.statistics.StatisticsQueryDTO;
import com.zpero.entity.ClassInfo;
import com.zpero.entity.College;
import com.zpero.entity.ParentFeedback;
import com.zpero.entity.SmsRecord;
import com.zpero.entity.Student;
import com.zpero.entity.StudentLetter;
import com.zpero.entity.SysUser;
import com.zpero.mapper.ClassInfoMapper;
import com.zpero.mapper.CollegeMapper;
import com.zpero.mapper.ParentFeedbackMapper;
import com.zpero.mapper.SmsRecordMapper;
import com.zpero.mapper.StudentLetterMapper;
import com.zpero.mapper.StudentMapper;
import com.zpero.mapper.SysUserMapper;
import com.zpero.security.SecurityUtil;
import com.zpero.security.dataScope.DataScopeContext;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.StatisticsService;
import com.zpero.vo.statistics.FeedbackStatisticsVO;
import com.zpero.vo.statistics.ReadStatisticsVO;
import com.zpero.vo.statistics.SendStatisticsItemVO;
import com.zpero.vo.statistics.SendStatisticsVO;
import com.zpero.vo.statistics.StatisticsStudentVO;
import com.zpero.vo.statistics.export.FeedbackStatisticsExportVO;
import com.zpero.vo.statistics.export.ReadStatisticsExportVO;
import com.zpero.vo.statistics.export.SendStatisticsExportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final String STUDENT_STATUS_IN_SCHOOL = "在校";
    private static final String SMS_STATUS_SUCCESS = "SUCCESS";
    private static final String LETTER_STATUS_READ = "READ";
    private static final String SEND_STATUS_SENT = "SENT";
    private static final String SEND_STATUS_UNSENT = "UNSENT";
    private static final String READ_STATUS_UNREAD = "UNREAD";
    private static final String ROLE_SCHOOL = "ROLE_SCHOOL";
    private static final String ROLE_COLLEGE = "ROLE_COLLEGE";

    private final StudentMapper studentMapper;
    private final SmsRecordMapper smsRecordMapper;
    private final StudentLetterMapper studentLetterMapper;
    private final ParentFeedbackMapper parentFeedbackMapper;
    private final CollegeMapper collegeMapper;
    private final SysUserMapper sysUserMapper;
    private final ClassInfoMapper classInfoMapper;
    private final DataScopeProvider dataScopeProvider;

    @Override
    public SendStatisticsVO getSchoolSendStatistics(StatisticsQueryDTO queryDTO) {
        dataScopeProvider.assertSchool();
        return buildSendStatistics(
                getScopedStudents(queryDTO),
                Student::getCollegeId,
                this::getCollegeName,
                "COLLEGE"
        );
    }

    @Override
    public SendStatisticsVO getCollegeSendStatistics(StatisticsQueryDTO queryDTO) {
        return buildSendStatistics(
                getScopedStudents(queryDTO),
                Student::getCounselorId,
                this::getCounselorName,
                "COUNSELOR"
        );
    }

    @Override
    public SendStatisticsVO getClassSendStatistics(StatisticsQueryDTO queryDTO) {
        return buildSendStatistics(
                getScopedStudents(queryDTO),
                Student::getClassId,
                this::getClassName,
                "CLASS"
        );
    }

    @Override
    public ReadStatisticsVO getReadStatistics(StatisticsQueryDTO queryDTO) {
        List<Long> studentIds = getScopedStudentIds(queryDTO);
        Long shouldSendCount = (long) studentIds.size();
        Long actualSendCount = countActualSentStudents(studentIds);
        Long readCount = countReadStudents(studentIds);

        ReadStatisticsVO vo = new ReadStatisticsVO();
        vo.setShouldSendCount(shouldSendCount);
        vo.setActualSendCount(actualSendCount);
        vo.setReadCount(readCount);
        vo.setUnreadCount(Math.max(actualSendCount - readCount, 0L));
        vo.setReadRate(rate(readCount, actualSendCount));
        return vo;
    }

    @Override
    public FeedbackStatisticsVO getFeedbackStatistics(StatisticsQueryDTO queryDTO) {
        List<Long> studentIds = getScopedStudentIds(queryDTO);
        Long shouldSendCount = (long) studentIds.size();
        Long actualSendCount = countActualSentStudents(studentIds);
        Long feedbackStudentCount = countFeedbackStudents(studentIds);
        Long totalFeedbackCount = countTotalFeedback(studentIds);

        FeedbackStatisticsVO vo = new FeedbackStatisticsVO();
        vo.setShouldSendCount(shouldSendCount);
        vo.setActualSendCount(actualSendCount);
        vo.setFeedbackStudentCount(feedbackStudentCount);
        vo.setNoFeedbackCount(Math.max(actualSendCount - feedbackStudentCount, 0L));
        vo.setTotalFeedbackCount(totalFeedbackCount);
        vo.setFeedbackRate(rate(feedbackStudentCount, actualSendCount));
        return vo;
    }

    @Override
    public PageResult<StatisticsStudentVO> getUnsentStudents(StatisticsQueryDTO queryDTO) {
        List<Student> students = getScopedStudents(queryDTO);
        List<Long> studentIds = students.stream()
                .map(Student::getId)
                .toList();
        Set<Long> actualSentStudentIds = getActualSentStudentIds(studentIds);

        List<StatisticsStudentVO> records = students.stream()
                .filter(student -> !actualSentStudentIds.contains(student.getId()))
                .map(student -> toStatisticsStudentVO(
                        student,
                        null,
                        null,
                        SEND_STATUS_UNSENT,
                        null
                ))
                .toList();

        return buildStudentPage(records, queryDTO);
    }

    @Override
    public PageResult<StatisticsStudentVO> getUnreadStudents(StatisticsQueryDTO queryDTO) {
        List<Student> students = getScopedStudents(queryDTO);
        List<Long> studentIds = students.stream()
                .map(Student::getId)
                .toList();
        Map<Long, SmsRecord> latestSuccessSmsMap = getLatestSuccessSmsMap(studentIds);
        Set<Long> readStudentIds = getReadStudentIds(studentIds);
        Map<Long, StudentLetter> latestLetterMap = getLatestLetterMap(studentIds);

        List<StatisticsStudentVO> records = students.stream()
                .filter(student -> latestSuccessSmsMap.containsKey(student.getId()))
                .filter(student -> !readStudentIds.contains(student.getId()))
                .map(student -> toStatisticsStudentVO(
                        student,
                        latestLetterMap.get(student.getId()),
                        latestSuccessSmsMap.get(student.getId()),
                        SEND_STATUS_SENT,
                        READ_STATUS_UNREAD
                ))
                .toList();

        return buildStudentPage(records, queryDTO);
    }

    @Override
    public void exportSendStatistics(StatisticsQueryDTO queryDTO,
                                     HttpServletResponse response) throws IOException {
        SendStatisticsVO statistics = getSendStatisticsForExport(queryDTO);
        List<SendStatisticsExportVO> rows = toSendStatisticsExportRows(statistics);
        writeExcel(response, "send-statistics.xlsx", "发送统计",
                SendStatisticsExportVO.class, rows);
    }

    @Override
    public void exportReadStatistics(StatisticsQueryDTO queryDTO,
                                     HttpServletResponse response) throws IOException {
        ReadStatisticsVO statistics = getReadStatistics(queryDTO);
        ReadStatisticsExportVO row = new ReadStatisticsExportVO();
        row.setShouldSendCount(statistics.getShouldSendCount());
        row.setActualSendCount(statistics.getActualSendCount());
        row.setReadCount(statistics.getReadCount());
        row.setUnreadCount(statistics.getUnreadCount());
        row.setReadRate(statistics.getReadRate());

        writeExcel(response, "read-statistics.xlsx", "查阅统计",
                ReadStatisticsExportVO.class, List.of(row));
    }

    @Override
    public void exportFeedbackStatistics(StatisticsQueryDTO queryDTO,
                                         HttpServletResponse response) throws IOException {
        FeedbackStatisticsVO statistics = getFeedbackStatistics(queryDTO);
        FeedbackStatisticsExportVO row = new FeedbackStatisticsExportVO();
        row.setShouldSendCount(statistics.getShouldSendCount());
        row.setActualSendCount(statistics.getActualSendCount());
        row.setFeedbackStudentCount(statistics.getFeedbackStudentCount());
        row.setNoFeedbackCount(statistics.getNoFeedbackCount());
        row.setTotalFeedbackCount(statistics.getTotalFeedbackCount());
        row.setFeedbackRate(statistics.getFeedbackRate());

        writeExcel(response, "feedback-statistics.xlsx", "反馈统计",
                FeedbackStatisticsExportVO.class, List.of(row));
    }

    private List<Student> getScopedStudents(StatisticsQueryDTO queryDTO) {
        StatisticsQueryDTO query = queryDTO == null ? new StatisticsQueryDTO() : queryDTO;

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStatus, STUDENT_STATUS_IN_SCHOOL);
        applyStudentDataScope(wrapper);
        wrapper.eq(query.getCollegeId() != null, Student::getCollegeId, query.getCollegeId())
                .eq(query.getCounselorId() != null, Student::getCounselorId, query.getCounselorId())
                .eq(query.getClassId() != null, Student::getClassId, query.getClassId());
        wrapper.orderByAsc(Student::getCollegeId)
                .orderByAsc(Student::getClassId)
                .orderByAsc(Student::getStudentNo)
                .orderByAsc(Student::getId);

        return studentMapper.selectList(wrapper);
    }

    private List<Long> getScopedStudentIds(StatisticsQueryDTO queryDTO) {
        return getScopedStudents(queryDTO)
                .stream()
                .map(Student::getId)
                .toList();
    }

    private void applyStudentDataScope(LambdaQueryWrapper<Student> wrapper) {
        DataScopeContext context = dataScopeProvider.current();
        if (context.isAll()) {
            return;
        }
        if (context.hasCounselorScope()) {
            wrapper.eq(Student::getCounselorId, context.getCounselorId());
            return;
        }
        if (context.hasCollegeScope()) {
            wrapper.eq(Student::getCollegeId, context.getCollegeId());
            return;
        }
        throw new BusinessException(403, "用户没有数据权限");
    }

    private Long countActualSentStudents(List<Long> studentIds) {
        return (long) getActualSentStudentIds(studentIds).size();
    }

    private Set<Long> getActualSentStudentIds(List<Long> studentIds) {
        return getSuccessSmsRecords(studentIds)
                .stream()
                .map(SmsRecord::getStudentId)
                .collect(Collectors.toSet());
    }

    private Long countReadStudents(List<Long> studentIds) {
        return (long) getReadStudentIds(studentIds).size();
    }

    private Long countFeedbackStudents(List<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return 0L;
        }

        return parentFeedbackMapper.selectList(
                        new LambdaQueryWrapper<ParentFeedback>()
                                .in(ParentFeedback::getStudentId, studentIds)
                )
                .stream()
                .map(ParentFeedback::getStudentId)
                .distinct()
                .count();
    }

    private Long countTotalFeedback(List<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return 0L;
        }

        return parentFeedbackMapper.selectCount(
                new LambdaQueryWrapper<ParentFeedback>()
                        .in(ParentFeedback::getStudentId, studentIds)
        );
    }

    private BigDecimal rate(Long numerator, Long denominator) {
        if (denominator == null || denominator == 0L) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private List<SmsRecord> getSuccessSmsRecords(List<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return List.of();
        }

        return smsRecordMapper.selectList(
                new LambdaQueryWrapper<SmsRecord>()
                        .in(SmsRecord::getStudentId, studentIds)
                        .eq(SmsRecord::getStatus, SMS_STATUS_SUCCESS)
                        .orderByDesc(SmsRecord::getSendTime)
                        .orderByDesc(SmsRecord::getId)
        );
    }

    private Map<Long, SmsRecord> getLatestSuccessSmsMap(List<Long> studentIds) {
        return getSuccessSmsRecords(studentIds)
                .stream()
                .collect(Collectors.toMap(
                        SmsRecord::getStudentId,
                        Function.identity(),
                        (oldValue, newValue) -> oldValue
                ));
    }

    private Set<Long> getReadStudentIds(List<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return Set.of();
        }

        return studentLetterMapper.selectList(
                        new LambdaQueryWrapper<StudentLetter>()
                                .in(StudentLetter::getStudentId, studentIds)
                                .eq(StudentLetter::getStatus, LETTER_STATUS_READ)
                )
                .stream()
                .map(StudentLetter::getStudentId)
                .collect(Collectors.toSet());
    }

    private Map<Long, StudentLetter> getLatestLetterMap(List<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return Map.of();
        }

        return studentLetterMapper.selectList(
                        new LambdaQueryWrapper<StudentLetter>()
                                .in(StudentLetter::getStudentId, studentIds)
                                .orderByDesc(StudentLetter::getUpdateTime)
                                .orderByDesc(StudentLetter::getId)
                )
                .stream()
                .collect(Collectors.toMap(
                        StudentLetter::getStudentId,
                        Function.identity(),
                        (oldValue, newValue) -> oldValue
                ));
    }

    private PageResult<StatisticsStudentVO> buildStudentPage(List<StatisticsStudentVO> records,
                                                             StatisticsQueryDTO queryDTO) {
        StatisticsQueryDTO query = queryDTO == null ? new StatisticsQueryDTO() : queryDTO;
        long page = positiveOrDefault(query.getPage(), 1L);
        long size = positiveOrDefault(query.getSize(), 10L);
        long total = records.size();
        long start = Math.min((page - 1) * size, total);
        long end = Math.min(start + size, total);

        PageResult<StatisticsStudentVO> result = PageResult.of(
                records.subList((int) start, (int) end),
                total
        );
        result.setPage(page);
        result.setSize(size);
        return result;
    }

    private long positiveOrDefault(Long value, long defaultValue) {
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return value;
    }

    private StatisticsStudentVO toStatisticsStudentVO(Student student,
                                                      StudentLetter letter,
                                                      SmsRecord smsRecord,
                                                      String sendStatus,
                                                      String readStatus) {
        StatisticsStudentVO vo = new StatisticsStudentVO();
        vo.setStudentId(student.getId());
        vo.setStudentNo(student.getStudentNo());
        vo.setStudentName(student.getName());
        vo.setStudentStatus(student.getStatus());
        vo.setCollegeId(student.getCollegeId());
        vo.setCollegeName(student.getCollegeId() == null ? null : getCollegeName(student.getCollegeId()));
        vo.setClassId(student.getClassId());
        vo.setClassName(student.getClassId() == null ? null : getClassName(student.getClassId()));
        vo.setCounselorId(student.getCounselorId());
        vo.setCounselorName(student.getCounselorId() == null ? null : getCounselorName(student.getCounselorId()));
        vo.setSendStatus(sendStatus);
        vo.setReadStatus(readStatus);

        if (letter != null) {
            vo.setLetterId(letter.getId());
            vo.setSendTime(letter.getSendTime());
            vo.setReadTime(letter.getReadTime());
        }
        if (vo.getSendTime() == null && smsRecord != null) {
            vo.setSendTime(smsRecord.getSendTime());
        }
        return vo;
    }

    private SendStatisticsVO getSendStatisticsForExport(StatisticsQueryDTO queryDTO) {
        String roleCode = SecurityUtil.getCurrentUserRoleCode();
        if (ROLE_SCHOOL.equals(roleCode)) {
            return getSchoolSendStatistics(queryDTO);
        }
        if (ROLE_COLLEGE.equals(roleCode)) {
            return getCollegeSendStatistics(queryDTO);
        }
        throw new BusinessException(403, "用户没有数据权限");
    }

    private List<SendStatisticsExportVO> toSendStatisticsExportRows(SendStatisticsVO statistics) {
        List<SendStatisticsExportVO> rows = new ArrayList<>();
        if (statistics.getRecords() != null) {
            rows.addAll(statistics.getRecords()
                    .stream()
                    .map(item -> toSendStatisticsExportRow(statistics.getTargetType(), item))
                    .toList());
        }
        if (statistics.getSummary() != null) {
            rows.add(toSendStatisticsExportRow(statistics.getTargetType(), statistics.getSummary()));
        }
        return rows;
    }

    private SendStatisticsExportVO toSendStatisticsExportRow(String targetType,
                                                            SendStatisticsItemVO item) {
        SendStatisticsExportVO row = new SendStatisticsExportVO();
        row.setTargetTypeName(targetTypeName(targetType));
        row.setTargetId(item.getTargetId());
        row.setTargetName(item.getTargetName());
        row.setShouldSendCount(item.getShouldSendCount());
        row.setActualSendCount(item.getActualSendCount());
        row.setUnsentCount(item.getUnsentCount());
        row.setCompletionRate(item.getCompletionRate());
        return row;
    }

    private String targetTypeName(String targetType) {
        if ("COLLEGE".equals(targetType)) {
            return "学院";
        }
        if ("COUNSELOR".equals(targetType)) {
            return "辅导员";
        }
        if ("CLASS".equals(targetType)) {
            return "班级";
        }
        return targetType;
    }

    private <T> void writeExcel(HttpServletResponse response,
                                String filename,
                                String sheetName,
                                Class<T> rowClass,
                                List<T> rows) throws IOException {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + encodedFilename);
        EasyExcel.write(response.getOutputStream(), rowClass)
                .autoCloseStream(false)
                .sheet(sheetName)
                .doWrite(rows);
    }

    private SendStatisticsVO buildSendStatistics(List<Student> students,
                                                 Function<Student, Long> groupGetter,
                                                 Function<Long, String> nameGetter,
                                                 String targetType) {
        List<Long> studentIds = students.stream()
                .map(Student::getId)
                .toList();
        Set<Long> actualSentStudentIds = getActualSentStudentIds(studentIds);

        Map<Long, List<Student>> groupedStudents = students.stream()
                .filter(student -> groupGetter.apply(student) != null)
                .collect(Collectors.groupingBy(
                        groupGetter,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<SendStatisticsItemVO> records = groupedStudents.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> buildSendStatisticsItem(
                        entry.getKey(),
                        nameGetter.apply(entry.getKey()),
                        entry.getValue(),
                        actualSentStudentIds
                ))
                .toList();

        SendStatisticsVO vo = new SendStatisticsVO();
        vo.setTargetType(targetType);
        vo.setRecords(records);
        vo.setSummary(buildSendStatisticsItem(null, "合计", students, actualSentStudentIds));
        return vo;
    }

    private SendStatisticsItemVO buildSendStatisticsItem(Long targetId,
                                                         String targetName,
                                                         List<Student> students,
                                                         Set<Long> actualSentStudentIds) {
        Long shouldSendCount = (long) students.size();
        Long actualSendCount = students.stream()
                .map(Student::getId)
                .filter(actualSentStudentIds::contains)
                .distinct()
                .count();

        SendStatisticsItemVO item = new SendStatisticsItemVO();
        item.setTargetId(targetId);
        item.setTargetName(targetName);
        item.setShouldSendCount(shouldSendCount);
        item.setActualSendCount(actualSendCount);
        item.setUnsentCount(Math.max(shouldSendCount - actualSendCount, 0L));
        item.setCompletionRate(rate(actualSendCount, shouldSendCount));
        return item;
    }

    private String getCollegeName(Long collegeId) {
        College college = collegeMapper.selectById(collegeId);
        return college == null ? "未知学院" : college.getName();
    }

    private String getCounselorName(Long counselorId) {
        SysUser counselor = sysUserMapper.selectById(counselorId);
        return counselor == null ? "未知辅导员" : counselor.getRealName();
    }

    private String getClassName(Long classId) {
        ClassInfo classInfo = classInfoMapper.selectById(classId);
        return classInfo == null ? "未知班级" : classInfo.getName();
    }
}
