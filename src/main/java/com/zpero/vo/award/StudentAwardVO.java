package com.zpero.vo.award;

import com.zpero.entity.StudentAward;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentAwardVO {

    private Long id;

    private Long studentId;

    private String awardName;

    private String awardLevel;

    private LocalDateTime awardTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public StudentAwardVO(StudentAward award) {
        this.id = award.getId();
        this.studentId = award.getStudentId();
        this.awardName = award.getAwardName();
        this.awardLevel = award.getAwardLevel();
        this.awardTime = award.getAwardTime();
        this.createTime = award.getCreateTime();
        this.updateTime = award.getUpdateTime();
    }
}
