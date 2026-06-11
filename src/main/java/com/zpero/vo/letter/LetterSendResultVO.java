package com.zpero.vo.letter;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LetterSendResultVO {

    private Integer totalCount = 0;

    private Integer successCount = 0;

    private Integer failCount = 0;

    private List<LetterSendFailVO> failList = new ArrayList<>();

    public void addSuccess() {
        this.successCount++;
    }

    public void addFail(Long studentId, String studentName, String reason) {
        this.failCount++;
        this.failList.add(new LetterSendFailVO(studentId, studentName, reason));
    }
}
