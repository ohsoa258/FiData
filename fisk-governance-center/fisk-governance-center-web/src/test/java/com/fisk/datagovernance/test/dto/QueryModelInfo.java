package com.fisk.datagovernance.test.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/11/23 17:12
 */
@Data
public class QueryModelInfo {

    private List<String> tableNameList;
    private List<String> columnList;
    private Map<String, String> whereMap;

}