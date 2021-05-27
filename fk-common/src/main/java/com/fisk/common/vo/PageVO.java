package com.fisk.common.vo;

import lombok.Data;

import java.util.List;

/**
 * 通用的分页结果对象
 * @param <T> 分页数据的类型
 */
@Data
public class PageVO<T> {
    private Long total;// 总条数
    private Long totalPage;// 总页数
    private List<T> items;// 当前页数据

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