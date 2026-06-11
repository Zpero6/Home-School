package com.zpero.dto.letter;

import lombok.Data;

import java.util.List;

@Data
public class LetterResendDTO {

    private List<Long> letterIds;
}
