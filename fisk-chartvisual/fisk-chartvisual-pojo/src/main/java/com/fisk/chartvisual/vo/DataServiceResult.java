package com.fisk.chartvisual.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author gy
 */
@Data
public class DataServiceResult {
    public Map<String,Object> aggregation;
    public List<Map<String, Object>> data;
}
