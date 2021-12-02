package com.fisk.dataservice.service;

import com.fisk.dataservice.dto.DataDoFieldDTO;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/12/1 19:50
 */
public interface BuildSqlService {

    /**
     * 执行sql
     * @param apiConfigureFieldList
     * @return
     */
    Object query(List<DataDoFieldDTO> apiConfigureFieldList);
}
