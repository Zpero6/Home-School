package com.zpero.common.result;


import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

@Data
public class PageResult {

    private Long total;

    private Long page;

    private Long size;

    private List<T> records;
}
