package com.fisk.mdm.dto.access;

import lombok.Data;

/**
 * @author jianwenyang
 */
@Data
public class AccessSqlDTO {
    private long id;
    private Integer dataSourceId;
    private String sqlScript;
}
