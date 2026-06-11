package com.zpero.dto.letter;

import lombok.Data;

@Data
public class LetterQueryDTO {

    private Long page = 1L;

    private Long size = 10L;

    private Long studentId;

    private String status;
}
