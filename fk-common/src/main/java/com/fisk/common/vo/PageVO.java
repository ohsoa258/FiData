package com.fisk.common.vo;

import lombok.Data;

import java.util.List;

/**
 * @author gy
 * 通用的分页结果对象
 * @param <T> 分页数据的类型
 */
@Data
public class PageVO<T> {
    private Long total;
    private Long totalPage;
    private List<T> items;

    public PageVO() {
    }

    public PageVO(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public PageVO(Long total, Long totalPage, List<T> items) {
        this.total = total;
        this.totalPage = totalPage;
        this.items = items;
    }
}