package com.fisk.dataservice.dto.datasource;

import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description DataDomainDTO
 * @date 2022/1/6 14:51
 */
@Data
public class DataDomainDTO {
    public String tableName;
    public String tableDetails;
    public String columnName;
    public String columnDetails;
}
