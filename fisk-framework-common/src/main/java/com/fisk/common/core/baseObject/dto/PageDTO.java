package com.fisk.common.core.baseObject.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * 通用的分页结果对象
 * @param <T> 分页数据的类型
 */
@Data
public class PageDTO<T> {
    private Long total;
    private Long totalPage;
    private List<T> items;

    public PageDTO() {
    }

    public PageDTO(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public PageDTO(Long total, Long totalPage, List<T> items) {
        this.total = total;
        this.totalPage = totalPage;
        this.items = items;
    }
}