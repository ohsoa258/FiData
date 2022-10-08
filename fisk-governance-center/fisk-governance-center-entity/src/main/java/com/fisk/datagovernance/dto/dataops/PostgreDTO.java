package com.fisk.datagovernance.dto.dataops;

import com.fisk.datagovernance.enums.DataSourceTypeEnum;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description PG/SQL SERVICE
 * @date 2022/4/22 13:48
 */
@Data
public class PostgreDTO {
    public int id;
    public int port;
    public String ip;
    public String dbName;
    public DataSourceTypeEnum dataSourceTypeEnum;
    public String sqlUrl;
    public String sqlUsername;
    public String sqlPassword;
}
