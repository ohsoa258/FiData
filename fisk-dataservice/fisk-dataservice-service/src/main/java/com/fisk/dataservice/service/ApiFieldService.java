package com.fisk.dataservice.service;

import org.apache.poi.ss.formula.functions.T;

import java.util.List;

/**
 * @author wangyan
 */
public interface ApiFieldService {

    /**
     * 根据路径查询拼接sql
     * @param apiRoute
     * @param offset 第几条显示
     * @param limit  页数
     * @return
     */
    List<Object> queryField(String apiRoute, Integer offset, Integer limit);
}
