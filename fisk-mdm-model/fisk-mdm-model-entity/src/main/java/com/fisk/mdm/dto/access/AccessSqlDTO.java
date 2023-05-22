package com.fisk.mdm.dto.access;

import lombok.Data;

/**
 * @author jianwenyang
 */
@Data
public class AccessSqlDTO {
    private long id;
    /**
     * 数据源id
     */
    private Integer dataSourceId;
    /**
     * 执行sql
     */
    private String sqlScript;
}
