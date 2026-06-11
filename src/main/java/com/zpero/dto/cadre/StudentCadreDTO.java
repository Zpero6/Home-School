package com.zpero.dto.cadre;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentCadreDTO {

    private String positionName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
