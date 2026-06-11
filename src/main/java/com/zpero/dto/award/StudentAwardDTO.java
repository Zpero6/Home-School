package com.zpero.dto.award;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentAwardDTO {

    private String awardName;

    private String awardLevel;

    private LocalDateTime awardTime;
}
