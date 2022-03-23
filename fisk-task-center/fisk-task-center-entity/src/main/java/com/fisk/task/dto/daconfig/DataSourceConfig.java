package com.fisk.task.dto.daconfig;

import com.fisk.common.enums.task.nifi.DriverTypeEnum;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import lombok.Data;

import java.util.List;

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
    public int syncMode;
    public String targetTableName;
    /**
     * 物理表字段
     */
    public List<TableFieldsDTO> tableFieldsList;
}
