package com.zpero.dto.link;

import lombok.Data;

import java.util.List;

@Data
public class ExternalLinkSortDTO {

    private List<SortItem> links;

    @Data
    public static class SortItem {

        private Long id;

        private Integer sort;
    }
}
