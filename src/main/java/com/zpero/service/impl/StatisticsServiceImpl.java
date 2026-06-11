package com.zpero.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.common.exception.BusinessException;
import com.zpero.dto.statistics.StatisticsQueryDTO;
import com.zpero.entity.ParentFeedback;
import com.zpero.entity.SmsRecord;
import com.zpero.entity.Student;
import com.zpero.entity.StudentLetter;
import com.zpero.mapper.ParentFeedbackMapper;
import com.zpero.mapper.SmsRecordMapper;
import com.zpero.mapper.StudentLetterMapper;
import com.zpero.mapper.StudentMapper;
import com.zpero.security.dataScope.DataScopeContext;
import com.zpero.security.dataScope.DataScopeProvider;
import com.zpero.service.StatisticsService;
import com.zpero.vo.statistics.FeedbackStatisticsVO;
import com.zpero.vo.statistics.ReadStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final String STUDENT_STATUS_IN_SCHOOL = "在校";
    private static final String SMS_STATUS_SUCCESS = "SUCCESS";
    private static final String LETTER_STATUS_READ = "READ";

    private final StudentMapper studentMapper;
    private final SmsRecordMapper smsRecordMapper;
    private final StudentLetterMapper studentLetterMapper;
    private final ParentFeedbackMapper parentFeedbackMapper;
    private final DataScopeProvider dataScopeProvider;

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

    private List<Long> getScopedStudentIds(StatisticsQueryDTO queryDTO) {
        StatisticsQueryDTO query = queryDTO == null ? new StatisticsQueryDTO() : queryDTO;

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStatus, STUDENT_STATUS_IN_SCHOOL);
        applyStudentDataScope(wrapper);
        wrapper.eq(query.getCollegeId() != null, Student::getCollegeId, query.getCollegeId())
                .eq(query.getCounselorId() != null, Student::getCounselorId, query.getCounselorId())
                .eq(query.getClassId() != null, Student::getClassId, query.getClassId());

        return studentMapper.selectList(wrapper)
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
        if (studentIds.isEmpty()) {
            return 0L;
        }

        return smsRecordMapper.selectList(
                        new LambdaQueryWrapper<SmsRecord>()
                                .in(SmsRecord::getStudentId, studentIds)
                                .eq(SmsRecord::getStatus, SMS_STATUS_SUCCESS)
                )
                .stream()
                .map(SmsRecord::getStudentId)
                .distinct()
                .count();
    }

    private Long countReadStudents(List<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return 0L;
        }

        return studentLetterMapper.selectCount(
                new LambdaQueryWrapper<StudentLetter>()
                        .in(StudentLetter::getStudentId, studentIds)
                        .eq(StudentLetter::getStatus, LETTER_STATUS_READ)
        );
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
}
