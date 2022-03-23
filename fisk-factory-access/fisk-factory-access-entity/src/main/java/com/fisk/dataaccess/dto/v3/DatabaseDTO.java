package com.fisk.dataaccess.dto.v3;

import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class DatabaseDTO {
    public int type;
    /**
     * 数据库名称
     */
    public String databaseName;
    /**
     * 表列表
     */
    public List<TableDTO> tableList;
}
