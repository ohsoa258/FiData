package com.fisk.common.service.sqlparser.model;

import lombok.Data;

/**
 * @author gy
 * @version 1.0
 * @description 表信息
 * @date 2022/12/6 17:31
 */
@Data
public class TableInfo {
    public String name;
    public String alias;
    public String schema;
    public TableTypeEnum tableType;
}
