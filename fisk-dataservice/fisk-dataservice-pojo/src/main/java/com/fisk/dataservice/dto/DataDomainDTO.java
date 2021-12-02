package com.fisk.dataservice.dto;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/12/1 21:46
 */
@Data
public class DataDomainDTO {
    public String tableName;
    public String tableDetails;
    public String columnName;
    public String columnDetails;
}
