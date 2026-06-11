package com.zpero.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private Long total;

    private Long page;

    private Long size;

    private List<T> records;

    /**
     * 从 MyBatis Plus 分页对象转换
     */
    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPage(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    /**
     * 手动构建（只给 records 和 total，page/size 由调用方自己 set）
     */
    public static <T> PageResult<T> of(List<T> records, Long total) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        return result;
    }
}
