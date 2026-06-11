package com.zpero.vo.score;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScoreImportResultVO {

    private int totalCount;

    private int successCount;

    private int failCount;

    private List<FailItem> failList = new ArrayList<>();

    public void addSuccess() {
        this.successCount++;
    }

    public void addFail(int rowNum, String studentNo, String reason) {
        this.failCount++;
        this.failList.add(new FailItem(rowNum, studentNo, reason));
    }

    @Data
    @AllArgsConstructor
    public static class FailItem {

        private int rowNum;

        private String studentNo;

        private String reason;
    }
}
