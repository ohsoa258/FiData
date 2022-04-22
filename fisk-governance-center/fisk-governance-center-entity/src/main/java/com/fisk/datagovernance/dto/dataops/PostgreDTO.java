package com.fisk.datagovernance.dto.dataops;

import com.fisk.datagovernance.enums.dataquality.DataSourceTypeEnum;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description PostgreDTO
 * @date 2022/4/22 13:48
 */
@Data
public class PostgreDTO {
    public int id;
    public int port;
    public String ip;
    public String dbName;
    public DataSourceTypeEnum dataSourceTypeEnum;
    public String pgsqlUrl;
    public String pgsqlUsername;
    public String pgsqlPassword;
}
