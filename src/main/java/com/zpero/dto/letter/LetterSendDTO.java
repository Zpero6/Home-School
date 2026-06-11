package com.zpero.dto.letter;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LetterSendDTO {

    private Long templateId;

    private List<Long> studentIds;

    private Map<String, String> customContents;
}
