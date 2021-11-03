package com.fisk.dataaccess.dto.v3;

import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class DataSourceDTO {

    public long id;
    /**
     * 数据源
     */
    public String driveType;
    /**
     * 数据库列表
     */
    public List<DatabaseDTO> dbList;
}
