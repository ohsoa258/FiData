package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.DataDoFieldDTO;

import java.util.List;
import java.util.Map;

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
    List<Map<String, Object>> query(List<DataDoFieldDTO> apiConfigureFieldList, Integer id);
}
