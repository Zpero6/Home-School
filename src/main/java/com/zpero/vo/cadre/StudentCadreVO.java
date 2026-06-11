package com.zpero.vo.cadre;

import com.zpero.entity.StudentCadre;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentCadreVO {

    private Long id;

    private Long studentId;

    private String positionName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public StudentCadreVO(StudentCadre cadre) {
        this.id = cadre.getId();
        this.studentId = cadre.getStudentId();
        this.positionName = cadre.getPositionName();
        this.startTime = cadre.getStartTime();
        this.endTime = cadre.getEndTime();
        this.createTime = cadre.getCreateTime();
        this.updateTime = cadre.getUpdateTime();
    }
}
