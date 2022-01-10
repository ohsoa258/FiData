package com.fisk.chartvisual.service;

import com.fisk.dataservice.dto.DataDoFieldDTO;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/12/1 19:50
 */
public interface BuildSqlService {

    /**
     * 白泽数据源生成执行sql
     * @param apiConfigureFieldList
     * @return
     */
    Object query(List<DataDoFieldDTO> apiConfigureFieldList);
}
