package com.fisk.task.dto.daconfig;

import com.fisk.common.enums.task.nifi.DriverTypeEnum;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class DataSourceConfig {
    public String componentId;
    public String jdbcStr;
    public DriverTypeEnum type;
    public String user;
    public String password;
}
