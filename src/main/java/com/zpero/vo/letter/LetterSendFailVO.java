package com.zpero.vo.letter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LetterSendFailVO {

    private Long studentId;

    private String studentName;

    private String reason;
}
