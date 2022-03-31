package com.fisk.dataaccess.dto.v3;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableDTO {
    public int type;
    /**
     * 表名称
     */
    public String tableName;
}
