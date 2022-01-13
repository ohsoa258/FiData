package com.fisk.dataservice.vo.datasource;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author dick
 * @version v1.0
 * @description DataServiceResult
 * @date 2022/1/6 14:51
 */
@Data
public class DataServiceResult {
    public Map<String,Object> aggregation;
    public List<Map<String, Object>> data;
    public List<Map<String, Object>> tableColumn;
}
